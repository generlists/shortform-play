package com.sean.ratel.android.data.watch

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.LocalDate

class WatchStatsRepositoryTest {
    private lateinit var scope: CoroutineScope
    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: WatchStatsRepository
    private val day = LocalDate.of(2026, 11, 14)

    @Before
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        file = File.createTempFile("watch-test", ".preferences_pb").also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        repository = WatchStatsRepository(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
        file.delete()
    }

    @Test
    fun `starts empty then accumulates records and survives a new repository`() =
        runBlocking {
            assertTrue(repository.days.first().isEmpty())
            repository.record(WatchDelta(day, 30, ChannelWatch("c1", "채널")))
            repository.record(WatchDelta(day, 15, null))
            val saved = repository.days.first().getValue(day)
            assertEquals(45L, saved.seconds)
            assertEquals(1, saved.videos)
            assertEquals("채널", saved.channels.single().name)

            val again = WatchStatsRepository(dataStore).days.first().getValue(day)
            assertEquals(saved, again)
        }

    @Test
    fun `clear removes everything`() =
        runBlocking {
            repository.record(WatchDelta(day, 30, null))
            repository.clear()
            assertTrue(repository.days.first().isEmpty())
        }

    @Test
    fun `corrupted storage reads as empty and the next record starts fresh`() =
        runBlocking {
            dataStore.edit { it[stringPreferencesKey("watch_stats_v1")] = "{not json" }
            assertTrue(repository.days.first().isEmpty())
            repository.record(WatchDelta(day, 5, null))
            assertEquals(5L, repository.days.first().getValue(day).seconds)
        }

    @Test
    fun `hidden videos are counted once per day across calls and survive other records`() =
        runBlocking {
            repository.recordHidden(day, listOf("a", "b"))
            repository.recordHidden(day, listOf("b", "c"))
            repository.record(WatchDelta(day, 20, null))
            assertEquals(3, repository.days.first().getValue(day).hidden)
            assertEquals(20L, repository.days.first().getValue(day).seconds)

            repository.recordHidden(day.plusDays(1), listOf("a"))
            assertEquals(1, repository.days.first().getValue(day.plusDays(1)).hidden)
            assertEquals(3, repository.days.first().getValue(day).hidden)
        }

    @Test
    fun `recording nothing hidden changes nothing and clear removes the hidden ids too`() =
        runBlocking {
            repository.recordHidden(day, emptyList())
            assertTrue(repository.days.first().isEmpty())
            repository.recordHidden(day, listOf("a"))
            repository.clear()
            repository.recordHidden(day, listOf("a"))
            assertEquals(1, repository.days.first().getValue(day).hidden)
        }
}
