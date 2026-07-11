package com.sean.ratel.android.ui.cast

import androidx.mediarouter.app.MediaRouteButton

interface MediaRouteButtonContainer {
    fun addMediaRouteButton(mediaRouteButton: MediaRouteButton)

    fun removeMediaRouteButton(mediaRouteButton: MediaRouteButton)
}
