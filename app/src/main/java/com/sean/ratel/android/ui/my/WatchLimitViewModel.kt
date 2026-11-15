package com.sean.ratel.android.ui.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.android.data.log.GAAccountAnalytics
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.watch.DayWatch
import com.sean.ratel.android.data.watch.LimitAck
import com.sean.ratel.android.data.watch.LimitMode
import com.sean.ratel.android.data.watch.LimitPrompt
import com.sean.ratel.android.data.watch.WatchLimitPolicy
import com.sean.ratel.android.data.watch.WatchLimitRepository
import com.sean.ratel.android.data.watch.WatchLimitSettings
import com.sean.ratel.android.data.watch.WatchStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 하루 시청 한도: 마이 탭의 설정과 재생 화면의 "계속 볼까요?"가 같이 쓴다. 기록·설정 모두 기기에만 있다.
 * [prompt] 는 한도에 닿았을 때만 값이 있다(꺼져 있으면 항상 null).
 */
@HiltViewModel
class WatchLimitViewModel
    @Inject
    constructor(
        private val limits: WatchLimitRepository,
        stats: WatchStatsRepository,
        private val gaLog: GALog,
    ) : ViewModel() {
        val settings: StateFlow<WatchLimitSettings> =
            limits.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), WatchLimitSettings())

        /** 오늘 시청 기록(없으면 0). */
        val today: StateFlow<DayWatch> =
            stats.days
                .map { it[LocalDate.now()] ?: DayWatch() }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DayWatch())

        val prompt: StateFlow<LimitPrompt?> =
            combine(limits.settings, stats.days, limits.ack) { settings, days, ack ->
                val date = LocalDate.now()
                WatchLimitPolicy.evaluate(settings, days[date], ack, date)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

        fun setMode(mode: LimitMode) {
            track(
                GAAccountAnalytics.Event.LIMIT_SET,
                GAAccountAnalytics.Action.SELECT,
                mapOf(GAAccountAnalytics.Param.MODE to mode.name.lowercase()),
            )
            viewModelScope.launch { limits.setMode(mode) }
        }

        fun setMinutes(minutes: Int) {
            track(
                GAAccountAnalytics.Event.LIMIT_SET,
                GAAccountAnalytics.Action.SELECT,
                mapOf(GAAccountAnalytics.Param.MODE to "time", GAAccountAnalytics.Param.VALUE to minutes),
            )
            viewModelScope.launch { limits.setMinutes(minutes) }
        }

        fun setVideos(videos: Int) {
            track(
                GAAccountAnalytics.Event.LIMIT_SET,
                GAAccountAnalytics.Action.SELECT,
                mapOf(GAAccountAnalytics.Param.MODE to "videos", GAAccountAnalytics.Param.VALUE to videos),
            )
            viewModelScope.launch { limits.setVideos(videos) }
        }

        /** "계속 보기" — 지금까지의 시청량을 기록해서, 다음 질문은 한 걸음(10분 또는 영상 5개) 뒤에 하게 한다. */
        fun continueWatching() {
            val current = prompt.value ?: return
            track(GAAccountAnalytics.Event.LIMIT_CONTINUE, params = mapOf(GAAccountAnalytics.Param.MODE to current.mode.name.lowercase()))
            viewModelScope.launch { limits.acknowledge(LimitAck(LocalDate.now(), current.seconds, current.videos)) }
        }

        /** 한도 안내를 화면에 띄운 것과 "그만 보기"를 GA 로 보낸다. */
        fun trackPromptShown() = track(GAAccountAnalytics.Event.LIMIT_PROMPT_SHOW, GAAccountAnalytics.Action.VIEW, promptParams())

        fun trackStop() = track(GAAccountAnalytics.Event.LIMIT_STOP, params = promptParams())

        private fun promptParams(): Map<String, Any> =
            prompt.value?.let {
                mapOf(GAAccountAnalytics.Param.MODE to it.mode.name.lowercase())
            }.orEmpty()

        private fun track(
            event: String,
            action: String = GAAccountAnalytics.Action.CLICK,
            params: Map<String, Any> = emptyMap(),
        ) = gaLog.sendEvent(GAAccountAnalytics.Screen.MY, event, action, params)

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
