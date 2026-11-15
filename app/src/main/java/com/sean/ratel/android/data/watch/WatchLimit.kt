package com.sean.ratel.android.data.watch

import java.time.LocalDate

/** 하루 시청 한도의 종류. [OFF] 가 기본 — 켜지 않으면 기록만 쌓고 아무것도 묻지 않는다. */
enum class LimitMode { OFF, TIME, VIDEOS }

/** 사용자가 정한 한도. 기기에만 저장한다. */
data class WatchLimitSettings(
    val mode: LimitMode = LimitMode.OFF,
    val minutes: Int = WatchLimitPolicy.DEFAULT_MINUTES,
    val videos: Int = WatchLimitPolicy.DEFAULT_VIDEOS,
)

/** "계속 볼까요?"에 "계속 보기"를 누른 시점(그날의 시간·영상 수). 다음 질문은 여기서 한 걸음 더 간 뒤에 한다. */
data class LimitAck(
    val date: LocalDate,
    val seconds: Long,
    val videos: Int,
)

/** 지금 물어야 한다는 신호. 값은 질문에 보여 줄 오늘의 시청 시간·영상 수. */
data class LimitPrompt(
    val mode: LimitMode,
    val seconds: Long,
    val videos: Int,
)

/** 한도에 닿았는지 판단한다. 순수 계산이라 기기 없이 테스트한다. */
object WatchLimitPolicy {
    val MINUTE_OPTIONS = listOf(15, 30, 60, 90, 120)
    val VIDEO_OPTIONS = listOf(10, 20, 30, 50, 100)
    const val DEFAULT_MINUTES = 30
    const val DEFAULT_VIDEOS = 20

    /** "계속 보기"를 누른 뒤 다시 묻기까지: 시간 한도는 10분, 영상 한도는 5개 더. */
    const val STEP_SECONDS = 10L * 60
    const val STEP_VIDEOS = 5

    private const val SECONDS_PER_MINUTE = 60L

    fun normalizeMinutes(value: Int?): Int = if (value in MINUTE_OPTIONS) value!! else DEFAULT_MINUTES

    fun normalizeVideos(value: Int?): Int = if (value in VIDEO_OPTIONS) value!! else DEFAULT_VIDEOS

    /**
     * @param today 오늘의 기록(없으면 null) @param ack 마지막 "계속 보기" (다른 날이면 무시)
     * @return 지금 물어야 하면 [LimitPrompt], 아니면 null
     */
    fun evaluate(
        settings: WatchLimitSettings,
        today: DayWatch?,
        ack: LimitAck?,
        date: LocalDate,
    ): LimitPrompt? {
        val day = today ?: return null
        val acked = ack?.takeIf { it.date == date }
        val reached =
            when (settings.mode) {
                LimitMode.OFF -> false
                LimitMode.TIME -> {
                    val limit = settings.minutes * SECONDS_PER_MINUTE
                    day.seconds >= maxOf(limit, acked?.let { it.seconds + STEP_SECONDS } ?: limit)
                }
                LimitMode.VIDEOS -> {
                    val limit = settings.videos
                    day.videos >= maxOf(limit, acked?.let { it.videos + STEP_VIDEOS } ?: limit)
                }
            }
        return if (reached) LimitPrompt(settings.mode, day.seconds, day.videos) else null
    }
}
