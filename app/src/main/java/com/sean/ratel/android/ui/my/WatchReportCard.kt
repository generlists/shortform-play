package com.sean.ratel.android.ui.my

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.data.watch.WeeklyReport
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.BarBorder
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60L
private val BAR_MAX_HEIGHT = 64.dp
private val BAR_MIN_HEIGHT = 4.dp

/**
 * 마이 탭의 "이번 주 시청 리포트"(시안 04) — 기간, 시청 시간과 지난주 대비, 본 쇼츠 수, 요일별 막대, 많이 본 채널.
 * 기록은 기기에만 있다.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun WatchReportCard(
    report: WeeklyReport,
    modifier: Modifier = Modifier,
) {
    val locale = remember { Locale.getDefault() }
    SectionCard(modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.report_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(rangeText(report), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 12.sp)
        }
        if (!report.hasData) {
            Text(stringResource(R.string.report_empty), color = Color(0xFFC9CBD0), fontSize = 13.sp, lineHeight = 20.sp)
            return@SectionCard
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    formatWatchTime(report.totalSeconds),
                    color = APP_TEXT_COLOR,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                // 지난주 기록이 있으면 "지난주 대비 ±N분", 없으면 "시청 시간" 라벨(오른쪽 "본 쇼츠"와 줄을 맞춘다)
                val change = report.changeMinutes
                val text =
                    when {
                        change == null -> stringResource(R.string.report_time_label)
                        change > 0 -> stringResource(R.string.report_vs_more, change)
                        change < 0 -> stringResource(R.string.report_vs_less, -change)
                        else -> stringResource(R.string.report_vs_same)
                    }
                Text(text, color = APP_SUBTITLE_TEXT_COLOR, fontSize = 12.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(R.string.report_videos_count, report.videos),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(stringResource(R.string.report_videos_label), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 12.sp)
            }
        }

        DayBars(report, locale)

        if (report.topChannels.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.report_top_channels), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 13.sp)
                report.topChannels.forEachIndexed { index, channel ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${index + 1}",
                            color = APP_TEXT_COLOR,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(20.dp),
                        )
                        Text(
                            channel.name.ifBlank { channel.id },
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            stringResource(R.string.report_channel_count, channel.count),
                            color = APP_SUBTITLE_TEXT_COLOR,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
    }
}

/** "11.8 – 11.14" 같은 리포트 기간. */
private fun rangeText(report: WeeklyReport): String {
    val format = DateTimeFormatter.ofPattern("M.d")
    return "${report.firstDay.format(format)} – ${report.lastDay.format(format)}"
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun DayBars(
    report: WeeklyReport,
    locale: Locale,
) {
    val max = report.maxDaySeconds.coerceAtLeast(1L)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        report.days.forEachIndexed { index, (date, seconds) ->
            val isToday = index == report.days.lastIndex
            val barHeight =
                if (seconds <= 0L) BAR_MIN_HEIGHT else (BAR_MAX_HEIGHT * (seconds.toFloat() / max)).coerceAtLeast(BAR_MIN_HEIGHT)
            val barColor =
                when {
                    seconds <= 0L -> BarBorder
                    isToday -> APP_TEXT_COLOR
                    else -> Color(0xFF7FA83A)
                }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier
                        .padding(horizontal = 2.dp)
                        .width(24.dp)
                        .height(barHeight)
                        .background(color = barColor, shape = RoundedCornerShape(6.dp)),
                )
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                    color = if (isToday) Color.White else APP_SUBTITLE_TEXT_COLOR,
                    fontSize = 12.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
internal fun formatWatchTime(seconds: Long): String {
    val minutes = seconds / SECONDS_PER_MINUTE
    return when {
        minutes < 1L -> stringResource(R.string.report_time_under_minute)
        minutes < MINUTES_PER_HOUR -> stringResource(R.string.report_time_minutes, minutes)
        else -> stringResource(R.string.report_time_hours, minutes / MINUTES_PER_HOUR, minutes % MINUTES_PER_HOUR)
    }
}
