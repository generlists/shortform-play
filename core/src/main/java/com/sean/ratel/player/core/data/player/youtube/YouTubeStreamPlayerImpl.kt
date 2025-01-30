package com.sean.ratel.player.core.data.player.youtube

import android.annotation.SuppressLint
import android.util.Size
import android.view.View
import androidx.lifecycle.Lifecycle
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.sean.ratel.player.core.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlayerError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class YouTubeStreamPlayerImpl(
    private val lifecycle: Lifecycle,
    private val youtubeStreamPlayerAdapter: YouTubeStreamPlayerAdapter,
    private val iFramePlayerOptions: IFramePlayerOptions,
    private val youtubeStreamPlayerTracker: YouTubePlayerTracker,
) : YouTubePlayerListener,
    YouTubeStreamPlayer {
    private var youTubeStreamPlayer: YouTubePlayer? = null
    private var initialPlayer: Boolean = false

    private val _playbackState =
        MutableStateFlow<YouTubeStreamPlaybackState>(
            YouTubeStreamPlaybackState.UnKnown,
        )
    override val playbackState: StateFlow<YouTubeStreamPlaybackState> = _playbackState.asStateFlow()

    private val _playbackError =
        MutableStateFlow<YouTubeStreamPlayerError>(
            YouTubeStreamPlayerError.UNKNOWN,
        )
    override val playbackError: StateFlow<YouTubeStreamPlayerError> = _playbackError.asStateFlow()

    private val _isMute = MutableStateFlow(true)

    private val _duration = MutableStateFlow(0f)
    override val duration: StateFlow<Float> = _duration.asStateFlow()

    private val _currentTime = MutableStateFlow(0f)
    override val currentTime: StateFlow<Float> = _currentTime.asStateFlow()

    override fun getYouTubePlayerView(): View = youtubeStreamPlayerAdapter.getYouTubePlayerView()

    override fun initPlayer(networkHandle: Boolean?) {
        if (initialPlayer) return

        youtubeStreamPlayerAdapter.initialize(
            this,
            networkHandle ?: false,
            iFramePlayerOptions,
        )
        initialPlayer = true
    }

    @SuppressLint("RestrictedApi")
    override fun loadOrCueVideo(
        videoId: String,
        startTime: Float?,
    ) {
        youTubeStreamPlayer?.loadOrCueVideo(
            lifecycle,
            videoId,
            startTime ?: 0f,
        )
    }

    override fun loadVideo(
        videoId: String,
        startTime: Float?,
    ) {
        youTubeStreamPlayer?.loadVideo(videoId, startTime ?: 0f)
    }

    override fun cueVideo(
        videoId: String,
        startTime: Float?,
    ) {
        youTubeStreamPlayer?.cueVideo(videoId, startTime ?: 0f)
    }

    override fun start() {
        youTubeStreamPlayer?.let { player ->
//            if (youtubeStreamPlayerTracker.state == PlayerState.PLAYING) player.pause()
//            else player.play()
            player.play()
            _playbackState.update { YouTubeStreamPlaybackState.Playing }
        }
    }

    override fun seekTo(msec: Float) {
        youTubeStreamPlayer?.seekTo(msec)
    }

    override fun resume() {}

    override fun pause() {
        youTubeStreamPlayer?.pause()
        _playbackState.update { YouTubeStreamPlaybackState.Paused }
    }

    override fun stop() {}

    override fun isPlaying(): Boolean = _playbackState.value is YouTubeStreamPlaybackState.Playing

    override fun release() {
        youtubeStreamPlayerAdapter.release()
        _playbackState.update { YouTubeStreamPlaybackState.RELEASE }
    }

    override fun setMute(mute: Boolean) {
        if (mute) youTubeStreamPlayer?.mute() else youTubeStreamPlayer?.unMute()

        _isMute.update { mute }
    }

    override fun getVideoSize(): Size? =
        if (_playbackState.value == YouTubeStreamPlaybackState.Playing ||
            _playbackState.value == YouTubeStreamPlaybackState.Paused ||
            _playbackState.value == YouTubeStreamPlaybackState.Buffering
        ) {
            Size(
                youtubeStreamPlayerAdapter.getYouTubePlayerView().width,
                youtubeStreamPlayerAdapter.getYouTubePlayerView().height,
            )
        } else {
            null
        }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        youTubeStreamPlayer = youTubePlayer
        youTubeStreamPlayer?.addListener(this)
        _playbackState.update { YouTubeStreamPlaybackState.Prepared(this) }
    }

    override fun onStateChange(
        youTubePlayer: YouTubePlayer,
        state: PlayerState,
    ) {
        _playbackState.update { (getConvertPlayerStateToYouTubeStreamPlaybackState(state)) }
    }

    override fun onPlaybackQualityChange(
        youTubePlayer: YouTubePlayer,
        playbackQuality: PlayerConstants.PlaybackQuality,
    ) {
    }

    override fun onPlaybackRateChange(
        youTubePlayer: YouTubePlayer,
        playbackRate: PlayerConstants.PlaybackRate,
    ) {
    }

    override fun onError(
        youTubePlayer: YouTubePlayer,
        error: PlayerConstants.PlayerError,
    ) {
        _playbackError.update { getConvertPlayerErrorToYouTubeStreamPlayerError(error) }
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {
    }

    override fun onCurrentSecond(
        youTubePlayer: YouTubePlayer,
        second: Float,
    ) {
        _currentTime.update { second }
    }

    override fun onVideoDuration(
        youTubePlayer: YouTubePlayer,
        duration: Float,
    ) {
        _duration.update { duration }
    }

    override fun onVideoLoadedFraction(
        youTubePlayer: YouTubePlayer,
        loadedFraction: Float,
    ) {
    }

    override fun onVideoId(
        youTubePlayer: YouTubePlayer,
        videoId: String,
    ) {}

    private fun getConvertPlayerStateToYouTubeStreamPlaybackState(state: PlayerState): YouTubeStreamPlaybackState =
        when (state) {
            PlayerState.UNKNOWN -> YouTubeStreamPlaybackState.UnKnown
            PlayerState.UNSTARTED -> YouTubeStreamPlaybackState.UnStarted
            PlayerState.VIDEO_CUED -> YouTubeStreamPlaybackState.Prepared(this)
            PlayerState.PLAYING -> {
                setMute(true)
                YouTubeStreamPlaybackState.Playing
            }
            PlayerState.PAUSED -> YouTubeStreamPlaybackState.Paused
            PlayerState.BUFFERING -> {
                setMute(true)
                YouTubeStreamPlaybackState.Buffering
            }
            PlayerState.ENDED -> YouTubeStreamPlaybackState.Ended
        }

    private fun getConvertPlayerErrorToYouTubeStreamPlayerError(error: PlayerConstants.PlayerError): YouTubeStreamPlayerError =
        when (error) {
            PlayerConstants.PlayerError.UNKNOWN -> YouTubeStreamPlayerError.UNKNOWN
            PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST -> YouTubeStreamPlayerError.INVALID_PARAMETER_IN_REQUEST
            PlayerConstants.PlayerError.HTML_5_PLAYER -> YouTubeStreamPlayerError.HTML_5_PLAYER
            PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> YouTubeStreamPlayerError.VIDEO_NOT_FOUND
            PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER -> YouTubeStreamPlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER
        }

    override fun toggleFullscreen() {
        youTubeStreamPlayer?.toggleFullscreen()
    }
}
