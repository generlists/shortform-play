package com.sean.ratel.android.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingPreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private val autoPlay = booleanPreferencesKey("AUTO_PLAY")
        private val loopPlay = booleanPreferencesKey("LOOP_PLAY")
        private val pipPlay = booleanPreferencesKey("PIP_PLAY")
        private val soundOnOff = booleanPreferencesKey("SOUND_ONOFF")
        private val wifiPlay = booleanPreferencesKey("WIFI_ONLY")

        suspend fun setPIPPlay(isPIPMode: Boolean) {
            dataStore.edit { it[pipPlay] = isPIPMode }
        }

        suspend fun setAutoPlay(isAutoPlay: Boolean) {
            dataStore.edit { it[autoPlay] = isAutoPlay }
        }

        suspend fun setLoopPlay(isLoop: Boolean) {
            dataStore.edit { it[loopPlay] = isLoop }
        }

        suspend fun setWifiOnlyPlay(isWifiPlay: Boolean) {
            dataStore.edit { it[wifiPlay] = isWifiPlay }
        }

        suspend fun setSoundOnOff(isSound: Boolean) {
            dataStore.edit { it[soundOnOff] = isSound }
        }

        suspend fun getAutoPlay(): Boolean = dataStore.data.map { it[autoPlay] }.first() ?: true

        suspend fun getLoopPlay(): Boolean = dataStore.data.map { it[loopPlay] }.first() ?: true

        suspend fun getPIPPlay(): Boolean = dataStore.data.map { it[pipPlay] }.first() ?: true

        suspend fun getWifiOnlyPlay(): Boolean = dataStore.data.map { it[wifiPlay] }.first() ?: true

        suspend fun getSoundOnOff(): Boolean = dataStore.data.map { it[soundOnOff] }.first() ?: false
    }
