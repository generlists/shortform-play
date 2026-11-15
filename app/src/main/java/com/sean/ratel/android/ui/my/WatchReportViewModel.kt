package com.sean.ratel.android.ui.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.android.data.watch.WatchStatsRepository
import com.sean.ratel.android.data.watch.WeeklyReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/** 마이 탭의 "이번 주 시청" 카드가 쓰는 주간 리포트. 기록은 기기에만 있다. */
@HiltViewModel
class WatchReportViewModel
    @Inject
    constructor(
        repository: WatchStatsRepository,
    ) : ViewModel() {
        val report: StateFlow<WeeklyReport> =
            repository.days
                .map { WeeklyReport.build(it, LocalDate.now()) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), WeeklyReport.Empty)

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
