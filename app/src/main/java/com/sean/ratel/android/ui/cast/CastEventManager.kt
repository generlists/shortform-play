package com.sean.ratel.android.ui.cast

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastEventManager
    @Inject
    constructor() {
        private val _playEvents = MutableStateFlow<CastEvent>(CastEvent.Idle)

        val playEvents = _playEvents.asStateFlow()

        private val _castSession = MutableStateFlow<CastSessionState>(CastSessionState.SessionUnKnown)

        val castSession = _castSession.asStateFlow()

        private var _playerState = MutableStateFlow<PlayerState>(PlayerState.LOCAL) // 플레이어를 로컬인지 캐스트 인지
        val playerState = _playerState.asStateFlow()

        fun setPlayerState(state: PlayerState) {
            _playerState.value = state
        }

        fun setCastSessionState(state: CastSessionState) {
            _castSession.value = state
        }

        fun sendEvent(event: CastEvent) {
            _playEvents.tryEmit(event)
        }
    }
