package com.sean.ratel.player.core.domain

import android.util.Size
import android.view.View
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlayerError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface YouTubeStreamPlayer {
    val playbackState: Flow<YouTubeStreamPlaybackState>

    val playbackError: Flow<YouTubeStreamPlayerError>

    val duration: StateFlow<Float>

    val currentTime: StateFlow<Float>

    fun getYouTubePlayerView(): View

    fun initPlayer(networkHandle: Boolean? = false)

    fun loadOrCueVideo(
        videoId: String,
        startTime: Float? = 0f,
    )

    fun loadVideo(
        videoId: String,
        startTime: Float? = 0f,
    )

    fun cueVideo(
        videoId: String,
        startTime: Float? = 0f,
    )

    fun start()

    fun seekTo(msec: Float)

    fun resume()

    fun pause()

    fun stop()

    fun isPlaying(): Boolean

    fun release()

    fun setMute(mute: Boolean)

    fun getVideoSize(): Size?

    fun toggleFullscreen()
}
