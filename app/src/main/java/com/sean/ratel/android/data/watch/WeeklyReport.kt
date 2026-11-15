package com.sean.ratel.android.data.watch

import java.time.LocalDate

/**
 * 주간 시청 리포트 — 오늘까지 최근 7일을 직전 7일과 비교한다(달력 주가 아니라 "최근 7일").
 * [days] 는 오래된 날 → 오늘 순서로 7개(기록이 없는 날은 0초).
 */
data class WeeklyReport(
    val days: List<Pair<LocalDate, Long>>,
    val totalSeconds: Long,
    val videos: Int,
    val activeDays: Int,
    val previousSeconds: Long,
    val topChannels: List<ChannelWatch>,
    val hiddenCount: Int = 0,
) {
    val hasData: Boolean get() = totalSeconds > 0L || videos > 0

    /** 직전 7일보다 몇 분 늘고 줄었는지(반올림). 비교할 기록이 없으면 null. */
    val changeMinutes: Int?
        get() = if (previousSeconds > 0L) Math.round((totalSeconds - previousSeconds) / SECONDS_PER_MINUTE).toInt() else null

    val firstDay: LocalDate get() = days.first().first
    val lastDay: LocalDate get() = days.last().first

    val maxDaySeconds: Long get() = days.maxOfOrNull { it.second } ?: 0L

    companion object {
        const val DAYS = 7
        const val TOP_CHANNELS = 3
        private const val SECONDS_PER_MINUTE = 60.0

        val Empty = build(emptyMap(), LocalDate.now())

        fun build(
            data: Map<LocalDate, DayWatch>,
            today: LocalDate,
        ): WeeklyReport {
            val current = (DAYS - 1 downTo 0).map { today.minusDays(it.toLong()) }
            val previous = (2 * DAYS - 1 downTo DAYS).map { today.minusDays(it.toLong()) }
            val perDay = current.map { it to (data[it]?.seconds ?: 0L) }
            val channels =
                current
                    .flatMap { data[it]?.channels.orEmpty() }
                    .groupBy { it.id }
                    .map { (_, list) -> list.first().copy(count = list.sumOf { it.count }) }
                    .sortedByDescending { it.count }
                    .take(TOP_CHANNELS)
            return WeeklyReport(
                days = perDay,
                totalSeconds = perDay.sumOf { it.second },
                videos = current.sumOf { data[it]?.videos ?: 0 },
                activeDays = perDay.count { it.second > 0L },
                previousSeconds = previous.sumOf { data[it]?.seconds ?: 0L },
                topChannels = channels,
                hiddenCount = current.sumOf { data[it]?.hidden ?: 0 },
            )
        }
    }
}
