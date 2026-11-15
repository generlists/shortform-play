package com.sean.ratel.android.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.sean.ratel.android.data.filter.ExcludeFilter
import com.sean.ratel.android.data.filter.FilterOptions
import com.sean.ratel.android.data.filter.FilterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** 내 제외 필터를 기기에 저장한다(로그인 사용자는 이 값을 서버와 동기화한다). 목록은 JSON 문자열로 둔다. */
@Singleton
class FilterPreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private val gson = Gson()

        private val topicsKey = stringPreferencesKey("exclude_filter_topics")
        private val keywordsKey = stringPreferencesKey("exclude_filter_keywords")
        private val channelsKey = stringPreferencesKey("exclude_filter_channels")
        private val exceptionsKey = stringPreferencesKey("exclude_filter_exceptions")
        private val versionKey = intPreferencesKey("exclude_filter_version")
        private val hideDislikedKey = booleanPreferencesKey("exclude_filter_hide_disliked")
        private val hideWatchedKey = booleanPreferencesKey("exclude_filter_hide_watched")
        private val dirtyKey = booleanPreferencesKey("exclude_filter_dirty")

        // 온보딩을 봤는지. 필터와 달리 로그아웃해도 지우지 않는다(온보딩은 설치당 한 번).
        private val onboardingSeenKey = booleanPreferencesKey("exclude_filter_onboarding_seen")

        val onboardingSeen: Flow<Boolean> = dataStore.data.map { it[onboardingSeenKey] ?: false }.distinctUntilChanged()

        suspend fun markOnboardingSeen() {
            dataStore.edit { it[onboardingSeenKey] = true }
        }

        // 홈의 "내 필터 만들기" 배너를 "나중에"로 닫았는지. 로그아웃해도 지우지 않는다(기기 설정).
        private val homeBannerDismissedKey = booleanPreferencesKey("exclude_filter_home_banner_dismissed")

        val homeBannerDismissed: Flow<Boolean> = dataStore.data.map { it[homeBannerDismissedKey] ?: false }.distinctUntilChanged()

        suspend fun dismissHomeBanner() {
            dataStore.edit { it[homeBannerDismissedKey] = true }
        }

        val state: Flow<FilterState> =
            dataStore.data
                .map { prefs ->
                    FilterState(
                        filter =
                            ExcludeFilter(
                                topics = decode(prefs[topicsKey]),
                                keywords = decode(prefs[keywordsKey]),
                                blockedChannels = decode(prefs[channelsKey]),
                                exceptions = decode(prefs[exceptionsKey]),
                                version = prefs[versionKey] ?: 0,
                            ),
                        options =
                            FilterOptions(
                                hideDisliked = prefs[hideDislikedKey] ?: false,
                                hideWatched = prefs[hideWatchedKey] ?: false,
                            ),
                        dirty = prefs[dirtyKey] ?: false,
                    )
                }.distinctUntilChanged()

        suspend fun save(state: FilterState) {
            dataStore.edit { prefs ->
                prefs[topicsKey] = gson.toJson(state.filter.topics)
                prefs[keywordsKey] = gson.toJson(state.filter.keywords)
                prefs[channelsKey] = gson.toJson(state.filter.blockedChannels)
                prefs[exceptionsKey] = gson.toJson(state.filter.exceptions)
                prefs[versionKey] = state.filter.version
                prefs[hideDislikedKey] = state.options.hideDisliked
                prefs[hideWatchedKey] = state.options.hideWatched
                prefs[dirtyKey] = state.dirty
            }
        }

        /** 로그아웃·탈퇴할 때 필터를 지운다(피드가 원래대로 돌아온다). */
        suspend fun clear() {
            dataStore.edit { prefs ->
                listOf(topicsKey, keywordsKey, channelsKey, exceptionsKey).forEach { prefs.remove(it) }
                prefs.remove(versionKey)
                prefs.remove(hideDislikedKey)
                prefs.remove(hideWatchedKey)
                prefs.remove(dirtyKey)
            }
        }

        private fun decode(json: String?): List<String> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                gson.fromJson(json, Array<String>::class.java)?.filterNotNull().orEmpty()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
