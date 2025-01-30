package com.sean.ratel.player.core.data.player.provider

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerAdapter

interface YouTubeStreamPlayerParamProvider {

    val youtubeStreamPlayerAdapter: YouTubeStreamPlayerAdapter
    val iFramePlayerOptions: IFramePlayerOptions
    val youtubeStreamPlayerTracker: YouTubePlayerTracker

}