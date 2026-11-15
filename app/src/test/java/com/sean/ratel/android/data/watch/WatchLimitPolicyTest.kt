package com.sean.ratel.android.data.watch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class WatchLimitPolicyTest {
    private val date = LocalDate.of(2026, 11, 14)
    private val time30 = WatchLimitSettings(LimitMode.TIME, minutes = 30)
    private val videos20 = WatchLimitSettings(LimitMode.VIDEOS, videos = 20)

    private fun day(
        minutes: Int = 0,
        videos: Int = 0,
        extraSeconds: Long = 0L,
    ) = DayWatch(seconds = minutes * 60L + extraSeconds, videos = videos)

    private fun evaluate(
        settings: WatchLimitSettings,
        today: DayWatch?,
        ack: LimitAck? = null,
    ) = WatchLimitPolicy.evaluate(settings, today, ack, date)

    @Test
    fun `off never asks however much was watched`() {
        assertNull(evaluate(WatchLimitSettings(), day(minutes = 600, videos = 999)))
    }

    @Test
    fun `no record for today means nothing to ask`() {
        assertNull(evaluate(time30, null))
    }

    @Test
    fun `time limit asks once the minutes are reached`() {
        assertNull(evaluate(time30, day(minutes = 29, extraSeconds = 59)))
        val prompt = evaluate(time30, day(minutes = 30, videos = 12))
        assertEquals(LimitPrompt(LimitMode.TIME, 1_800L, 12), prompt)
    }

    @Test
    fun `video limit asks once the count is reached`() {
        assertNull(evaluate(videos20, day(videos = 19, minutes = 500)))
        assertEquals(LimitMode.VIDEOS, evaluate(videos20, day(videos = 20))?.mode)
    }

    @Test
    fun `after continuing the next question waits ten more minutes`() {
        val ack = LimitAck(date, seconds = 1_810L, videos = 9)
        assertNull(evaluate(time30, day(minutes = 35), ack))
        assertNull(evaluate(time30, DayWatch(seconds = 1_810L + WatchLimitPolicy.STEP_SECONDS - 1), ack))
        assertEquals(LimitMode.TIME, evaluate(time30, DayWatch(seconds = 1_810L + WatchLimitPolicy.STEP_SECONDS), ack)?.mode)
    }

    @Test
    fun `after continuing the next video question waits five more videos`() {
        val ack = LimitAck(date, seconds = 0L, videos = 20)
        assertNull(evaluate(videos20, day(videos = 24), ack))
        assertEquals(LimitMode.VIDEOS, evaluate(videos20, day(videos = 25), ack)?.mode)
    }

    @Test
    fun `an acknowledgement from another day is ignored`() {
        val yesterday = LimitAck(date.minusDays(1), seconds = 99_999L, videos = 999)
        assertEquals(LimitMode.TIME, evaluate(time30, day(minutes = 30), yesterday)?.mode)
    }

    @Test
    fun `raising the limit after continuing never asks sooner than the new limit`() {
        val ack = LimitAck(date, seconds = 1_800L, videos = 0)
        val raised = WatchLimitSettings(LimitMode.TIME, minutes = 120)
        assertNull(evaluate(raised, day(minutes = 60), ack))
        assertEquals(LimitMode.TIME, evaluate(raised, day(minutes = 120), ack)?.mode)
    }

    @Test
    fun `only the chosen mode counts`() {
        assertNull(evaluate(time30, day(minutes = 10, videos = 500)))
        assertNull(evaluate(videos20, day(minutes = 500, videos = 5)))
    }

    @Test
    fun `unknown option values fall back to the defaults`() {
        assertEquals(WatchLimitPolicy.DEFAULT_MINUTES, WatchLimitPolicy.normalizeMinutes(7))
        assertEquals(WatchLimitPolicy.DEFAULT_MINUTES, WatchLimitPolicy.normalizeMinutes(null))
        assertEquals(60, WatchLimitPolicy.normalizeMinutes(60))
        assertEquals(WatchLimitPolicy.DEFAULT_VIDEOS, WatchLimitPolicy.normalizeVideos(7))
        assertEquals(50, WatchLimitPolicy.normalizeVideos(50))
    }
}
