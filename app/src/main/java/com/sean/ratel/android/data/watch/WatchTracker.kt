package com.sean.ratel.android.data.watch

import java.time.LocalDate

/**
 * "지금 실제로 재생 중인 영상"의 시간을 잰다. 여러 화면(영상마다 프래그먼트)이 상태를 알려 오므로,
 * 재생 중이라고 알린 영상 하나만 센다 — 다른 영상이 재생되기 시작하면 이전 영상은 거기서 끝난다.
 *
 * - 시계는 [nowMs] 로 받는다(기기에서는 elapsedRealtime). 테스트에서는 가짜 시계를 쓴다.
 * - 기록은 [emit] 으로 내보낸다. 영상이 멈출 때, 그리고 재생이 길면 [tick] 마다 나눠서 내보낸다(앱이 죽어도 대부분 남도록).
 * - "본 영상"은 [MIN_VIEW_SECONDS] 이상 봐야 센다. 같은 영상을 멈췄다 다시 재생하거나 반복 재생해도 다른 영상으로 넘어가기 전까지는 한 번만 센다.
 */
class WatchTracker(
    private val nowMs: () -> Long,
    private val today: () -> LocalDate,
    private val emit: (WatchDelta) -> Unit,
) {
    private var activeId: String? = null
    private var lastId: String? = null
    private var activeChannel = ChannelWatch()
    private var segmentStartMs = 0L
    private var videoSeconds = 0L
    private var counted = false

    @Synchronized
    fun update(
        videoId: String,
        channelId: String,
        channelName: String,
        playing: Boolean,
    ) {
        if (videoId.isEmpty()) return
        if (playing) {
            if (activeId == videoId) return
            stopInternal()
            activeId = videoId
            activeChannel = ChannelWatch(channelId, channelName)
            segmentStartMs = nowMs()
            // 멈췄다 같은 영상을 이어 보는 것이면 본 시간과 "봤다" 표시를 이어 간다
            if (videoId != lastId) {
                videoSeconds = 0L
                counted = false
            }
            lastId = videoId
        } else if (activeId == videoId) {
            // 다른 영상이 재생 중일 때 오는 "멈춤" 알림은 무시한다
            stopInternal()
        }
    }

    /** 재생이 이어지는 동안 가끔 불러 준다. 일정 시간이 쌓였으면 그만큼 기록하고 다시 센다. */
    @Synchronized
    fun tick() {
        if (activeId == null) return
        if (nowMs() - segmentStartMs >= TICK_MS) flush()
    }

    /** [videoId] 가 지금 세고 있는 영상이면(또는 null 이면 무엇이든) 멈춘다. */
    @Synchronized
    fun stop(videoId: String? = null) {
        if (videoId == null || videoId == activeId) stopInternal()
    }

    private fun stopInternal() {
        if (activeId == null) return
        flush()
        activeId = null
    }

    private fun flush() {
        val now = nowMs()
        val elapsed = ((now - segmentStartMs) / MS_PER_SECOND).coerceIn(0L, MAX_SEGMENT_SECONDS)
        segmentStartMs = now
        videoSeconds += elapsed
        val view = if (!counted && videoSeconds >= MIN_VIEW_SECONDS) activeChannel.also { counted = true } else null
        if (elapsed > 0L || view != null) emit(WatchDelta(today(), elapsed, view))
    }

    companion object {
        const val MIN_VIEW_SECONDS = 3L
        const val TICK_MS = 15_000L
        const val MAX_SEGMENT_SECONDS = 2L * 60 * 60 // 이상한 값(시계 변경 등)을 막는 상한
        private const val MS_PER_SECOND = 1_000L
    }
}
