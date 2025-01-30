package com.sean.ratel.player.core.domain.model

import com.sean.ratel.player.core.domain.MediaStreamPlayer


sealed class PlaybackState {

    data class Idle(val index: Int? = null) : PlaybackState()
    data class Preparing(val index: Int? = null) : PlaybackState()
    data class Prepared(val player: MediaStreamPlayer, val index: Int? = null) : PlaybackState()
    data class Playing(val player: MediaStreamPlayer, val index: Int? = null) : PlaybackState()
    data class Buffering(val index: Int? = null) : PlaybackState()
    data class Pause(val index: Int? = null) : PlaybackState()
    data class Stop(val index: Int? = null) : PlaybackState()
    data class Complete(val index: Int? = null) : PlaybackState()
    data class Release(val index: Int? = null) : PlaybackState()

    data class Error(val errorCode: Int, val throwable: Throwable?, val index: Int? = null) :
        PlaybackState()
}