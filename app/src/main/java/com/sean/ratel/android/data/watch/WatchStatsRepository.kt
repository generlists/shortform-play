package com.sean.ratel.android.data.watch

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 하루 합계 시청 기록을 **기기에만** 저장한다(서버로 보내지 않는다). 영상 목록은 저장하지 않고,
 * 날짜별 시간·본 영상 수·채널별 횟수만 90일까지 둔다.
 */
@Singleton
class WatchStatsRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private val gson = Gson()
        private val key = stringPreferencesKey("watch_stats_v1")
        private val hiddenKey = stringPreferencesKey("watch_hidden_today_v1")

        /** 날짜 → 하루 합계. 읽을 수 없는 값이면 빈 맵. */
        val days: Flow<Map<LocalDate, DayWatch>> =
            dataStore.data
                .map { decode(it[key]) }
                .map { raw -> raw.mapNotNull { (k, v) -> WatchLog.parseDate(k)?.let { it to v } }.toMap() }
                .distinctUntilChanged()

        suspend fun record(delta: WatchDelta) {
            dataStore.edit { prefs ->
                prefs[key] = gson.toJson(WatchLog.apply(decode(prefs[key]), delta))
            }
        }

        /** 오늘 내 필터가 빼고 보여준 영상들을 센다. 같은 영상은 하루에 한 번만 센다. */
        suspend fun recordHidden(
            date: LocalDate,
            ids: Collection<String>,
        ) {
            if (ids.isEmpty()) return
            dataStore.edit { prefs ->
                val merged = WatchLog.mergeHidden(decodeHidden(prefs[hiddenKey]), date, ids)
                prefs[hiddenKey] = gson.toJson(merged)
                prefs[key] = gson.toJson(WatchLog.withHidden(decode(prefs[key]), date, merged.ids.size))
            }
        }

        suspend fun clear() {
            dataStore.edit {
                it.remove(key)
                it.remove(hiddenKey)
            }
        }

        private fun decodeHidden(json: String?): HiddenToday? {
            if (json.isNullOrBlank()) return null
            return try {
                gson.fromJson(json, HiddenToday::class.java)
            } catch (e: Exception) {
                null
            }
        }

        private fun decode(json: String?): Map<String, DayWatch> {
            if (json.isNullOrBlank()) return emptyMap()
            return try {
                val type = object : TypeToken<Map<String, DayWatch>>() {}.type
                gson.fromJson<Map<String, DayWatch>>(json, type) ?: emptyMap()
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
