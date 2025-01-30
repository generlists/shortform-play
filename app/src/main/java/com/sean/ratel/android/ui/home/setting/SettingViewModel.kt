package com.sean.ratel.android.ui.home.setting

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.repository.SettingRepository
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.Navigator
import com.sean.ratel.android.utils.PhoneUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel
    @Inject
    constructor(
        private val navigator: Navigator,
        private val settingRepository: SettingRepository,
        private val gaLog: GALog,
    ) : ViewModel() {
        fun runAppManagerDetail() {
            navigator.navigateTo(Destination.AppManager.route)
        }

        fun runNotice() {
            navigator.navigateTo(Destination.Notices.route)
        }

        fun runQNA(context: Context) = PhoneUtil.sendEmail(context, STRINGS.FEEDBACK_TITLE, qnaResource(context))

        fun qnaResource(context: Context): String {
            val str = StringBuilder()
            str.append("App Version : ")
            str.append(PhoneUtil.getAppVersionName(context))
            str.append("\n")
            str.append(PhoneUtil.getEnvironment())
            return str.toString()
        }

        fun runLegal() {
            navigator.navigateTo(Destination.Regal.route)
        }

        fun runAppDetail() {
            navigator.navigateTo(Destination.SettingAppManagerDetail.route)
        }

        fun runAppLink(
            context: Context,
            url: String,
        ) {
            PhoneUtil.openBrowsere(context, url)
        }

        fun runAppStore(
            context: Context,
            storeUrl: String,
        ) {
            try {
                val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(storeUrl)
                        setPackage("com.android.vending")
                    }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                val fallbackIntent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            STRINGS.URLUPDATE_GOOGLE_PLAY_WEB(
                                context.getString(
                                    R.string.title,
                                ),
                            ),
                        ),
                    )
                context.startActivity(fallbackIntent)
            }
        }

        fun goAppSettingsOpenSourceLicense(context: Context) = PhoneUtil.goAppSettingsOpenSourceLicense(context)

        suspend fun getAutoPlay(): Boolean = settingRepository.getAutoPlay()

        suspend fun getLoopPlay(): Boolean = settingRepository.getLoopPlay()

        suspend fun getPIPPlay(): Boolean = settingRepository.getPIPPlay()

        suspend fun getWifiOnlyPlay(): Boolean = settingRepository.getWifiOnlyPlay()

        suspend fun getSoundOnOff(): Boolean = settingRepository.getSoundOnOff()

        suspend fun setAutoPlay(isAutoPlay: Boolean) {
            settingRepository.setAutoPlay(isAutoPlay)
        }

        suspend fun setLoopPlay(isLoopPlay: Boolean) {
            settingRepository.setLoopPlay(isLoopPlay)
        }

        suspend fun setPIPPlay(isPIPMode: Boolean) {
            settingRepository.setPIPPlay(isPIPMode)
        }

        suspend fun setSoundOff(isSound: Boolean) {
            settingRepository.setSoundOnOff(isSound)
        }

        suspend fun setWifiOnlyPlay(isWifiOnly: Boolean) {
            settingRepository.setWifiOnlyPlay(isWifiOnly)
        }

        fun sendGALog(
            event: String,
            route: String? = null,
            viewType: ViewType? = null,
        ) {
            gaLog.sendEvent(event, route, viewType)
        }

        companion object {
            const val TAG = "SettingViewModel"
        }
    }
