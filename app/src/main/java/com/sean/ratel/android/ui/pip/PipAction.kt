package com.sean.ratel.android.ui.pip

import android.app.PendingIntent
import android.app.RemoteAction
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import com.sean.ratel.android.R

private const val REQUEST_CODE_PLAY = 1
private const val REQUEST_CODE_PAUSE = 2
private const val REQUEST_CODE_SKIP_PREVIOUS = 3
private const val REQUEST_CODE_SKIP_NEXT = 4
private const val INTENT_ACTION_OF_PIP = "splay.intent.action.pip"
private const val INTENT_EXTRA_NAME_OF_PIP = "splay.intent.extra.pip"
private const val INTENT_EXTRA_VALUE_OF_PLAY = 1
private const val INTENT_EXTRA_VALUE_OF_PAUSE = 2
private const val INTENT_EXTRA_VALUE_OF_PREVIOUS = 3
private const val INTENT_EXTRA_VALUE_OF_NEXT = 4

/**
 * An enum class to create [RemoteAction] of PIP
 * todo
 * 1. next,prev  추가,설정추가(PIP),연속재생옵션,애니매이션 튜닝,cose 할때 홈으로 이동되나 리서치 해봐야함,미디어 세션으로 꼭 해야하나?,소리제어
 */
enum class PipAction(
    val requestCode: Int,
    val intentAction: String,
    val intentExtraName: String,
    val intentExtraValue: Int,
    val iconDrawableResId: Int,
) {
    PLAY(
        requestCode = REQUEST_CODE_PLAY,
        intentAction = INTENT_ACTION_OF_PIP,
        intentExtraName = INTENT_EXTRA_NAME_OF_PIP,
        intentExtraValue = INTENT_EXTRA_VALUE_OF_PLAY,
        iconDrawableResId = R.drawable.pip_play,
    ),
    PAUSE(
        requestCode = REQUEST_CODE_PAUSE,
        intentAction = INTENT_ACTION_OF_PIP,
        intentExtraName = INTENT_EXTRA_NAME_OF_PIP,
        intentExtraValue = INTENT_EXTRA_VALUE_OF_PAUSE,
        iconDrawableResId = R.drawable.pip_pause,
    ),
    SKIP_PREVIOUS(
        requestCode = REQUEST_CODE_SKIP_PREVIOUS,
        intentAction = INTENT_ACTION_OF_PIP,
        intentExtraName = INTENT_EXTRA_NAME_OF_PIP,
        intentExtraValue = INTENT_EXTRA_VALUE_OF_PREVIOUS,
        iconDrawableResId = R.drawable.pip_skip_previous,
    ),
    SKIP_NEXT(
        requestCode = REQUEST_CODE_SKIP_NEXT,
        intentAction = INTENT_ACTION_OF_PIP,
        intentExtraName = INTENT_EXTRA_NAME_OF_PIP,
        intentExtraValue = INTENT_EXTRA_VALUE_OF_NEXT,
        iconDrawableResId = R.drawable.pip_skip_next,
    ),
    ;

    companion object {
        fun getRemoteAction(
            context: Context,
            isPlaying: Boolean,
        ): RemoteAction = getRemoteAction(context, if (isPlaying) PAUSE else PLAY)

        fun getIntentFilter() = IntentFilter(INTENT_ACTION_OF_PIP)

        fun isPlayAction(intent: Intent?): Boolean {
            if (!hasPipAction(intent)) return false
            return intent?.getIntExtra(INTENT_EXTRA_NAME_OF_PIP, 0) ==
                INTENT_EXTRA_VALUE_OF_PLAY
        }

        fun isPauseAction(intent: Intent?): Boolean {
            if (!hasPipAction(intent)) return false
            return intent?.getIntExtra(INTENT_EXTRA_NAME_OF_PIP, 0) ==
                INTENT_EXTRA_VALUE_OF_PAUSE
        }

        fun isPreviousAction(intent: Intent?): Boolean {
            if (!hasPipAction(intent)) return false
            return intent?.getIntExtra(INTENT_EXTRA_NAME_OF_PIP, 0) ==
                INTENT_EXTRA_VALUE_OF_PREVIOUS
        }

        fun isNextAction(intent: Intent?): Boolean {
            if (!hasPipAction(intent)) return false
            return intent?.getIntExtra(INTENT_EXTRA_NAME_OF_PIP, 0) ==
                INTENT_EXTRA_VALUE_OF_NEXT
        }

        private fun hasPipAction(intent: Intent?): Boolean = intent?.action == INTENT_ACTION_OF_PIP

        fun getRemoteAction(
            context: Context,
            action: PipAction,
        ): RemoteAction {
            val intent =
                PendingIntent.getBroadcast(
                    context,
                    action.requestCode,
                    Intent(action.intentAction)
                        .setPackage(context.packageName) // Android 15 부터 명시적 내부전송을 명확히 보내야함
                        .putExtra(
                            action.intentExtraName,
                            action.intentExtraValue,
                        ),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            val icon = Icon.createWithResource(context, action.iconDrawableResId)
            return RemoteAction(icon, "", "", intent)
        }
    }
}
