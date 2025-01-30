package com.sean.ratel.android.ui.pip

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import android.util.Size
import androidx.lifecycle.ViewModel
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.utils.UIUtil.hasPipPermission

/**
 * PIP View 모델
 */
class PIPViewModel : ViewModel() {
    fun enterPipMode(
        context: Context?,
        videoSize: Size?,
        rect: Rect,
        isPlaying: Boolean,
    ): PipResult {
        val pipContext = context ?: return PipResult.UnKnownReason
        if (videoSize == null) return PipResult.UnKnownReason

        val hasPipSystemFeature =
            pipContext.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        if (!hasPipSystemFeature) return PipResult.NoSystemFeature

        if (!pipContext.hasPipPermission()) return PipResult.NoPermission
        updatePipParamsAndGo(context, isPlaying, videoSize, rect)

        return PipResult.Success
    }

    private fun updatePipParamsAndGo(
        context: Context?,
        isPlaying: Boolean,
        videoSize: Size?,
        rect: Rect,
    ) = updatePipParams(context, isPlaying, videoSize, rect, true)

    fun updatePipParams(
        context: Context?,
        isPlaying: Boolean,
        videoSize: Size?,
        rect: Rect,
        enter: Boolean = false,
    ) {
        val pipContext = (context as? Activity) ?: return
        val aspectRatio = getPipAspectRatio(videoSize) ?: return
        val playAction = PipAction.getRemoteAction(pipContext, isPlaying)
        val playPrevAction = PipAction.getRemoteAction(pipContext, PipAction.SKIP_PREVIOUS)
        val playNextAction = PipAction.getRemoteAction(pipContext, PipAction.SKIP_NEXT)

        RLog.d(
            "hbungshin",
            "[PIP] updatePipParams isPlaying $isPlaying ," +
                " aspectRatio : $aspectRatio, enter : $enter",
        )

        val params =
            PictureInPictureParams
                .Builder()
                .setActions(listOf(playPrevAction, playAction, playNextAction))
                .setAspectRatio(aspectRatio)
                .setSourceRectHint(rect)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // The screen automatically turns into the picture-in-picture mode when it is hidden
            // by the "Home" button.
            params.setAutoEnterEnabled(true)
            params.setSeamlessResizeEnabled(true)
        }
        val updateParam =
            params.build().also {
                pipContext.setPictureInPictureParams(it)
            }
        if (enter) {
            pipContext.enterPictureInPictureMode(updateParam)
        } else {
            pipContext.setPictureInPictureParams(updateParam)
        }
    }

    private fun getPipAspectRatio(videoSize: Size?): Rational? {
        val size = videoSize ?: return null
        val aspectRatio = Rational(size.width, size.height)
        return when {
            aspectRatio >= MAX_PIP_ASPECT_RATIO -> MAX_PIP_ASPECT_RATIO
            aspectRatio <= MIN_PIP_ASPECT_RATIO -> MIN_PIP_ASPECT_RATIO
            else -> aspectRatio
        }
    }

    companion object {
        val TAG = "PIPViewModel"
        private val MAX_PIP_ASPECT_RATIO: Rational = Rational(720, 1080)
        private val MIN_PIP_ASPECT_RATIO: Rational = Rational(9, 16)
    }
}
