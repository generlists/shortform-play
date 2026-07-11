package com.sean.ratel.player.demo.ui.cast.notifications

import android.graphics.Bitmap

data class CastNotificationState(
    val isVisible: Boolean = false,
    val isFinish: Boolean = false,
    val title: String = "",
    val channelName: String = "",
    val deviceName: String = "",
    val thumbnail: Bitmap? = null,
    val thumbUrl: String? = null,
    val isPlaying: Boolean? = null,
    val isFirst: Boolean = true,
    val isLast: Boolean = false,
)
