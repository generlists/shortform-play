package com.sean.ratel.android.data.watch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WatchLogTest {
    private val d1 = LocalDate.of(2026, 11, 14)

    @Test
    fun `adds seconds videos and channel counts to the day`() {
        var days = emptyMap<String, DayWatch>()
        days = WatchLog.apply(days, WatchDelta(d1, 30, ChannelWatch("c1", "채널1")))
        days = WatchLog.apply(days, WatchDelta(d1, 20, ChannelWatch("c1", "채널1")))
        days = WatchLog.apply(days, WatchDelta(d1, 5, ChannelWatch("c2", "채널2")))
        days = WatchLog.apply(days, WatchDelta(d1, 10, null))
        val day = days.getValue("2026-11-14")
        assertEquals(65L, day.seconds)
        assertEquals(3, day.videos)
        assertEquals(listOf("c1" to 2, "c2" to 1), day.channels.map { it.id to it.count })
    }

    @Test
    fun `a channel without id is merged by name and one without either is skipped`() {
        var days = WatchLog.apply(emptyMap(), WatchDelta(d1, 1, ChannelWatch("", "이름만")))
        days = WatchLog.apply(days, WatchDelta(d1, 1, ChannelWatch("", "이름만")))
        days = WatchLog.apply(days, WatchDelta(d1, 1, ChannelWatch("", "")))
        val day = days.getValue("2026-11-14")
        assertEquals(3, day.videos)
        assertEquals(listOf("이름만" to 2), day.channels.map { it.id to it.count })
    }

    @Test
    fun `negative seconds never reduce the total`() {
        val days = WatchLog.apply(mapOf("2026-11-14" to DayWatch(seconds = 50)), WatchDelta(d1, -10, null))
        assertEquals(50L, days.getValue("2026-11-14").seconds)
    }

    @Test
    fun `channels per day are capped and keep the most watched`() {
        var days = emptyMap<String, DayWatch>()
        repeat(WatchLog.MAX_CHANNELS_PER_DAY + 5) { i -> days = WatchLog.apply(days, WatchDelta(d1, 1, ChannelWatch("c$i", "n$i"))) }
        days = WatchLog.apply(days, WatchDelta(d1, 1, ChannelWatch("c3", "n3")))
        val channels = days.getValue("2026-11-14").channels
        assertEquals(WatchLog.MAX_CHANNELS_PER_DAY, channels.size)
        assertEquals("c3", channels.first().id)
    }

    @Test
    fun `old days and unreadable keys are pruned`() {
        val old = d1.minusDays(WatchLog.MAX_DAYS + 1L).toString()
        val edge = d1.minusDays(WatchLog.MAX_DAYS.toLong()).toString()
        val days =
            WatchLog.apply(
                mapOf(old to DayWatch(seconds = 1), edge to DayWatch(seconds = 2), "garbage" to DayWatch(seconds = 3)),
                WatchDelta(d1, 1, null),
            )
        assertFalse(old in days)
        assertFalse("garbage" in days)
        assertTrue(edge in days)
        assertTrue("2026-11-14" in days)
    }

    @Test
    fun `hidden ids are merged per day without double counting and reset on a new day`() {
        val first = WatchLog.mergeHidden(null, d1, listOf("a", "b", ""))
        assertEquals(listOf("a", "b"), first.ids)
        val again = WatchLog.mergeHidden(first, d1, listOf("b", "c"))
        assertEquals(listOf("a", "b", "c"), again.ids)
        val nextDay = WatchLog.mergeHidden(again, d1.plusDays(1), listOf("z"))
        assertEquals(listOf("z"), nextDay.ids)
        assertEquals("2026-11-15", nextDay.date)
    }

    @Test
    fun `the hidden id set is capped`() {
        val many = (1..WatchLog.MAX_HIDDEN_IDS + 50).map { "id$it" }
        assertEquals(WatchLog.MAX_HIDDEN_IDS, WatchLog.mergeHidden(null, d1, many).ids.size)
    }

    @Test
    fun `the daily hidden count only grows and keeps the other numbers`() {
        var days = WatchLog.apply(emptyMap(), WatchDelta(d1, 30, ChannelWatch("c", "채널")))
        days = WatchLog.withHidden(days, d1, 4)
        days = WatchLog.withHidden(days, d1, 2)
        val day = days.getValue("2026-11-14")
        assertEquals(4, day.hidden)
        assertEquals(30L, day.seconds)
        assertEquals(1, day.videos)
    }
}
