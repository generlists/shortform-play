package com.sean.ratel.android.data.common

import android.content.Context
import androidx.compose.ui.unit.dp
import com.sean.ratel.android.BuildConfig
import com.sean.ratel.android.R

object STRINGS {
    const val MY_EMAIL_ACCOUNT = BuildConfig.MY_EMAIL_ACCOUNT
    const val NOTICES_URL = BuildConfig.NOTICES_URL

    const val NOTICES_URL_EN = BuildConfig.NOTICES_URL_EN

    const val REGAL_URL = BuildConfig.REGAL_URL

    const val REGAL_URL_EN = BuildConfig.REGAL_URL_EN

    const val FEEDBACK_TITLE = "[숏폼플레이 문의]"

    const val MAX_ADAPTIVE_BANNER_SIZE = 100

    val REMAIN_AD_MARGIN = 24.dp

    // ad
    const val TEST_DEVICE_HASHED_ID = "ABCDEF012345"

    @Suppress("ktlint:standard:function-naming")
    fun URL_GOOGLE_PLAY_APP(packageName: String): String = "market://details?id=$packageName"

    @Suppress("ktlint:standard:function-naming")
    fun URLUPDATE_GOOGLE_PLAY_WEB(appTitle: String): String = "https://play.google.com/store/search?q=$appTitle&c=apps"

    const val URL_MY_PACKAGE_NAME: String = "com.sean.ratel.android"

    const val SERVICE_START_DATE = "20241029"
    const val SERVICE_RENEWAL_START_DATE = "20241207"

    @Suppress("ktlint:standard:function-naming")
    fun YOUTUBE_APP_BY_VIDEO_ID(videoId: String): String = "https://shortform-play.ai/share?vid=$videoId"

    @Suppress("ktlint:standard:function-naming")
    fun YOUTUBE_APP_BY_CHANNEL_ID(channelId: String): String = "https://m.youtube.com/channel/$channelId"

    fun getShortFormCountry(context: Context): List<Pair<String, String>> =
        listOf(
            Pair(context.getString(R.string.select_country_korea), "KR"),
            Pair(context.getString(R.string.select_country_usa), "US"),
            Pair(context.getString(R.string.select_country_japan), "JP"),
            Pair(context.getString(R.string.select_country_taiwan), "TW"),
            Pair(context.getString(R.string.select_country_indonesia), "ID"),
            Pair(context.getString(R.string.select_country_tailand), "TH"),
            Pair(context.getString(R.string.select_country_canada_en), "CA"),
        )
}
