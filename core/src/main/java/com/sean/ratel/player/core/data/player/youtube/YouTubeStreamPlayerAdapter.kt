package com.sean.ratel.player.core.data.player.youtube

import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

interface YouTubeStreamPlayerAdapter {

    fun getYouTubePlayerView():View

    fun initialize(youTubePlayerListener: YouTubePlayerListener, handleNetworkEvents: Boolean, playerOptions: IFramePlayerOptions, videoId: String?)

    fun initialize(youTubePlayerListener: YouTubePlayerListener, handleNetworkEvents: Boolean, playerOptions: IFramePlayerOptions)

    fun initialize(youTubePlayerListener: YouTubePlayerListener, handleNetworkEvents: Boolean)

    fun initialize(youTubePlayerListener: YouTubePlayerListener, playerOptions: IFramePlayerOptions)

    fun initialize(youTubePlayerListener: YouTubePlayerListener)

    fun getYouTubePlayerWhenReady(youTubePlayerCallback: YouTubePlayerCallback)

    fun inflateCustomPlayerUi(@LayoutRes layoutId: Int):View

    fun setCustomPlayerUi(view: View)

    fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event)

    fun release()

    fun addYouTubePlayerListener(youTubePlayerListener: YouTubePlayerListener): Boolean

    fun removeYouTubePlayerListener(youTubePlayerListener: YouTubePlayerListener):Boolean

    fun addFullscreenListener(fullscreenListener: FullscreenListener):Boolean

    fun removeFullscreenListener(fullscreenListener: FullscreenListener): Boolean

    fun matchParent()

    fun wrapContent()

}