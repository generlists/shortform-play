package com.sean.ratel.android.ui.home.setting

import androidx.annotation.DrawableRes
import com.sean.ratel.android.R

enum class SettingsItems(
    @DrawableRes val icon: Int?,
    val mainTitle: Int,
    val description: Int?,
) {
    SERVICE_TITLE_NOTICE(
        null,
        R.string.setting_notice,
        null,
    ),
    SERVICE_TITLE_QNA(
        null,
        R.string.setting_qna,
        null,
    ),
    SERVICE_TITLE_REGAL(
        null,
        R.string.setting_private_policy,
        null,
    ),
    SETTING_SHORTFORM_COUNTRY(
        null,
        0,
        R.string.setting_select_shortform_country_description,
    ),
    SERVICE_VIDEO_AUTO_PLAY(
        null,
        R.string.setting_play_auto_play,
        R.string.setting_play_auto_play_description,
    ),
    SERVICE_VIDEO_LOOP_PLAY(
        null,
        R.string.setting_play_clipping_play,
        R.string.setting_play_clipping_dscription,
    ),
    SERVICE_VIDEO_PIP_PLAY(
        null,
        R.string.setting_pip_play,
        R.string.setting_pip_play_discription,
    ),
    SERVICE_VIDEO_SOUND(
        null,
        R.string.setting_sound,
        R.string.setting_sound_description,
    ),
    SERVICE_VIDEO_WIFI_STATE(
        null,
        R.string.setting_play_wifi_play,
        R.string.setting_play_wifi_play_discription,
    ),
    SETTING_APP_MANAGER(
        null,
        R.string.setting_app_manager,
        null,
    ),
    SETTING_APP_OPEN_SOURCE(
        null,
        R.string.setting_app_openSource,
        null,
    ),
    SETTING_APP_VERSION(
        null,
        R.string.setting_app_version,
        null,
    ),
    SETTING_APP_RATE(
        null,
        R.string.setting_app_rate,
        null,
    ),
    SETTING_APP_PRESENT(
        null,
        R.string.setting_app_present,
        null,
    ),
}
