package com.sean.ratel.android.data.watch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeeklyReportTest {
    private val today = LocalDate.of(2026, 11, 14)

    private fun day(
        ago: Int,
        seconds: Long,
        videos: Int = 1,
        channels: List<ChannelWatch> = emptyList(),
    ) = today.minusDays(ago.toLong()) to DayWatch(seconds, videos, channels)

    @Test
    fun `an empty log gives seven zero days and no data`() {
        val report = WeeklyReport.build(emptyMap(), today)
        assertEquals(7, report.days.size)
        assertEquals(today.minusDays(6), report.days.first().first)
        assertEquals(today, report.days.last().first)
        assertFalse(report.hasData)
        assertNull(report.changeMinutes)
        assertEquals(0, report.activeDays)
        assertEquals(0L, report.maxDaySeconds)
    }

    @Test
    fun `totals cover the last seven days and compare with the seven before`() {
        val data =
            mapOf(
                day(0, 600, videos = 4),
                day(3, 1_200, videos = 6),
                day(6, 600, videos = 2),
                // 직전 7일
                day(7, 1_200),
                day(13, 1_200),
                // 범위 밖
                day(14, 9_999),
            )
        val report = WeeklyReport.build(data, today)
        assertEquals(2_400L, report.totalSeconds)
        assertEquals(12, report.videos)
        assertEquals(3, report.activeDays)
        assertEquals(2_400L, report.previousSeconds)
        assertEquals(0, report.changeMinutes)
        assertEquals(1_200L, report.maxDaySeconds)
        assertTrue(report.hasData)
    }

    @Test
    fun `change in minutes can be positive or negative`() {
        val up = WeeklyReport.build(mapOf(day(0, 1_500), day(8, 1_000)), today)
        assertEquals(8, up.changeMinutes) // 500초 = 8.3분
        val down = WeeklyReport.build(mapOf(day(0, 500), day(8, 1_000)), today)
        assertEquals(-8, down.changeMinutes)
        assertNull(WeeklyReport.build(mapOf(day(0, 500)), today).changeMinutes)
    }

    @Test
    fun `hidden counts are summed over the seven days only`() {
        val data =
            mapOf(
                today to DayWatch(hidden = 5),
                today.minusDays(3) to DayWatch(seconds = 10, hidden = 7),
                today.minusDays(8) to DayWatch(hidden = 99),
            )
        val report = WeeklyReport.build(data, today)
        assertEquals(12, report.hiddenCount)
        assertEquals(today.minusDays(6), report.firstDay)
        assertEquals(today, report.lastDay)
    }

    @Test
    fun `top channels are merged across days and limited to three`() {
        val data =
            mapOf(
                day(0, 10, channels = listOf(ChannelWatch("a", "A", 2), ChannelWatch("b", "B", 1))),
                day(1, 10, channels = listOf(ChannelWatch("a", "A", 1), ChannelWatch("c", "C", 5), ChannelWatch("d", "D", 1))),
                day(2, 10, channels = listOf(ChannelWatch("e", "E", 1))),
                // 직전 주는 제외
                day(9, 10, channels = listOf(ChannelWatch("z", "Z", 99))),
            )
        val top = WeeklyReport.build(data, today).topChannels
        assertEquals(listOf("c" to 5, "a" to 3), top.take(2).map { it.id to it.count })
        assertEquals(3, top.size)
    }
}
