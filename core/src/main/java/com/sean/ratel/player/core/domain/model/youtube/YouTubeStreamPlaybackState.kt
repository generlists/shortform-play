package com.sean.ratel.player.core.domain.model.youtube

import com.sean.ratel.player.core.domain.YouTubeStreamPlayer


sealed class YouTubeStreamPlaybackState {

    data object UnKnown : YouTubeStreamPlaybackState()
    data object UnStarted : YouTubeStreamPlaybackState()
    data class Prepared(val youTubeStreamPlayer: YouTubeStreamPlayer) : YouTubeStreamPlaybackState()
    data object Playing : YouTubeStreamPlaybackState()
    data object Paused : YouTubeStreamPlaybackState()
    data object Buffering : YouTubeStreamPlaybackState()
    data object Ended : YouTubeStreamPlaybackState()
    data object RELEASE : YouTubeStreamPlaybackState()
    data class Error(val error: YouTubeStreamPlayerError) : YouTubeStreamPlaybackState()
}