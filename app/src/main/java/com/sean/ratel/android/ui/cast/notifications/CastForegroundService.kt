package com.sean.ratel.android.ui.cast.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresPermission
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext.Companion.TAG
import com.sean.ratel.android.MainActivity
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.cast.CastSessionState
import com.sean.ratel.android.ui.cast.YouTubePlayersManager
import com.sean.ratel.android.ui.cast.notifications.CastNotificationAction.ACTION_STOP_CAST
import com.sean.ratel.android.ui.cast.notifications.CastNotificationAction.NOTIFICATION_ID
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.demo.ui.cast.notifications.CastNotificationState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import so.smartlab.common.utils.log.RLog
import javax.inject.Inject

@AndroidEntryPoint
class CastForegroundService : Service() {
    @Inject
    lateinit var castNotificationManager: CastNotificationManager

    @Inject
    lateinit var youTubePlayersManager: YouTubePlayersManager

    private lateinit var mediaSession: MediaSessionCompat

    private val serviceJob = SupervisorJob()

    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var playbackSpeed = 1f
    private var playbackCurrentTime = 0L
    private var currentState = PlaybackStateCompat.STATE_PAUSED
    private var actions: Long = PlaybackStateCompat.ACTION_PLAY

    override fun onCreate() {
        super.onCreate()
        RLog.d("CastForegroundService", "onCreate")

        createChannel()
        setupMediaSession()
        youTubePlayersManager.startCollect(serviceScope)
        loadCastPlay()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        RLog.d("CastForegroundService", "onStartCommand , action : ${intent?.action}")
        if (intent?.action == ACTION_STOP_CAST) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            updateInitPlayBack()
            stopSelf()

            return START_NOT_STICKY
        }

        val notification =
            buildNotification(
                CastNotificationState(isVisible = true),
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        observeState()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        RLog.d("CastForegroundService", "onDestroy")
        castNotificationManager.release()
        mediaSession.release()
        playbackCurrentTime = 0L
        serviceScope.cancel()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun observeState() {
        castNotificationManager.state
            .onEach { state ->
                RLog.d("CastForegroundService", "observeState : $state")
                when {
                    state.isVisible -> updateNotification(state)
                    !state.isVisible && state.isPlaying == null && !state.isFinish -> updateNotification(state)
                    else -> hideNotification()
                }
            }.launchIn(serviceScope)

        youTubePlayersManager.castDuration
            .onEach { duration ->
                duration?.let {
                    if (it > 0) updateDuration(it.toLong())
                }
            }.launchIn(serviceScope)

        youTubePlayersManager.castCurrentTime
            .onEach { currentTime ->
                currentTime?.let {
                    playbackCurrentTime = it.toLong()
                    if (it > 0) updatePlaybackState(it.toLong())
                }
            }.launchIn(serviceScope)

        youTubePlayersManager.currentPlaySpeed
            .onEach { speed ->
                speed.let { speedRate ->
                    speedRate?.let {
                        playbackSpeed = speedRate.rate
                        updatePlaybackSpeed(playbackSpeed)
                    }
                }
            }.launchIn(serviceScope)

        youTubePlayersManager.castPlayState
            .onEach { state ->
                when (state) {
                    YouTubeStreamPlaybackState.Playing -> {
                        currentState =
                            PlaybackStateCompat.STATE_PLAYING
                    }

                    YouTubeStreamPlaybackState.Paused -> {
                        currentState =
                            PlaybackStateCompat.STATE_PAUSED
                    }

                    else -> {
                        Unit
                    }
                }
            }.launchIn(serviceScope)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateNotification(state: CastNotificationState) {
        setControlAction(state)
        val notification = buildNotification(state)
        NotificationManagerCompat
            .from(this)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun hideNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(state: CastNotificationState): Notification {
        val openIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    action = CastNotificationAction.ACTION_OPEN_CAST_SCREEN
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(this, CastNotificationAction.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_cast_connected)
                .setLargeIcon(
                    state.thumbnail ?: AppCompatResources
                        .getDrawable(
                            this,
                            R.drawable.shortform_play_icon_main,
                        )?.toBitmap(),
                ).setContentTitle(state.title)
                .setContentText(
                    String.format(
                        applicationContext.getString(R.string.cast_running_cast_device),
                        state.deviceName,
                    ),
                ).setSubText(state.channelName)
                .setContentIntent(openIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)

        notification
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                androidx.media.app.NotificationCompat
                    .MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2),
            )

        // 액션 1: 이전
        state.isPlaying?.let {
            notification.addAction(
                buildAction(
                    R.drawable.ic_skip_previous,
                    applicationContext.getString(R.string.cast_play_prev),
                    CastNotificationAction.ACTION_PREVIOUS,
                ),
            )
        }

        // 재생/일시정지
        state.isPlaying?.let { isPlaying ->
            notification.addAction(
                buildAction(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                    if (isPlaying) {
                        applicationContext.getString(
                            R.string.cast_play_pause,
                        )
                    } else {
                        applicationContext.getString(R.string.cast_play)
                    },
                    if (isPlaying) CastNotificationAction.ACTION_PAUSE else CastNotificationAction.ACTION_PLAY,
                ),
            )
        }

        // 다음
        state.isPlaying?.let {
            notification.addAction(
                buildAction(
                    R.drawable.ic_skip_next,
                    applicationContext.getString(R.string.cast_next),
                    CastNotificationAction.ACTION_NEXT,
                ),
            )
        }

        return notification.build()
    }

    private fun buildAction(
        iconRes: Int,
        title: String,
        action: String,
    ): NotificationCompat.Action {
        // RLog.d("CastForegroundService", "buildAction :  $action")
        val intent =
            Intent().apply {
                this.action = action
                `package` = packageName
            }

        val pi =
            PendingIntent.getBroadcast(
                this,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        // RLog.d("CastForegroundService", "pi=$pi action=$action")

        return NotificationCompat.Action.Builder(iconRes, title, pi).build()
    }

    private fun createChannel() {
        val channel =
            NotificationChannel(
                CastNotificationAction.CHANNEL_ID,
                "Cast 재생 컨트롤",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "TV로 캐스팅 중일 때 표시"
                setShowBadge(false)
            }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun setControlAction(state: CastNotificationState) {
        actions =

            PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or
            (if (!state.isFirst) PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS else 0L) or
            (if (!state.isLast) PlaybackStateCompat.ACTION_SKIP_TO_NEXT else 0L)
    }

    private fun setupMediaSession() {
        RLog.d("CastForegroundService", "setupMediaSession")
        mediaSession =
            MediaSessionCompat(this, "CastMediaSession").apply {
                isActive = true

                // 초기 상태
                setPlaybackState(
                    PlaybackStateCompat
                        .Builder()
                        .setActions(0)
                        .setState(
                            PlaybackStateCompat.STATE_NONE,
                            0L,
                            1f,
                        ).build(),
                )

                // metadata (duration 필수)
                setMetadata(
                    MediaMetadataCompat
                        .Builder()
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0L) // 처음엔 0
                        .build(),
                )

                setCallback(
                    object : MediaSessionCompat.Callback() {
                        override fun onPlay() {
                            RLog.d("CastForegroundService", "noti onPlay")
                            sync(true)
                        }

                        override fun onPause() {
                            RLog.d("CastForegroundService", "noti onPause")
                            sync(false)
                        }

                        override fun onSkipToNext() {
                            RLog.d("CastForegroundService", "noti onSkipToNext")
                            playbackCurrentTime = 0L
                            youTubePlayersManager.castNextPlay()
                        }

                        override fun onSkipToPrevious() {
                            RLog.d("CastForegroundService", "noti onSkipToPrevious")
                            playbackCurrentTime = 0L
                            youTubePlayersManager.castPrevPlay()
                        }
                    },
                )
            }
    }

    private fun updatePlaybackState(position: Long) {
        // RLog.d("CastForegroundService","updatePlaybackState")
        mediaSession.setPlaybackState(
            PlaybackStateCompat
                .Builder()
                .setActions(actions)
                .setState(
                    currentState,
                    position,
                    playbackCurrentTime.toFloat(),
                ).build(),
        )
    }

    private fun updatePlaybackSpeed(speed: Float) {
        // RLog.d("CastForegroundService","updatePlaybackSpeed")
        mediaSession.setPlaybackState(
            PlaybackStateCompat
                .Builder()
                .setActions(actions)
                .setState(
                    currentState,
                    playbackCurrentTime,
                    speed,
                ).build(),
        )
    }

    private fun sync(isPlaying: Boolean) {
        RLog.d("CastForegroundService", "sync $isPlaying")
        val position = 0L // UI용이면 일단 0으로 둬도 됨
        val speed = 1f

        mediaSession.setPlaybackState(
            PlaybackStateCompat
                .Builder()
                .setActions(actions)
                .setState(
                    currentState,
                    position,
                    speed,
                ).build(),
        )

        youTubePlayersManager.castPlayPause(isPlaying)
    }

    fun updateDuration(duration: Long) {
        // RLog.d("CastForegroundService","updateDuration $duration")
        mediaSession.setMetadata(
            MediaMetadataCompat
                .Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build(),
        )
    }

    fun updateInitPlayBack() {
        // RLog.d("CastForegroundService","updateInitPlayBack")
        mediaSession.setPlaybackState(
            PlaybackStateCompat
                .Builder()
                .setActions(0)
                .setState(
                    currentState,
                    0,
                    0f,
                ).build(),
        )
    }

    fun loadCastPlay() {
        serviceScope.launch {
            combine(
                youTubePlayersManager.isCastPlayerReady,
                youTubePlayersManager.castEventManager.castSession,
                youTubePlayersManager.currentVideo,
            ) { ready, session, currentVideo ->
                Triple(ready, session, currentVideo)
                // CastPlayState(ready, session, playEvent, currentVideo)
            }.onEach { load ->
                RLog.e(TAG, "onEach ready ${load.first} , session : ${load.second} , videoId : ${load.third?.shortsVideoModel?.videoId}")
            }.collect { (ready, session, currentVideo) ->
                RLog.d(
                    TAG,
                    "Service ready : $ready session : $session , currentVideo : ${currentVideo?.shortsVideoModel?.videoId}",
                )
                if (
                    (ready is YouTubeStreamPlaybackState.IFrameReady || ready is YouTubeStreamPlaybackState.Ready) &&
                    session is CastSessionState.SessionStart &&
                    currentVideo != null
                ) {
                    RLog.d(TAG, "$ready , $session ,${currentVideo.shortsVideoModel?.videoId}")
                    youTubePlayersManager.loadCastVideo(
                        currentVideo.shortsVideoModel?.videoId ?: "",
                        playbackCurrentTime.toFloat(),
                    )
                }
            }
        }
    }
}
