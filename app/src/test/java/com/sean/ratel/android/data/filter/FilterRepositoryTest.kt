package com.sean.ratel.android.data.filter

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.sean.ratel.android.data.local.pref.FilterPreference
import com.sean.ratel.android.data.local.pref.RecentVideoPreference
import com.sean.ratel.android.data.local.pref.YouTubeEndPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/** DataStore 를 임시 파일로 만들어 저장·컴파일·신호 연동을 확인한다(안드로이드 기기 없이 JVM 에서). */
class FilterRepositoryTest {
    private val topicsJson =
        """{"version":1,"topics":[{"id":"mukbang","names":{"ko":"먹방","en":"Mukbang"},
            "terms":{"ko":["먹방"],"en":["mukbang"],"ja":["モッパン"]}}]}"""

    private lateinit var scope: CoroutineScope
    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preference: FilterPreference
    private lateinit var repository: FilterRepository

    @Before
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        file = File.createTempFile("filter-test", ".preferences_pb").also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        preference = FilterPreference(dataStore)
        repository =
            FilterRepository(
                preference,
                TopicPackRepository { topicsJson },
                RecentVideoPreference(dataStore),
                YouTubeEndPreference(dataStore),
            )
    }

    @After
    fun tearDown() {
        scope.cancel()
        file.delete()
    }

    private fun <T> run(block: suspend () -> T): T = runBlocking { withTimeout(10_000) { block() } }

    @Test
    fun `nothing saved means no-op`() =
        run {
            assertTrue(repository.compiled("KR").first().isNoOp)
            assertEquals(FilterState(), repository.state.first())
        }

    @Test
    fun `saved state round trips`() =
        run {
            val state =
                FilterState(
                    ExcludeFilter(
                        topics = listOf("mukbang"),
                        keywords = listOf("직캠", "boy band"),
                        blockedChannels = listOf("UC1"),
                        exceptions = listOf("v1"),
                        version = 4,
                    ),
                    FilterOptions(hideDisliked = true, hideWatched = false),
                )
            repository.save(state)
            assertEquals(state, repository.state.first())
        }

    @Test
    fun `saved keywords and channels filter videos`() =
        run {
            repository.save(
                FilterState(ExcludeFilter(keywords = listOf("직캠"), blockedChannels = listOf("UCbad"))),
            )
            val compiled = repository.compiled("KR").first { !it.isNoOp }
            assertFalse(compiled.keep(video("a", title = "무대 직캠")))
            assertFalse(compiled.keep(video("b", channelId = "UCbad")))
            assertTrue(compiled.keep(video("c")))
        }

    @Test
    fun `topics resolve to the selected country language`() =
        run {
            repository.save(FilterState(ExcludeFilter(topics = listOf("mukbang"))))

            val kr = repository.compiled("KR").first { !it.isNoOp }
            assertEquals(FilterReason.TOPIC, kr.evaluate(video("a", title = "라면 먹방"))?.reason)
            assertEquals(FilterReason.TOPIC, kr.evaluate(video("b", title = "mukbang time"))?.reason)

            // 미국은 영어 단어만 쓴다
            val us = repository.compiled("US").first { !it.isNoOp }
            assertNull(us.evaluate(video("c", title = "라면 먹방")))
            assertNotNull(us.evaluate(video("d", title = "mukbang time")))

            val jp = repository.compiled("JP").first { !it.isNoOp }
            assertNotNull(jp.evaluate(video("e", title = "大人気モッパン")))
        }

    @Test
    fun `disliked videos follow the dislike keys and the option`() =
        run {
            // 앱이 싫어요를 누르면 DataStore 에 "disLike{videoId}" 키가 생긴다(YouTubeEndCompose)
            dataStore.edit { it[stringPreferencesKey("disLikeV1")] = "{}" }
            dataStore.edit { it[stringPreferencesKey("likeV2")] = "{}" }

            repository.save(FilterState(options = FilterOptions(hideDisliked = true)))
            val compiled = repository.compiled("KR").first { !it.isNoOp }
            assertFalse(compiled.keep(video("V1")))
            assertTrue(compiled.keep(video("V2"))) // 좋아요는 숨기지 않는다

            // 새로 싫어요를 누르면 필터가 다시 만들어진다
            dataStore.edit { it[stringPreferencesKey("disLikeV3")] = "{}" }
            val updated = repository.compiled("KR").first { !it.keep(video("V3")) }
            assertFalse(updated.keep(video("V3")))

            // 옵션을 끄면 싫어요를 숨기지 않는다
            repository.save(FilterState(options = FilterOptions(hideDisliked = false)))
            assertTrue(repository.compiled("KR").first().isNoOp)
        }

    @Test
    fun `watched videos come from the recent video list`() =
        run {
            val recent = Gson().toJson(listOf(video("W1"), video("W2")))
            dataStore.edit { it[stringPreferencesKey("recent_video_list")] = recent }

            repository.save(FilterState(options = FilterOptions(hideWatched = true)))
            val compiled = repository.compiled("KR").first { !it.isNoOp }
            assertFalse(compiled.keep(video("W1")))
            assertFalse(compiled.keep(video("W2")))
            assertTrue(compiled.keep(video("W3")))
        }

    @Test
    fun `broken recent list does not crash`() =
        run {
            dataStore.edit { it[stringPreferencesKey("recent_video_list")] = "{ not json" }
            repository.save(FilterState(ExcludeFilter(keywords = listOf("직캠")), FilterOptions(hideWatched = true)))
            val compiled = repository.compiled("KR").first { !it.isNoOp }
            assertFalse(compiled.keep(video("a", title = "직캠")))
            assertTrue(compiled.keep(video("b")))
        }

    @Test
    fun `clear restores the default no-op`() =
        run {
            repository.save(FilterState(ExcludeFilter(keywords = listOf("직캠"), version = 2), FilterOptions(hideDisliked = true)))
            repository.clear()
            assertEquals(FilterState(), repository.state.first())
            assertTrue(repository.compiled("KR").first().isNoOp)
        }

    @Test
    fun `corrupted stored lists fall back to empty`() =
        run {
            dataStore.edit {
                it[stringPreferencesKey("exclude_filter_keywords")] = "{ not json"
                it[stringPreferencesKey("exclude_filter_topics")] = "null"
            }
            assertEquals(FilterState(), repository.state.first())
        }

    @Test
    fun `onboarding seen flag is separate from the filter and survives clear`() =
        run {
            assertFalse(repository.onboardingSeen.first())
            repository.markOnboardingSeen()
            assertTrue(repository.onboardingSeen.first())
            repository.clear()
            assertTrue(repository.onboardingSeen.first())
            assertEquals(FilterState(), repository.state.first())
        }
}
