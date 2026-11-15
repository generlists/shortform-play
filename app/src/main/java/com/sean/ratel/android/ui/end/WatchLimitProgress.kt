package com.sean.ratel.android.ui.end

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.R
import com.sean.ratel.android.data.watch.LimitMode
import com.sean.ratel.android.ui.my.WatchLimitViewModel
import com.sean.ratel.android.ui.my.formatWatchTime
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR

private const val SEGMENTS = 20
private const val TOOLBAR_HEIGHT_DP = 56
private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60

/**
 * 재생 화면 상단의 "오늘의 쇼츠 7 / 20" 진행 표시(시안 02). 하루 시청 한도를 **켰을 때만** 보인다.
 * 영상 수 한도면 본 영상 수, 시간 한도면 오늘 본 시간을 한도와 함께 20칸 막대로 보여 준다. 터치는 가로채지 않는다.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun WatchLimitProgress(modifier: Modifier = Modifier) {
    val viewModel: WatchLimitViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()
    if (settings.mode == LimitMode.OFF) return

    val used: Long
    val limit: Long
    val label: String
    when (settings.mode) {
        LimitMode.VIDEOS -> {
            used = today.videos.toLong()
            limit = settings.videos.toLong()
            label = stringResource(R.string.limit_progress_videos, today.videos, settings.videos)
        }
        else -> {
            used = today.seconds
            limit = settings.minutes * SECONDS_PER_MINUTE
            label = stringResource(R.string.limit_progress_time, formatWatchTime(today.seconds), limitLabel(settings.minutes))
        }
    }
    val filled = if (limit <= 0L) 0 else (used * SEGMENTS / limit).toInt().coerceIn(0, SEGMENTS)
    val reached = used >= limit

    Column(
        modifier
            .fillMaxWidth()
            // 상태 표시줄 + 상단 도구 줄(뒤로가기·캐스트 …) 바로 아래
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + TOOLBAR_HEIGHT_DP.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(SEGMENTS) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (index < filled) APP_TEXT_COLOR else Color(0x40FFFFFF), RoundedCornerShape(2.dp)),
                )
            }
        }
        Text(label, color = if (reached) APP_TEXT_COLOR else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

/** 한도 이름(15분, 1시간 30분 …). */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun limitLabel(minutes: Int): String =
    when {
        minutes < MINUTES_PER_HOUR -> stringResource(R.string.report_time_minutes, minutes.toLong())
        minutes % MINUTES_PER_HOUR == 0 -> stringResource(R.string.limit_option_hours, minutes / MINUTES_PER_HOUR)
        else -> stringResource(R.string.report_time_hours, (minutes / MINUTES_PER_HOUR).toLong(), (minutes % MINUTES_PER_HOUR).toLong())
    }
