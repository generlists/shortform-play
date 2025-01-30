package com.sean.ratel.player.core.data.player.media

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.SurfaceView
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import com.sean.player.utils.log.RLog
import com.sean.ratel.player.core.Configurations
import com.sean.ratel.player.core.configurations
import com.sean.ratel.player.core.domain.MediaStreamPlayer
import com.sean.ratel.player.core.domain.api.UserAgentProvider
import com.sean.ratel.player.core.domain.model.PlayMuteInfo
import com.sean.ratel.player.core.domain.model.PlaybackState
import com.sean.ratel.player.core.domain.model.Resolution
import com.sean.ratel.player.core.domain.model.SampleBandWidth
import com.sean.ratel.player.core.domain.model.track.AudioTrack
import com.sean.ratel.player.core.domain.model.track.VideoTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.TreeMap

@UnstableApi
class MediaExoStreamPlayer(
    private val context: Context,
    userAgentProvider: UserAgentProvider
):MediaStreamPlayer,
  Player.Listener{

    private var player: ExoPlayer? = null
    private var bufferForPlaybackMs: Int = BUFFER_FOR_PLAYBACK_TIME
    private var bufferForPlaybackAfterRebufferMs: Int = BUFFER_FOR_PLAYBACK_AFTER_REBUFFER
    private var maxBufferMs: Int = MAX_BUFFER_PLAYBACK_TIME
    private var minBufferMs: Int = MIN_BUFFER_PLAYBACK_TIME
    private var isPreparing = false
    private var playerIndex: Int? = null
    private var userAgent: String = userAgentProvider.userAgent

    private val defaultBandwidthMeter =
        DefaultBandwidthMeter.Builder(context)
            .setResetOnNetworkTypeChange(true)
            /** Network changes invalidate existing data **/
            // .setInitialBitrateEstimate(Integer.MAX_VALUE.toLong()) // later tuning
            .build()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle(playerIndex))
    override val playbackState: Flow<PlaybackState> = _playbackState

    private  val _isMute = MutableStateFlow(PlayMuteInfo(0,false))
    override val isMute: StateFlow<PlayMuteInfo> = _isMute

    private val _maximumVideoQuality =  MutableStateFlow<Int>(Int.MAX_VALUE)
    override val maximumVideoQuality: StateFlow<Int> = _maximumVideoQuality

    private val _videoTracks = MutableStateFlow<List<VideoTrack>>(emptyList())
    private val _audioTracks = MutableStateFlow<List<AudioTrack>>(emptyList())

    override val videoTracks: StateFlow<List<VideoTrack>> = _videoTracks
    override val audioTracks: StateFlow<List<AudioTrack>> = _audioTracks


    private val _selectedAudioTrack = MutableStateFlow<AudioTrack?>(null)
    override val selectedAudioTrack: StateFlow<AudioTrack?> = _selectedAudioTrack

    private val _selectedVideoTrack = MutableStateFlow<VideoTrack?>(null)
    override val selectedVideoTrack: StateFlow<VideoTrack?> = _selectedVideoTrack


    private val _resolution = MutableStateFlow(
        Resolution(
            1080,
            720,
            1F
        )
    )
    override val resolution: Flow<Resolution> = _resolution

    private val _sampleBandWidth = MutableStateFlow(SampleBandWidth(0, 0L, 0L))
    override val sampleBandWidth: Flow<SampleBandWidth> = _sampleBandWidth


    override fun setPlayerConfig(configurations: Configurations) {
        this.bufferForPlaybackAfterRebufferMs =
            configurations.videoConfig.bufferForPlaybackAfterRebufferMs
                ?: BUFFER_FOR_PLAYBACK_AFTER_REBUFFER // rebuffering
        this.bufferForPlaybackMs =
            configurations.videoConfig.bufferForPlaybackMs ?: BUFFER_FOR_PLAYBACK_TIME // init,seek
        this.maxBufferMs =
            configurations.videoConfig.maxBufferMs ?: MAX_BUFFER_PLAYBACK_TIME // max buffering
        this.minBufferMs =
            configurations.videoConfig.minBufferMs ?: MIN_BUFFER_PLAYBACK_TIME // min buffering
        this._maximumVideoQuality.update {
            configurations.videoConfig.maximumVideoQuality ?: MAX_VIDEO_QUALITY
        }
    }

    override fun start(uri: Uri, playIndex: Int?) {
        RLog.d(TAG, "play = ${uri} userAgent : $userAgent ,  player : $player :$playIndex")
        playerIndex = playIndex ?: 0
        player?.let { release() }

        _playbackState.update { PlaybackState.Idle(playerIndex) }

        player = createPlayer().apply {
            initListener(this)
            isPreparing = true
            _playbackState.update {
                PlaybackState.Preparing(playerIndex)
            }
            setMediaSource(getMediaSource(uri))
            prepare()
            mutePlay(_isMute.value)
            playWhenReady = true
        }
    }

    override fun resume() {
        val player = player ?: return
        RLog.d(TAG, "resume() playbackState= ${player.playbackState}")

        if (player.playbackState == ExoPlayer.STATE_IDLE) { // Not ready to play.
            return
        }
        if (!player.playWhenReady) {
            player.playWhenReady = true
        }
        when (player.playbackState) {
            ExoPlayer.STATE_BUFFERING -> if (player.playWhenReady) _playbackState.update {
                PlaybackState.Buffering(
                    playerIndex
                )
            }

            ExoPlayer.STATE_READY -> _playbackState.update {
                PlaybackState.Playing(
                    player = this,
                    playerIndex
                )
            }

            else -> Unit
        }
    }
    override fun clearVideoSurface() = setVideoSurfaceView(null)

    override fun setMute(mute: Boolean) {
        _isMute.update { PlayMuteInfo(0, mute) }
    }
    override fun setMute(playIndex: Int, mute: Boolean) {
        _isMute.update { PlayMuteInfo(playIndex, mute) }
    }
    override fun setMaximumVideoQuality(quality: Int, isUserSelect: Boolean) {
        RLog.d(TAG, "setMaximumVideoQuality() called with: quality = $quality")
        val videoTracks = videoTracks.value
//        if (videoTracks.all { track -> track.height != null }) {
//            // VOD: 해상도 정보가 모두 존재
//            player?.apply {
//                trackSelectionParameters = trackSelectionParameters
//                    .buildUpon()
//                    .setMaxVideoBitrate(Int.MAX_VALUE)
//                    .setMaxVideoSize(Int.MAX_VALUE, quality)
//                    .build()
//            }
//        } else {

//            PlayerLog.d("videoSize","videoTrack : ${videoTracks}")
        // LIVE: 해상도 정보 미제공 -> 화질로 품질 추정
        val bitrateMap = TreeMap<Int, Int>()
        videoTracks.forEach { track ->
            bitrateMap[track.estimatedHeightFromBitrate] = track.bitrate ?: Int.MAX_VALUE
        }
        val bitrate = bitrateMap.ceilingEntry(quality)?.value ?: Int.MAX_VALUE
        player?.apply {
            trackSelectionParameters = trackSelectionParameters
                .buildUpon()
                .clearVideoSizeConstraints()
                .setMaxVideoBitrate(bitrate)
                .build()
        }
        configurations(context) {
            playConfiguration(
                bufferForPlaybackAfterRebufferMs = bufferForPlaybackAfterRebufferMs,
                bufferForPlaybackMs = bufferForPlaybackAfterRebufferMs,
                maxBufferMs = maxBufferMs,
                minBufferMs = minBufferMs,
                maximumVideoQuality = quality
            )
        }
    }

    override fun pause() {
        player?.takeIf { it.playWhenReady }?.apply {
            playWhenReady = false
            _playbackState.update { PlaybackState.Pause(playerIndex) }
        } ?: return
    }

    override fun stop() {
        player?.takeIf { it.playWhenReady }?.apply {
            stop()
            _playbackState.update { PlaybackState.Stop(playerIndex) }
        } ?: return
    }

    override fun seekTo(msec: Long) {
        player?.seekTo(msec)
    }

    override fun isPlaying(): Boolean =
        player != null &&
                player?.playbackState == ExoPlayer.STATE_READY &&
                player?.playWhenReady == true

    override fun isPlayComplete(): Boolean = player != null &&
            player?.playbackState == ExoPlayer.STATE_ENDED

    override fun getDuration(): Long = player?.duration ?: 0

    override fun getCurrentPosition(): Long = player?.currentPosition ?: 0

    override fun getBufferedPosition(): Long = player?.bufferedPosition ?: 0

    override fun release() {
        RLog.d(TAG,"release() player : $player")
        _playbackState.update { PlaybackState.Release(playerIndex) }
        player?.also {
            it.removeListener(this)
            it.release()
        }.also { player = null }
        player = null
        _playbackState.update { PlaybackState.Idle(playerIndex) }
    }

    override fun setVideoSurface(surface: Surface?) {
        player?.setVideoSurface(surface)
    }

    override fun setVideoSurfaceView(surface: SurfaceView?) {
        player?.setVideoSurfaceView(surface)
    }
    @Deprecated("Deprecated in Java")
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> _playbackState.update { PlaybackState.Idle(playerIndex) }
            Player.STATE_BUFFERING -> if (playWhenReady) _playbackState.update { PlaybackState.Buffering(playerIndex) }
            Player.STATE_READY -> {
                if (playWhenReady) {
                    if (isPreparing) {
                        _playbackState.update { PlaybackState.Prepared(this,playerIndex) }
                        isPreparing = false
                    }
                    _playbackState.update { PlaybackState.Playing(this,playerIndex) }
                } else {
                    _playbackState.update { PlaybackState.Pause(playerIndex) }
                }
            }

            Player.STATE_ENDED -> if (playWhenReady) _playbackState.update { PlaybackState.Complete(playerIndex) }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        RLog.e(TAG,"cause : ${error.cause}")
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> {
                // restart play
                val uri = player?.currentMediaItem?.localConfiguration?.uri
                if (uri == null) {
                    dispatchError(error.errorCode, error.cause)
                } else {
                    val prevVolume = _isMute.value
                    start(uri)
                    _isMute.update { prevVolume }
                }
            }

            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> dispatchError(error.errorCode, error.cause)

            else -> {
                dispatchError(error.errorCode, error.cause)
            }
        }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) = _resolution.update {
        Resolution(
            videoSize.width,
            videoSize.height,
            videoSize.pixelWidthHeightRatio
        )
    }
    override fun onTracksChanged(tracks: Tracks) {
        RLog.d(TAG, "onTracksChanged() tracks = $tracks")
        val videoTracks = mutableListOf<VideoTrack>()
        val audioTracks = mutableListOf<AudioTrack>()
        tracks.groups.forEachIndexed { _, group ->
            when (group.type) {
                C.TRACK_TYPE_VIDEO -> {
                    videoTracks.addAll(getVideoTracks(group))
                }

                C.TRACK_TYPE_AUDIO -> {
                    audioTracks.addAll(getAudioTracks(group))
                }
            }
        }
        _videoTracks.update { videoTracks }
        _audioTracks.update { audioTracks }

    }
    private fun getVideoTracks(group: Tracks.Group): List<VideoTrack> {
        val tracks = mutableListOf<VideoTrack>()
        for (i in 0 until group.length) {
            group.getTrackFormat(i).apply {
                val track = VideoTrack(
                    id,
                    if (bitrate == Format.NO_VALUE) null else bitrate,
                    if (width == Format.NO_VALUE) null else width,
                    if (height == Format.NO_VALUE) null else height,
                    if (frameRate == Format.NO_VALUE.toFloat()) null else frameRate,
                    language
                )
                tracks.add(track)
            }
        }
        return tracks
    }

    private fun getAudioTracks(group: Tracks.Group): List<AudioTrack> {
        val tracks = mutableListOf<AudioTrack>()
        for (i in 0 until group.length) {
            group.getTrackFormat(i).apply {
                val track = AudioTrack(id, language, channelCount)
                tracks.add(track)
            }
        }
        return tracks
    }
    private fun initListener(player: ExoPlayer?) =
        player?.run {
            addListener(this@MediaExoStreamPlayer)
        }

    private fun getMediaSource(uri: Uri): MediaSource =
        when (Util.inferContentType(uri)) {
            C.CONTENT_TYPE_HLS -> {
                HlsMediaSource.Factory(getDefaultDatasource()).setAllowChunklessPreparation(true)
                    .setLoadErrorHandlingPolicy(
                        DefaultLoadErrorHandlingPolicy(HLS_REQUEST_RETRY_COUNT)
                    ).createMediaSource(getMediaItem(uri))
            }

            else -> ProgressiveMediaSource.Factory(getDefaultDatasource())
                .createMediaSource(MediaItem.Builder().setUri(uri).build())
        }


    private fun getMediaItem(uri: Uri): MediaItem {
        return MediaItem.Builder()
            .setUri(uri)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()
    }
    @OptIn(UnstableApi::class)
    private fun createPlayer(): ExoPlayer {
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(
            TARGET_DURATION_MS,
            (TARGET_DURATION_MS * QUALITY_DECREASE_SCALE).toInt(),
            (TARGET_DURATION_MS * QUALITY_DECREASE_SCALE).toInt(),
            AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION
        )
        val renderersFactory = DefaultRenderersFactory(context)
        renderersFactory.setEnableDecoderFallback(true) // 소프트웨어 코덱 사용

        val trackSelector = DefaultTrackSelector(context, videoTrackSelectionFactory)
        val loadControlBuilder = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
            .setBufferDurationsMs(
                minBufferMs,
                maxBufferMs,
                bufferForPlaybackMs,
                bufferForPlaybackAfterRebufferMs
            ).
            build()

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(getDefaultDatasource()))
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(getBandwidthMeter())
            .setLoadControl(loadControlBuilder)
            .setRenderersFactory(renderersFactory)
            .build()
    }

    private fun getBandwidthMeter(): DefaultBandwidthMeter =

        defaultBandwidthMeter.apply {
            addEventListener(
                Handler(Looper.getMainLooper()),
                bandwidthMeterEventListener
            )
        }

    private fun getDefaultDatasource(): DefaultHttpDataSource.Factory =
        DefaultHttpDataSource.Factory().setUserAgent(userAgent)

    private val bandwidthMeterEventListener =
        BandwidthMeter.EventListener { elapsedMs: Int, bytes: Long, bitrate: Long ->
            //TLog.d(TAG, "elapsedMs : $elapsedMs , bytes : $bytes , bitrate : $bitrate")
            _sampleBandWidth.update { SampleBandWidth(elapsedMs, bytes, bitrate) }
        }

    private fun dispatchError(errorCode: Int, cause: Throwable?) {
        _playbackState.update { PlaybackState.Error(errorCode, cause,playerIndex) }
    }

    private fun mutePlay(muteInfo: PlayMuteInfo) {
        val (playIndex, isMute) = muteInfo
        if (playerIndex == playIndex) {
            player?.also {
                it.volume = if (isMute) 0f else 1f
            }
        }
    }

    companion object {
        private const val TAG = "RExoPlayer"
        private const val TARGET_DURATION_MS = 2000
        private const val QUALITY_DECREASE_SCALE = 2.5f
        private const val HLS_REQUEST_RETRY_COUNT = 3
        private const val MIN_BUFFER_PLAYBACK_TIME = 2_000
        private const val MAX_BUFFER_PLAYBACK_TIME = 24_0000
        private const val BUFFER_FOR_PLAYBACK_TIME = 2_000
        private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER = 2_000
        private const val MAX_VIDEO_QUALITY = Int.MAX_VALUE
    }

}