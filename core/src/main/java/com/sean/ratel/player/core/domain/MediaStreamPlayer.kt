package com.sean.ratel.player.core.domain

import android.net.Uri
import android.view.Surface
import android.view.SurfaceView
import com.sean.ratel.player.core.Configurations
import com.sean.ratel.player.core.domain.model.PlayMuteInfo
import com.sean.ratel.player.core.domain.model.PlaybackState
import com.sean.ratel.player.core.domain.model.Resolution
import com.sean.ratel.player.core.domain.model.SampleBandWidth
import com.sean.ratel.player.core.domain.model.track.AudioTrack
import com.sean.ratel.player.core.domain.model.track.VideoTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MediaStreamPlayer {
    val playbackState: Flow<PlaybackState>

    val resolution: Flow<Resolution>

    val sampleBandWidth: Flow<SampleBandWidth>

    val isMute: StateFlow<PlayMuteInfo>

    val maximumVideoQuality: StateFlow<Int>

    val videoTracks: StateFlow<List<VideoTrack>>

    val audioTracks: StateFlow<List<AudioTrack>>

    val selectedVideoTrack: StateFlow<VideoTrack?>

    val selectedAudioTrack: StateFlow<AudioTrack?>


    fun setPlayerConfig(configurations: Configurations)

    fun start(uri: Uri, playIndex: Int? = null)

    fun resume()

    fun pause()

    fun stop()

    fun seekTo(msec: Long)

    fun isPlaying(): Boolean

    fun isPlayComplete(): Boolean

    fun getDuration(): Long

    fun getCurrentPosition(): Long

    fun getBufferedPosition(): Long

    fun release()

    fun setVideoSurface(surface: Surface?)

    fun setVideoSurfaceView(surface: SurfaceView?)

    fun clearVideoSurface()

    fun setMute(mute: Boolean)

    fun setMute(playIndex: Int, mute: Boolean)

    fun setMaximumVideoQuality(quality: Int, isUserSelect: Boolean = true)
}