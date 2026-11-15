package com.sean.ratel.android.ui.my

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.data.watch.DayWatch
import com.sean.ratel.android.data.watch.LimitMode
import com.sean.ratel.android.data.watch.WatchLimitPolicy
import com.sean.ratel.android.data.watch.WatchLimitSettings
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR

private const val MINUTES_PER_HOUR = 60

/**
 * 마이 탭의 "시청 시간 관리" — 하루 한도를 시간 또는 영상 수로 정한다. 기본은 꺼짐.
 * 한도에 닿으면 재생 화면이 "계속 볼까요?"라고 묻는다(기록·설정은 기기에만 저장).
 */
@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun WatchLimitCard(
    settings: WatchLimitSettings,
    today: DayWatch,
    onMode: (LimitMode) -> Unit,
    onMinutes: (Int) -> Unit,
    onVideos: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.limit_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.limit_desc), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 13.sp, lineHeight = 19.sp)
        }
        Text(
            stringResource(R.string.limit_today, formatWatchTime(today.seconds), today.videos),
            color = APP_TEXT_COLOR,
            fontSize = 13.sp,
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LimitMode.entries.forEach { mode ->
                ToggleChip(
                    text =
                        stringResource(
                            when (mode) {
                                LimitMode.OFF -> R.string.limit_mode_off
                                LimitMode.TIME -> R.string.limit_mode_time
                                LimitMode.VIDEOS -> R.string.limit_mode_videos
                            },
                        ),
                    selected = settings.mode == mode,
                    onClick = { onMode(mode) },
                )
            }
        }

        when (settings.mode) {
            LimitMode.TIME ->
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    WatchLimitPolicy.MINUTE_OPTIONS.forEach { minutes ->
                        ToggleChip(text = minutesLabel(minutes), selected = settings.minutes == minutes, onClick = { onMinutes(minutes) })
                    }
                }
            LimitMode.VIDEOS ->
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    WatchLimitPolicy.VIDEO_OPTIONS.forEach { videos ->
                        ToggleChip(
                            text = stringResource(R.string.limit_videos_option, videos),
                            selected = settings.videos == videos,
                            onClick = { onVideos(videos) },
                        )
                    }
                }
            LimitMode.OFF -> Unit
        }
    }
}

/** 15분, 30분, 1시간, 1시간 30분, 2시간 같은 한도 이름. */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun minutesLabel(minutes: Int): String =
    when {
        minutes < MINUTES_PER_HOUR -> stringResource(R.string.report_time_minutes, minutes.toLong())
        minutes % MINUTES_PER_HOUR == 0 -> stringResource(R.string.limit_option_hours, minutes / MINUTES_PER_HOUR)
        else -> stringResource(R.string.report_time_hours, (minutes / MINUTES_PER_HOUR).toLong(), (minutes % MINUTES_PER_HOUR).toLong())
    }
