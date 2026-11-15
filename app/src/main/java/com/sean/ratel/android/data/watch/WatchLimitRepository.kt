package com.sean.ratel.android.data.watch

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** 하루 시청 한도 설정과 마지막 "계속 보기"를 기기에 저장한다(서버로 보내지 않는다). 잘못된 값은 기본값으로 읽는다. */
@Singleton
class WatchLimitRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private val modeKey = stringPreferencesKey("watch_limit_mode")
        private val minutesKey = intPreferencesKey("watch_limit_minutes")
        private val videosKey = intPreferencesKey("watch_limit_videos")
        private val ackDateKey = stringPreferencesKey("watch_limit_ack_date")
        private val ackSecondsKey = longPreferencesKey("watch_limit_ack_seconds")
        private val ackVideosKey = intPreferencesKey("watch_limit_ack_videos")

        val settings: Flow<WatchLimitSettings> =
            dataStore.data
                .map { prefs ->
                    WatchLimitSettings(
                        mode = prefs[modeKey]?.let { name -> LimitMode.entries.firstOrNull { it.name == name } } ?: LimitMode.OFF,
                        minutes = WatchLimitPolicy.normalizeMinutes(prefs[minutesKey]),
                        videos = WatchLimitPolicy.normalizeVideos(prefs[videosKey]),
                    )
                }.distinctUntilChanged()

        val ack: Flow<LimitAck?> =
            dataStore.data
                .map { prefs ->
                    val date = prefs[ackDateKey]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    date?.let { LimitAck(it, prefs[ackSecondsKey] ?: 0L, prefs[ackVideosKey] ?: 0) }
                }.distinctUntilChanged()

        // 한도 설정을 바꾸면 이전 "계속 보기" 기록을 지운다 — 새 한도가 바로 적용돼야 예측할 수 있다

        suspend fun setMode(mode: LimitMode) {
            dataStore.edit {
                it[modeKey] = mode.name
                clearAck(it)
            }
        }

        suspend fun setMinutes(minutes: Int) {
            dataStore.edit {
                it[minutesKey] = WatchLimitPolicy.normalizeMinutes(minutes)
                clearAck(it)
            }
        }

        suspend fun setVideos(videos: Int) {
            dataStore.edit {
                it[videosKey] = WatchLimitPolicy.normalizeVideos(videos)
                clearAck(it)
            }
        }

        private fun clearAck(prefs: MutablePreferences) {
            prefs.remove(ackDateKey)
            prefs.remove(ackSecondsKey)
            prefs.remove(ackVideosKey)
        }

        suspend fun acknowledge(ack: LimitAck) {
            dataStore.edit {
                it[ackDateKey] = ack.date.toString()
                it[ackSecondsKey] = ack.seconds
                it[ackVideosKey] = ack.videos
            }
        }
    }
