package com.sean.ratel.android.ui.cast

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.repository.SettingRepository
import com.sean.ratel.android.ui.cast.notifications.CastForegroundService
import com.sean.ratel.android.ui.cast.notifications.CastNotificationAction.ACTION_STOP_CAST
import com.sean.ratel.android.ui.end.YouTubeEndFragment
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.youtube.YouTubeCastPlayerAdapterImpl
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.youtube.YouTubeCastPlayerImpl
import com.sean.ratel.player.core.data.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlayerError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import so.smartlab.common.utils.log.RLog
import java.lang.ref.WeakReference
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubePlayersManager
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val castEventManager: CastEventManager,
        private val settingRepository: SettingRepository,
        private val gaLog: GALog,
    ) : ChromecastConnectionListener {
        private var youTubeStreamPlayer: YouTubeStreamPlayer? = null
        private var youTubeCastPlayer: YouTubeStreamPlayer? = null
        private var saveCurrentTime = 0f

        private var activityRef: WeakReference<ComponentActivity>? = null
        private var chromecastYoutubePlayerContextRef: ChromecastYouTubePlayerContext? = null
        private var firstCaption = false
        private var closeCast = false
        private var viewPager: ViewPager2? = null
        private var lastCurrentTime: Float = 0f

        private val _castConnectLoading = MutableStateFlow<Boolean>(false)
        val castConnectLoading = _castConnectLoading.asStateFlow()

        private val _castDuration = MutableStateFlow<Float?>(null)
        val castDuration = _castDuration.asStateFlow()

        private val _castCurrentTime = MutableStateFlow<Float?>(null)
        val castCurrentTime = _castCurrentTime.asStateFlow()

        private val _captionOnOff = MutableStateFlow<Boolean>(true)
        val captionOnOff = _captionOnOff.asStateFlow()

        private val _captionAvailable = MutableStateFlow<Boolean?>(null)
        val captionAvailable = _captionAvailable.asStateFlow()

        private val _castPlayerInit =
            MutableSharedFlow<Boolean>(
                extraBufferCapacity = 1,
            )

        val castPlayerInit = _castPlayerInit.asSharedFlow()

        private val _videoId = MutableStateFlow<String>("")
        val videoId = _videoId.asStateFlow()

        private val currentIndex = MutableStateFlow<Int>(0)

        private val _mute = MutableStateFlow<Boolean>(false)
        val mute = _mute.asStateFlow()

        private val _castPlayState =
            MutableStateFlow<YouTubeStreamPlaybackState>(YouTubeStreamPlaybackState.UnKnown)
        val castPlayState = _castPlayState.asStateFlow()

        private val _localPlayState =
            MutableStateFlow<YouTubeStreamPlaybackState>(YouTubeStreamPlaybackState.UnKnown)
        val localPlayState = _localPlayState.asStateFlow()

        private val _videoList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val videoList = _videoList.asStateFlow()

        private val _currentVideo = MutableStateFlow<MainShortsModel?>(null)
        val currentVideo = _currentVideo.asStateFlow()

        private val _currentPlaySpeed =
            MutableStateFlow<YouTubeStreamPlaybackRate?>(null)
        val currentPlaySpeed = _currentPlaySpeed.asStateFlow()

        private val _isFirst = MutableStateFlow<Boolean>(true)
        val isFirst = _isFirst.asStateFlow()

        private val _isLast = MutableStateFlow<Boolean>(true)
        val isLast = _isLast.asStateFlow()

        private val currentRoute = MutableStateFlow<String>(Destination.Home.Main.route)

        private val _retainSoundCaption = MutableStateFlow<Boolean>(false)
        val retainSoundCaption = _retainSoundCaption.asStateFlow()

        private val _isCastPlayerReady =
            MutableStateFlow<YouTubeStreamPlaybackState>(YouTubeStreamPlaybackState.UnKnown)

        val isCastPlayerReady = _isCastPlayerReady.asStateFlow()

        fun setVideoList(
            viewPager2: ViewPager2?,
            videoList: List<MainShortsModel>,
        ) {
            _videoList.value = videoList
            viewPager = viewPager2
        }

        fun setActivity(activity: FragmentActivity) {
            activityRef = WeakReference(activity)
        }

        // 영상이 루핑되어 처리
        fun setRetainSound(isRetain: Boolean) {
            _retainSoundCaption.value = isRetain
        }

        fun initChromeCast() {
            initYoutubeChromecast()
        }

        private fun initYoutubeChromecast() {
            RLog.d(
                TAG,
                "[initYoutubeChromecast] currentSession : ${CastContext.getSharedInstance(context).sessionManager}",
            )

            ChromecastYouTubePlayerContext(CastContext.getSharedInstance(context).sessionManager, this)
        }

        fun setCurrentPosition(selection: Int) {
            RLog.d(TAG, "[setCurrentPosition] selection :$selection")
            if (_videoList.value.isEmpty()) return

            currentIndex.value = selection
            _currentVideo.value = _videoList.value.get(currentIndex.value)
        }

        fun setCurrentRoute(route: String) {
            // RLog.d(TAG, "[setCurrentRoute] route :$route")
            currentRoute.value = route
        }

        override fun onChromecastConnecting() {
            RLog.d(TAG, "[onChromecastConnecting] onChromecastConnecting...")
            _castConnectLoading.value = true
        }

        override fun onChromecastConnected(
            castDevice: String?,
            chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext,
        ) {
            RLog.d(
                TAG,
                "[onChromecastConnected] castDevice $castDevice ,  chromecastYouTubePlayerContext : $chromecastYouTubePlayerContext",
            )

            val activity = activityRef?.get()
            if (activity != null) {
                chromecastYoutubePlayerContextRef = chromecastYouTubePlayerContext

                initializeCastPlayer(activity)
                onPlayerState(activity)
                _castConnectLoading.value = false

                if (ProcessLifecycleOwner
                        .get()
                        .lifecycle.currentState
                        .isAtLeast(Lifecycle.State.STARTED)
                ) {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(activity, CastForegroundService::class.java),
                    )
                }

                if (currentRoute.value == Destination.YouTube.route) {
                    _currentVideo.value = _videoList.value[currentIndex.value]
                }
                closeCast = false
                castEventManager.setCastSessionState(CastSessionState.SessionStart(castDevice))
                castEventManager.setPlayerState(PlayerState.CAST)
            }
        }

        override fun onChromecastDisconnected() {
            RLog.d(TAG, "[onChromecastDisconnected] onChromecastDisconnected")

            _castConnectLoading.value = false
            chromecastYoutubePlayerContextRef?.endCurrentSession()

            val activity = activityRef?.get()
            val intent =
                Intent(activity, CastForegroundService::class.java).apply {
                    action = ACTION_STOP_CAST
                }

            activity?.stopService(intent)

            castEventManager.setCastSessionState(CastSessionState.SessionEnd)
            _currentVideo.value = null
            _castDuration.value = null
            _castCurrentTime.value = null
            _retainSoundCaption.value = false
            _isCastPlayerReady.value = YouTubeStreamPlaybackState.UnKnown
            saveCurrentTime = _castCurrentTime.value ?: 0f
            firstCaption = false
            closeCast = true
            castEventManager.setPlayerState(PlayerState.LOCAL)
            castEventManager.sendEvent(CastEvent.Idle)
            lastCurrentTime = 0f
        }

        fun initializeCastPlayer(activity: ComponentActivity) {
            RLog.d(TAG, "[initializeCastPlayer] : youTubeCastPlayer : $youTubeCastPlayer")
            if (youTubeCastPlayer != null) return

            chromecastYoutubePlayerContextRef?.let {
                youTubeCastPlayer =
                    YouTubeCastPlayerImpl(
                        lifecycle = activity.lifecycle,
                        autoPlay = true,
                        youtubeCastPlayerAdapter = YouTubeCastPlayerAdapterImpl(it),
                    )

                val videoId = _currentVideo.value?.shortsVideoModel?.videoId

                youTubeCastPlayer?.initPlayer(videoId = videoId)
                youTubeCastPlayer?.setMute(mute = true)
                _castPlayerInit.tryEmit(true)

                RLog.d(
                    TAG,
                    "[initializeCastPlayer]  플레이어 생성:  videoId : $videoId , youTubeCastPlayer : $youTubeCastPlayer",
                )
            }
        }

        fun setLocalPlayer(localPlayer: YouTubeStreamPlayer?) {
            youTubeStreamPlayer = localPlayer
        }

        fun onLocalPlayState(playState: YouTubeStreamPlaybackState) {
            _localPlayState.value = playState
        }

        fun onPlayerState(activity: ComponentActivity) {
            activity.lifecycleScope.launch {
                activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    val playerState = castEventManager.playerState
                    val localPlayerState = _localPlayState
                    val castPlayState = _castPlayState

                    combine(playerState, localPlayerState, castPlayState) { p, l, c ->
                        Triple(p, l, c)
                    }.collect { (playerState, localPlayerState, castPlayState) ->

                        val currentTime = saveCurrentTime
                        RLog.v(
                            TAG,
                            "closeCast : $closeCast , castPlayerState : $playerState , " +
                                "localPlayerState : $localPlayerState ," +
                                " castPlayState : $castPlayState currentTime :$currentTime",
                        )

                        if (playerState == PlayerState.LOCAL && closeCast) {
                            val currentIndex = currentIndex.value
                            activity.let {
                                val f = getEndFragment(activity)
                                viewPager?.setCurrentItem(currentIndex, true)
                                f?.seekTo(currentTime ?: 0f)
                                f?.play()
                                //
                            }
                        } else if (playerState == PlayerState.CAST && localPlayerState == YouTubeStreamPlaybackState.Playing) {
                            val activity = activityRef?.get()
                            activity?.let {
                                val f = getEndFragment(activity)
                                f?.pause()
                            }
                        }
                    }
                }
            }
        }

        private fun getEndFragment(context: Context): YouTubeEndFragment? {
            val fragmentManager =
                (context as FragmentActivity).supportFragmentManager
            val itemId = viewPager?.adapter?.getItemId(viewPager?.currentItem ?: 0)
            val tag = "f$itemId"
            return fragmentManager.findFragmentByTag(tag) as? YouTubeEndFragment
        }

        fun setMute(isMute: Boolean?) {
            RLog.d(TAG, "[setMute] youTubeCastPlayer : $youTubeCastPlayer , isMute : $isMute")
            // cast
            isMute?.let {
                youTubeCastPlayer?.setMute(!isMute)
                _mute.value = !isMute
            }
        }

        fun castPlayPause(isPlaying: Boolean) {
            RLog.w(TAG, "[castPlayPause] : isPlaying $isPlaying")
            if (!isPlaying) {
                youTubeCastPlayer?.pause()
                castEventManager.sendEvent(CastEvent.Pause)
            } else {
                youTubeCastPlayer?.start()
                castEventManager.sendEvent(CastEvent.Play)
            }
        }

        fun castPrevPlay() {
            currentIndex.value -= 1
            _currentVideo.value = _videoList.value[currentIndex.value]

            RLog.d(
                TAG,
                "[castPrevPlay] prev size : ${_videoList.value.size} , currentIndex : ${currentIndex.value}",
            )
            castEventManager.sendEvent(CastEvent.Prev)
            firstCaption = false
        }

        fun castNextPlay() {
            currentIndex.value += 1
            _currentVideo.value = _videoList.value[currentIndex.value]
            RLog.d(
                TAG,
                "[castNextPlay] next size : ${_videoList.value.size} , currentIndex : ${currentIndex.value}",
            )
            castEventManager.sendEvent(CastEvent.Next)
            firstCaption = false
        }

        fun loadCastVideo(
            videoId: String,
            startTime: Float,
        ) {
            val event = castEventManager.playEvents.value
            when (event) {
//                is CastEvent.Idle, CastEvent.Play -> {
//
//                }

                is CastEvent.Prev -> {
                    val prevVideoId =
                        _currentVideo.value
                            ?.shortsVideoModel
                            ?.videoId ?: ""
                    youTubeCastPlayer?.loadOrCueVideo(prevVideoId, startTime)
                }

                is CastEvent.Next -> {
                    RLog.d(
                        TAG,
                        "[loadCastVideo] Event : ${castEventManager.playEvents.value}, " +
                            "_currentVideo : ${_currentVideo.value?.shortsVideoModel?.videoId} , " +
                            "currentIndex : ${currentIndex.value} , size : ${_videoList.value.size}",
                    )

                    val nextVideoId =
                        _currentVideo.value
                            ?.shortsVideoModel
                            ?.videoId ?: ""

                    youTubeCastPlayer?.loadOrCueVideo(nextVideoId, startTime)
                }

                else -> {
                    RLog.d(
                        TAG,
                        "[loadCastVideo] Event : ${castEventManager.playEvents.value}, " +
                            "_currentVideo : ${_currentVideo.value?.shortsVideoModel?.videoId} ," +
                            " currentIndex : ${currentIndex.value} , size : ${_videoList.value.size}",
                    )
                    youTubeCastPlayer?.loadOrCueVideo(videoId, startTime)
                }
            }
        }

        fun setCaptionOnOff(captionOnOff: Boolean) {
            RLog.d(TAG, "[setCaptionOnOff] set captionOnOff : $captionOnOff")

            if (captionOnOff) {
                youTubeCastPlayer?.enableCaptions(Locale.getDefault().language)
            } else {
                youTubeCastPlayer?.disableCaptions()
            }

            _captionOnOff.value = captionOnOff
        }

        fun onPlayError(
            context: Context,
            scope: CoroutineScope,
        ) {
            scope.launch {
                youTubeStreamPlayer?.playbackError?.collect { state ->
                    if (state != YouTubeStreamPlayerError.UNKNOWN) {
                        Toast.makeText(context, "$state", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        fun startCollect(serviceScope: CoroutineScope) {
            serviceScope.launch {
                youTubeCastPlayer?.currentTime?.collect { currentTime ->
                    // RLog.d("SSSSSSSSSSSLLLLLLLLL", "currentTime : $currentTime")
                    if (currentTime > 0) _castCurrentTime.value = currentTime
                }
            }
            serviceScope.launch {
                youTubeCastPlayer?.duration?.collect { duration ->
                    if (duration > 0f) _castDuration.value = duration
                }
            }

            serviceScope.launch {
                youTubeCastPlayer?.videoSpeedChange?.collect { rate ->
                    rate?.let {
                        _currentPlaySpeed.value = rate
                        RLog.d(TAG, "[videoSpeedChange] change rate :: $rate")
                    }
                }
            }

            serviceScope.launch {
                youTubeCastPlayer?.let { castPlayer ->

                    combine(
                        castPlayer.playbackState,
                        castPlayer.currentTime,
                    ) { playbackState, currentTime ->

                        Pair(playbackState, currentTime)
                    }.collect { (playbackState, currentTime) ->
                        var isPlaying = currentTime - lastCurrentTime > 0 && currentTime > 1f

                        if (currentTime > 0f && lastCurrentTime == 0f) {
                            isPlaying = false
                        }

                        // RLog.e("LLLLLLL", "currentTime : $currentTime")
                        when (playbackState) {
                            is YouTubeStreamPlaybackState.IFrameReady -> {
                                RLog.d(
                                    TAG,
                                    "[Cast playbackState] IFrameReady",
                                )
                                _isCastPlayerReady.value =
                                    YouTubeStreamPlaybackState.IFrameReady
                            }

                            is YouTubeStreamPlaybackState.Ready -> {
                                RLog.d(
                                    TAG,
                                    "[Cast playbackState] Ready",
                                )
                                _isCastPlayerReady.value = YouTubeStreamPlaybackState.Ready
                            }

                            is YouTubeStreamPlaybackState.Prepared,
                            -> {
                                delay(500)
                                youTubeCastPlayer?.start()
                            }

                            YouTubeStreamPlaybackState.UnStarted -> {
                                delay(500)
                                youTubeCastPlayer?.start()
                            }

                            YouTubeStreamPlaybackState.Playing -> {}

                            YouTubeStreamPlaybackState.Paused -> {
                                castEventManager.sendEvent(CastEvent.Pause)
                            }

                            YouTubeStreamPlaybackState.Ended -> {
                                RLog.d(TAG, "[Cast playbackState] 캐스트 영상 종료 및 루프")
                                castPlayer.start()
                            }

                            else -> {}
                        }

                        if (isPlaying) {
                            if (playbackState == YouTubeStreamPlaybackState.Playing) {
                                _castPlayState.value = YouTubeStreamPlaybackState.Playing
                                castEventManager.sendEvent(CastEvent.Play)
                                closeCast = false
                            } else if (playbackState == YouTubeStreamPlaybackState.Paused) {
                                _castPlayState.value = YouTubeStreamPlaybackState.Paused
                                castEventManager.sendEvent(CastEvent.Pause)
                            }
                        } else {
                            if (playbackState != YouTubeStreamPlaybackState.Playing && playbackState != YouTubeStreamPlaybackState.Paused) {
                                _castPlayState.value = playbackState
                            }
                        }

                        RLog.v(
                            TAG,
                            " castPlayState : ${_castPlayState.value} ,  " +
                                "isPlaying : $isPlaying ,currentTime : $currentTime " +
                                "lastCurrentTime : $lastCurrentTime ," +
                                " origin : $playbackState",
                        )
                        lastCurrentTime = currentTime
                    }
                }
            }

            serviceScope.launch {
                youTubeCastPlayer?.let { player ->
                    combine(
                        player.captionAvailable,
                        player.playbackState,
                        player.currentTime,
                    ) { available, state, currentTime ->
                        if (state == YouTubeStreamPlaybackState.Playing && currentTime > 3f) {
                            available
                        } else {
                            null
                        }
                    }.collect { available ->
                        if (!firstCaption && available != null) {
                            RLog.d(TAG, "[Caption] available : $available")
                            _captionAvailable.value = available
                            _captionOnOff.value = available
                            firstCaption = true
                        }
                    }
                }
            }
        }

        fun isFirst(): Boolean {
            _isFirst.value = currentIndex.value == 0
            return currentIndex.value == 0
        }

        fun isLast(): Boolean {
            _isLast.value = currentIndex.value == ((_videoList.value.size) - 1)
            return currentIndex.value == ((_videoList.value.size) - 1)
        }

        fun speedUp() {
            var initRate = _currentPlaySpeed.value

            if (initRate == null) {
                initRate = YouTubeStreamPlaybackRate.RATE_1
            }
            val localRateIndex = YouTubeStreamPlaybackRate.entries.indexOf(initRate)
            if (localRateIndex < YouTubeStreamPlaybackRate.entries.lastIndex) {
                youTubeStreamPlayer?.setPlaybackRate(YouTubeStreamPlaybackRate.entries[localRateIndex + 1])
            }

            val castIndex = YouTubeStreamPlaybackRate.entries.indexOf(initRate)
            if (castIndex < YouTubeStreamPlaybackRate.entries.lastIndex) {
                youTubeCastPlayer?.setPlaybackRate(YouTubeStreamPlaybackRate.entries[castIndex + 1])
            }
            RLog.d(
                TAG,
                "[speedUp] _currentPlaySpeed : ${_currentPlaySpeed.value} castIndex :: $castIndex",
            )
        }

        fun speedDown() {
            var initRate = _currentPlaySpeed.value

            if (initRate == null) {
                initRate = YouTubeStreamPlaybackRate.RATE_1
            }
            val localRateIndex = YouTubeStreamPlaybackRate.entries.indexOf(initRate)

            if (localRateIndex > 0) {
                youTubeStreamPlayer?.setPlaybackRate(YouTubeStreamPlaybackRate.entries[localRateIndex - 1])
            }
            val remoteRateIndex = YouTubeStreamPlaybackRate.entries.indexOf(initRate)
            if (localRateIndex > 0) {
                youTubeCastPlayer?.setPlaybackRate(YouTubeStreamPlaybackRate.entries[remoteRateIndex - 1])
            }
        }

        fun release() {
            chromecastYoutubePlayerContextRef?.endCurrentSession()
        }

        fun getSoundOff() = settingRepository.soundOnOffFlow

        fun getCaptionEnabled() = settingRepository.captionEnabledFlow

        fun sendGALog(
            screenName: String,
            eventName: String,
            actionName: String,
            parameter: Map<String, String>,
        ) {
            gaLog.sendEvent(
                screenName,
                eventName,
                actionName,
                parameter,
            )
        }

        companion object {
            private val TAG = "CHROMECAST DEV"
        }
    }
