package com.sean.ratel.android.ui.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object PushChannelIds {
    const val APP_UPDATE = "app_update"
    const val VIDEO_UPLOAD = "video_upload"
    const val RECOMMEND = "recommend"
}

enum class PushChannelType(
    val channelId: String,
) {
    APP_UPDATE(PushChannelIds.APP_UPDATE),
    VIDEO_UPLOAD(PushChannelIds.VIDEO_UPLOAD),
    RECOMMEND(PushChannelIds.RECOMMEND),
}

data class PushChannelStatus(
    val channelId: String,
    val appNotificationsEnabled: Boolean,
    val channelExists: Boolean,
    val channelEnabled: Boolean,
    val importance: Int?,
) {
    val blocked: Boolean
        get() = !appNotificationsEnabled || !channelEnabled
}

object PushChannelHelper {
    fun isAppNotificationEnabled(context: Context): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun getChannelStatus(
        context: Context,
        channelId: String,
    ): PushChannelStatus {
        val appEnabled = isAppNotificationEnabled(context)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel: NotificationChannel? = notificationManager.getNotificationChannel(channelId)

        if (channel == null) {
            return PushChannelStatus(
                channelId = channelId,
                appNotificationsEnabled = appEnabled,
                channelExists = false,
                channelEnabled = false,
                importance = null,
            )
        }

        val channelEnabled = channel.importance != NotificationManager.IMPORTANCE_NONE

        return PushChannelStatus(
            channelId = channelId,
            appNotificationsEnabled = appEnabled,
            channelExists = true,
            channelEnabled = channelEnabled,
            importance = channel.importance,
        )
    }

    fun isChannelEnabled(
        context: Context,
        channelId: String,
    ): Boolean =
        getChannelStatus(context, channelId).channelEnabled &&
            getChannelStatus(context, channelId).appNotificationsEnabled

    fun isChannelBlocked(
        context: Context,
        channelId: String,
    ): Boolean = !isChannelEnabled(context, channelId)

    fun getAllChannelStatuses(context: Context): List<PushChannelStatus> =
        PushChannelType.entries.map { type ->
            getChannelStatus(context, type.channelId)
        }

    fun openAppNotificationSettings(context: Context) {
        val intent =
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }

    fun openChannelNotificationSettings(
        context: Context,
        channelId: String,
    ) {
        val intent =
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }
}
