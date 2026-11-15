package com.sean.ratel.android.data.watch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class WatchTrackerTest {
    private var now = 0L
    private val day = LocalDate.of(2026, 11, 14)
    private val out = mutableListOf<WatchDelta>()
    private lateinit var tracker: WatchTracker

    @Before
    fun setUp() {
        now = 1_000_000L
        out.clear()
        tracker = WatchTracker(nowMs = { now }, today = { day }, emit = { out += it })
    }

    private fun play(
        id: String,
        channel: String = "ch-$id",
    ) = tracker.update(id, channel, "name-$channel", playing = true)

    private fun pause(id: String) = tracker.update(id, "ch-$id", "n", playing = false)

    private fun seconds() = out.sumOf { it.seconds }

    @Test
    fun `plays then pauses counts the seconds and one view`() {
        play("a")
        now += 10_000
        pause("a")
        assertEquals(10L, seconds())
        assertEquals(1, out.count { it.view != null })
        assertEquals(ChannelWatch("ch-a", "name-ch-a"), out.single { it.view != null }.view)
        assertEquals(day, out.first().date)
    }

    @Test
    fun `a video watched under the minimum is not counted as viewed`() {
        play("a")
        now += 2_000
        pause("a")
        assertEquals(2L, seconds())
        assertEquals(0, out.count { it.view != null })
    }

    @Test
    fun `starting another video ends the previous one there`() {
        play("a")
        now += 5_000
        play("b")
        now += 7_000
        pause("b")
        assertEquals(12L, seconds())
        assertEquals(2, out.count { it.view != null })
    }

    @Test
    fun `a pause notice for a video that is not the active one is ignored`() {
        play("b")
        now += 4_000
        pause("a") // 화면 밖 영상의 알림
        now += 4_000
        pause("b")
        assertEquals(8L, seconds())
    }

    @Test
    fun `pausing and resuming the same video counts one view`() {
        play("a")
        now += 4_000
        play("a") // 같은 영상이 다시 재생 중이라고 알려도 이어서 센다
        now += 4_000
        pause("a")
        play("a")
        now += 6_000
        pause("a")
        assertEquals(14L, seconds())
        assertEquals(1, out.count { it.view != null })
    }

    @Test
    fun `resuming counts the earlier seconds toward the minimum`() {
        play("a")
        now += 2_000
        pause("a")
        play("a")
        now += 2_000
        pause("a")
        assertEquals(4L, seconds())
        assertEquals(1, out.count { it.view != null })
    }

    @Test
    fun `going back to a video after another one counts it again`() {
        play("a")
        now += 5_000
        play("b")
        now += 5_000
        play("a")
        now += 5_000
        pause("a")
        assertEquals(3, out.count { it.view != null })
    }

    @Test
    fun `tick saves long playback in pieces without losing time`() {
        play("a")
        now += 10_000
        tracker.tick() // 아직 15초가 안 됐다
        assertEquals(0, out.size)
        now += 6_000
        tracker.tick()
        assertEquals(16L, seconds())
        now += 5_000
        pause("a")
        assertEquals(21L, seconds())
        assertEquals(1, out.count { it.view != null }) // 조각으로 나눠도 영상은 한 번
    }

    @Test
    fun `stop without an id ends whatever is playing and a second stop does nothing`() {
        play("a")
        now += 9_000
        tracker.stop()
        tracker.stop()
        assertEquals(9L, seconds())
    }

    @Test
    fun `an absurd gap is capped`() {
        play("a")
        now += 10L * 60 * 60 * 1000
        pause("a")
        assertEquals(WatchTracker.MAX_SEGMENT_SECONDS, seconds())
    }

    @Test
    fun `blank video ids and idle updates do nothing`() {
        tracker.update("", "c", "n", playing = true)
        pause("a")
        tracker.tick()
        assertEquals(0, out.size)
        assertNull(out.firstOrNull())
    }
}
