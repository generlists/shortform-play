package com.sean.ratel.android.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import coil.ImageLoader
import coil.request.ImageRequest
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.R
import com.sean.ratel.android.data.domain.model.push.AppPushType
import com.sean.ratel.android.data.local.pref.PushPreference
import com.sean.ratel.android.ui.push.PushChannelIds.APP_UPDATE
import com.sean.ratel.android.ui.push.PushChannelIds.RECOMMEND
import com.sean.ratel.android.ui.push.PushChannelIds.VIDEO_UPLOAD
import com.sean.ratel.android.utils.PhoneUtil.getAppVersionCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import so.smartlab.common.push.fcm.data.domain.PushType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationManager
    @Inject
    constructor(
        val pushPreference: PushPreference,
    ) {
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun show(
            context: Context,
            data: Map<String, String>,
        ) {
            val type = data["type"] ?: return

            when (type) {
                PushType.Update.name -> showUpdate(context, data)
                PushType.Upload.name -> showUpload(context, data)
                PushType.Recommend.name -> showRecommend(context, data)
            }
        }

        private fun showUpdate(
            context: Context,
            data: Map<String, String>,
        ) {
            val pendingIntent = createPendingIntent(context, data["id"], data["type"], data["linkUrl"])
            val appIconBitmap =
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.shortform_play_icon_main,
                )
            val forceUpdate = data["force"]

            val builder =
                NotificationCompat
                    .Builder(context, APP_UPDATE)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(if (forceUpdate.toBoolean()) "[업데이트 필수]" + "${data["title"]}" else "${data["title"]}")
                    .setContentText(data["body"])
                    .setLargeIcon(appIconBitmap)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

            notify(context, builder)
        }

        private fun showUpload(
            context: Context,
            data: Map<String, String>,
        ) {
            // Message(type=Upload, title=1658개의 영상이 업로드 되었어요., body=Shrimp vs Time, data={body=Shrimp vs Time, date=2026.03.18, type=Upload, count=, title=1658개의 영상이 업로드 되었어요., linkUrl=https://shortform-play.ai?vid=BepAjwAjZoA, videoId=BepAjwAjZoA, thumbUrl=https://i.ytimg.com/vi/BepAjwAjZoA/hqdefault.jpg, channelThumbUrl=https://yt3.ggpht.com/4UQ577L3bctWdMmwi-PjYIqXXFj_WGRIZNwMuL8-JN2xtfb56DRVv8ONu6VakDZHCqOISI6D=s88-c-k-c0x00ffffff-no-rj})
            val pendingIntent = createPendingIntent(context, data["id"], data["type"], data["linkUrl"])

            CoroutineScope(Dispatchers.IO).launch {
                val bigImage =
                    loadBitmapWithRightPadding(
                        context = context,
                        url = data["channelThumbUrl"] ?: "",
                    )
                val imageUrl = data["thumbUrl"]
                val bitmap =
                    loadBitmapWithRightPadding(
                        context = context,
                        url = imageUrl ?: "",
                    )
                withContext(Dispatchers.Main) {
                    val builder =
                        NotificationCompat
                            .Builder(context, VIDEO_UPLOAD)
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(data["title"])
                            .setContentText(data["body"])
                            .setLargeIcon(bigImage)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)

                    if (bitmap != null) {
                        builder.setStyle(
                            NotificationCompat
                                .BigPictureStyle()
                                .bigPicture(bitmap)
                                .bigLargeIcon(null as Bitmap?),
                        )

                        notify(context, builder)
                    } else {
                        notify(context, builder)
                    }
                }
            }
        }

        private fun createPendingIntent(
            context: Context,
            id: String?,
            type: String?,
            url: String?,
        ): PendingIntent {
            RLog.d("PushNotificationManager", "createPendingIntent $url")

            val intent =
                when (type) {
                    AppPushType.Recommend.name, AppPushType.Upload.name -> {
                        Intent(Intent.ACTION_VIEW, "$url&v=${getAppVersionCode(context)}".toUri()).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    }

                    AppPushType.Update.name -> {
                        Intent(Intent.ACTION_VIEW, url?.toUri())
                    }

                    else -> {
                        Intent(Intent.ACTION_VIEW, "shortformplay://home".toUri()).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    }
                }

            scope.launch {
                pushPreference.saveNewPush(true)
            }

            intent.putExtra("notification_id", id)
            intent.putExtra("notification_type", type)
            intent.putExtra("notification_click", true)

            return PendingIntent.getActivity(
                context,
                10000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun showRecommend(
            context: Context,
            data: Map<String, String>,
        ) {
//        {
//            "type":"Recommend",
//            "title":"[추천알림] BTS OFFICIAL LIGHT STICK VER.4",
//            "body":"Connect with BTS:\nhttps://ibighit.com/bts\nhttps://x.com/bts_bighit\nhttps://x.com/BTS_twt\nhttp://www.facebook.com/bangtan.official\nhttps://www.youtube.com/user/BANGTANTV\nhttp://instagram.com/BTS.bighitofficial\nhttps://www.tiktok.com/@bts_official_bighit\nhttps://weverse.onelink.me/qt3S/94808190\nhttps://www.weibo.com/BTSbighit",
//            "videoId":"BRYAWqGjmPo",
//            "linkUrl":"https://shortform-play.ai?vid=BRYAWqGjmPo",
//            "channelThumbUrl":"https://yt3.ggpht.com/AXxjT9r1AoLxyny3L5lquEVIP6Qa5gJnQUtf94De2QydHrse6OCkkLpHOsTiQ37_t7wQ22G3pA=s88-c-k-c0x00ffffff-no-rj",
//            "thumbUrl":"https://i.ytimg.com/vi/BRYAWqGjmPo/hqdefault.jpg"
//        }
            val pendingIntent =
                createPendingIntent(context, data["id"], data["type"], data["linkUrl"])

            CoroutineScope(Dispatchers.IO).launch {
                val bigImage =
                    loadBitmapWithRightPadding(
                        context = context,
                        url = data["channelThumbUrl"] ?: "",
                    )
                val imageUrl = data["thumbUrl"]
                val bitmap =
                    loadBitmapWithRightPadding(
                        context = context,
                        url = imageUrl ?: "",
                    )
                withContext(Dispatchers.Main) {
                    val builder =
                        NotificationCompat
                            .Builder(context, RECOMMEND)
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(data["title"])
                            .setContentText(data["body"])
                            .setLargeIcon(bigImage)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)

                    if (bitmap != null) {
                        builder.setStyle(
                            NotificationCompat
                                .BigPictureStyle()
                                .bigPicture(bitmap)
                                .bigLargeIcon(null as Bitmap?),
                        )
                        notify(context, builder)
                    } else {
                        notify(context, builder)
                    }
                }
            }
        }

        private fun notify(
            context: Context,
            builder: NotificationCompat.Builder,
        ) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.notify(System.currentTimeMillis().toInt(), builder.build())
        }

        fun createChannels(context: Context) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val update =
                NotificationChannel(
                    APP_UPDATE,
                    context.getString(R.string.notification_update),
                    NotificationManager.IMPORTANCE_HIGH,
                )

            val event =
                NotificationChannel(
                    RECOMMEND,
                    context.getString(R.string.notification_recommend),
                    NotificationManager.IMPORTANCE_DEFAULT,
                )

            val upload =
                NotificationChannel(
                    VIDEO_UPLOAD,
                    context.getString(R.string.notification_upload),
                    NotificationManager.IMPORTANCE_DEFAULT,
                )

            manager.createNotificationChannel(update)
            manager.createNotificationChannel(event)
            manager.createNotificationChannel(upload)
        }

        suspend fun loadBitmapWithRightPadding(
            context: Context,
            url: String,
        ): Bitmap? {
            val loader = ImageLoader(context)

            val request =
                ImageRequest
                    .Builder(context)
                    .data(url)
                    .allowHardware(false) // Bitmap 필요
                    .build()

            val result = loader.execute(request)

            return (result.drawable as? BitmapDrawable)?.bitmap
        }
    }
