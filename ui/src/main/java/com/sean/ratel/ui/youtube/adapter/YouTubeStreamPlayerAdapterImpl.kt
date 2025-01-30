package com.sean.ratel.ui.youtube.adapter

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerAdapter

class YouTubeStreamPlayerAdapterImpl(private val youTubePlayerView: YouTubePlayerView) :
    YouTubeStreamPlayerAdapter {

    override fun getYouTubePlayerView(): View = youTubePlayerView

    override fun initialize(
        youTubePlayerListener: YouTubePlayerListener,
        handleNetworkEvents: Boolean,
        playerOptions: IFramePlayerOptions,
        videoId: String?
    ) =
        youTubePlayerView.initialize(
            youTubePlayerListener,
            handleNetworkEvents,
            playerOptions,
            videoId
        )

    override fun initialize(
        youTubePlayerListener: YouTubePlayerListener,
        handleNetworkEvents: Boolean,
        playerOptions: IFramePlayerOptions
    ) =
        youTubePlayerView.initialize(youTubePlayerListener, handleNetworkEvents, playerOptions)


    override fun initialize(
        youTubePlayerListener: YouTubePlayerListener,
        handleNetworkEvents: Boolean
    ) =
        youTubePlayerView.initialize(youTubePlayerListener, handleNetworkEvents)


    override fun initialize(
        youTubePlayerListener: YouTubePlayerListener,
        playerOptions: IFramePlayerOptions
    ) = youTubePlayerView.initialize(youTubePlayerListener, playerOptions)


    override fun initialize(youTubePlayerListener: YouTubePlayerListener) =
        youTubePlayerView.initialize(youTubePlayerListener)


    override fun getYouTubePlayerWhenReady(youTubePlayerCallback: YouTubePlayerCallback) {
        youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayerCallback)
    }

    override fun inflateCustomPlayerUi(layoutId: Int): View =
        youTubePlayerView.inflateCustomPlayerUi(layoutId)


    override fun setCustomPlayerUi(view: View) {
        youTubePlayerView.setCustomPlayerUi(view)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        youTubePlayerView.onStateChanged(source, event)
    }

    override fun release() {
        youTubePlayerView.release()
    }

    override fun addYouTubePlayerListener(youTubePlayerListener: YouTubePlayerListener): Boolean =
        youTubePlayerView.addYouTubePlayerListener(youTubePlayerListener)


    override fun removeYouTubePlayerListener(youTubePlayerListener: YouTubePlayerListener): Boolean =
        youTubePlayerView.removeYouTubePlayerListener(youTubePlayerListener)


    override fun addFullscreenListener(fullscreenListener: FullscreenListener): Boolean =
        youTubePlayerView.addFullscreenListener(fullscreenListener)


    override fun removeFullscreenListener(fullscreenListener: FullscreenListener): Boolean =
        youTubePlayerView.removeFullscreenListener(fullscreenListener)


    override fun matchParent() {
        youTubePlayerView.matchParent()
    }

    override fun wrapContent() {
        youTubePlayerView.wrapContent()
    }
}