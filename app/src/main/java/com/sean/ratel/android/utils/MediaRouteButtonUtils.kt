package com.sean.ratel.android.utils

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.mediarouter.R
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.sean.ratel.android.ui.cast.MediaRouteButtonContainer
import so.smartlab.common.utils.log.RLog

object MediaRouteButtonUtils {
    fun initMediaRouteButton(context: Context): MediaRouteButton? =
        try {
            val button = MediaRouteButton(context)
            CastButtonFactory.setUpMediaRouteButton(context, button)
            button
        } catch (e: IllegalArgumentException) {
            RLog.e("MediaRouteButtonUtils", "Failed to create MediaRouteButton", e)
            null
        }

    fun addMediaRouteButtonToPlayerUi(
        mediaRouteButton: MediaRouteButton,
        tintColor: Int,
        disabledContainer: MediaRouteButtonContainer?,
        activatedContainer: MediaRouteButtonContainer?,
    ) {
        setMediaRouterButtonTint(mediaRouteButton, tintColor)

        disabledContainer?.removeMediaRouteButton(mediaRouteButton)
        if (mediaRouteButton.parent != null) return
        // activatedContainer.addMediaRouteButton(mediaRouteButton)
    }

    private fun setMediaRouterButtonTint(
        mediaRouterButton: MediaRouteButton,
        color: Int,
    ) {
        val castContext = ContextThemeWrapper(mediaRouterButton.context, androidx.mediarouter.R.style.Theme_MediaRouter)
        val styledAttributes =
            castContext.obtainStyledAttributes(
                null,
                androidx.mediarouter.R.styleable.MediaRouteButton,
                androidx.mediarouter.R.attr.mediaRouteButtonStyle,
                0,
            )
        val drawable =
            styledAttributes.getDrawable(androidx.mediarouter.R.styleable.MediaRouteButton_externalRouteEnabledDrawable)

        styledAttributes.recycle()
        drawable?.let {
            DrawableCompat.setTint(
                it,
                ContextCompat.getColor(mediaRouterButton.context, color),
            )
        }

        mediaRouterButton.setRemoteIndicatorDrawable(drawable)
    }
}
