package com.sean.ratel.android.ui.cast

sealed interface CastEvent {
    data object Idle : CastEvent

    data object Play : CastEvent

    data object Error : CastEvent

    data object Pause : CastEvent

    data object Prev : CastEvent

    data object Next : CastEvent

    data object END : CastEvent

    data class PlaySpeed(
        val speed: Float,
    ) : CastEvent

    data class CaptionOnOff(
        val isCaptionOn: Boolean,
    ) : CastEvent

    data class Mute(
        val isMute: Boolean,
    ) : CastEvent
}

enum class PlayerState {
    LOCAL,
    CAST,
}

sealed interface CastSessionState {
    data class SessionStart(
        val castDevice: String?,
    ) : CastSessionState

    object SessionEnd : CastSessionState

    object SessionUnKnown : CastSessionState

    object SessionFail : CastSessionState
}
