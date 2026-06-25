package com.sean.ratel.android.data.repository

import com.sean.ratel.android.data.local.pref.SettingPreference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class
SettingRepository
    @Inject
    constructor(
        private val settingPreference: SettingPreference,
    ) {
        suspend fun setAutoPlay(isAutoPlay: Boolean) {
            settingPreference.setAutoPlay(isAutoPlay)
        }

        suspend fun setLoopPlay(isLoop: Boolean) {
            settingPreference.setLoopPlay(isLoop)
        }

        suspend fun setPIPPlay(isPIPMode: Boolean) {
            settingPreference.setPIPPlay(isPIPMode)
        }

        suspend fun setSoundOnOff(isSound: Boolean) {
            settingPreference.setSoundOnOff(isSound)
        }

        suspend fun setCaptionEnabled(caption: Boolean) {
            settingPreference.setCaptionEnabled(caption)
        }

        suspend fun setWifiOnlyPlay(isWifiPlay: Boolean) {
            settingPreference.setWifiOnlyPlay(isWifiPlay)
        }

        suspend fun setLocale(locale: String) {
            settingPreference.setLocale(locale)
        }

        suspend fun setNewUpdate(update: Boolean) {
            settingPreference.setNewUpdate(update)
        }

        suspend fun setUserId() {
            settingPreference.setUserId()
        }

        suspend fun getAutoPlay() = settingPreference.getAutoPlay()

        suspend fun getLoopPlay(): Boolean = settingPreference.getLoopPlay()

        suspend fun getSoundOnOff(): Boolean = settingPreference.getSoundOnOff()

        suspend fun getWifiOnlyPlay(): Boolean = settingPreference.getWifiOnlyPlay()

        fun getCaptionEnabled(): Boolean = settingPreference.getCaptionEnabled()

        suspend fun getPIPPlay() = settingPreference.getPIPPlay()

        fun getLocale() = settingPreference.getLocale()

        val userIdFlow: Flow<String?> = settingPreference.userIdFlow
    }
