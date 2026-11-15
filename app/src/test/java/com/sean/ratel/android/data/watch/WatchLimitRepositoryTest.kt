package com.sean.ratel.android.data.watch

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.LocalDate

class WatchLimitRepositoryTest {
    private lateinit var scope: CoroutineScope
    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: WatchLimitRepository

    @Before
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        file = File.createTempFile("limit-test", ".preferences_pb").also { it.delete() }
        dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        repository = WatchLimitRepository(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
        file.delete()
    }

    @Test
    fun `defaults are off with the default options and no acknowledgement`() =
        runBlocking {
            assertEquals(WatchLimitSettings(), repository.settings.first())
            assertNull(repository.ack.first())
        }

    @Test
    fun `settings persist across a new repository`() =
        runBlocking {
            repository.setMode(LimitMode.VIDEOS)
            repository.setMinutes(90)
            repository.setVideos(50)
            assertEquals(WatchLimitSettings(LimitMode.VIDEOS, 90, 50), WatchLimitRepository(dataStore).settings.first())
        }

    @Test
    fun `an unknown option written by mistake is stored as the default`() =
        runBlocking {
            repository.setMinutes(7)
            repository.setVideos(3)
            val saved = repository.settings.first()
            assertEquals(WatchLimitPolicy.DEFAULT_MINUTES, saved.minutes)
            assertEquals(WatchLimitPolicy.DEFAULT_VIDEOS, saved.videos)
        }

    @Test
    fun `corrupted stored values read as defaults`() =
        runBlocking {
            dataStore.edit {
                it[stringPreferencesKey("watch_limit_mode")] = "NOPE"
                it[intPreferencesKey("watch_limit_minutes")] = 13
                it[stringPreferencesKey("watch_limit_ack_date")] = "not-a-date"
            }
            assertEquals(WatchLimitSettings(), repository.settings.first())
            assertNull(repository.ack.first())
        }

    @Test
    fun `the acknowledgement is saved with its day`() =
        runBlocking {
            val ack = LimitAck(LocalDate.of(2026, 11, 14), 1_800L, 12)
            repository.acknowledge(ack)
            assertEquals(ack, repository.ack.first())
        }

    @Test
    fun `changing any limit setting forgets the earlier acknowledgement`() =
        runBlocking {
            val ack = LimitAck(LocalDate.of(2026, 11, 14), 1_800L, 12)
            repository.acknowledge(ack)
            repository.setMode(LimitMode.TIME)
            assertNull(repository.ack.first())

            repository.acknowledge(ack)
            repository.setMinutes(60)
            assertNull(repository.ack.first())

            repository.acknowledge(ack)
            repository.setVideos(50)
            assertNull(repository.ack.first())
        }
}
