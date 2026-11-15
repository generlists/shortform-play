package com.sean.ratel.android.data.watch

import androidx.annotation.Keep
import java.time.LocalDate

/** 한 채널을 본 횟수(영상 단위). [id] 가 없으면 이름으로 합친다. */
@Keep
data class ChannelWatch(
    val id: String = "",
    val name: String = "",
    val count: Int = 0,
)

/**
 * 하루치 시청 합계. 영상 목록은 저장하지 않는다 — 시간, 본 영상 수, 채널별 횟수, 필터가 빼고 보여준 영상 수만.
 * [hidden]: 그날 내 필터가 목록에서 뺀 서로 다른 영상의 수("이번 주에 N개를 빼고 보여드렸어요").
 */
@Keep
data class DayWatch(
    val seconds: Long = 0L,
    val videos: Int = 0,
    val channels: List<ChannelWatch> = emptyList(),
    val hidden: Int = 0,
)

/** 오늘 필터가 뺀 영상 id — 같은 영상을 두 번 세지 않으려고 **오늘 것만** 둔다(날짜가 바뀌면 새로 시작). */
@Keep
data class HiddenToday(
    val date: String = "",
    val ids: List<String> = emptyList(),
)

/** 기록 한 번의 변화. [view] 가 있으면 영상 한 편을 "봤다"고 센다(그 영상의 채널). */
data class WatchDelta(
    val date: LocalDate,
    val seconds: Long,
    val view: ChannelWatch?,
)

/** 날짜(yyyy-MM-dd) → 하루 합계. 순수 계산이라 기기 없이 테스트한다. */
object WatchLog {
    const val MAX_DAYS = 90
    const val MAX_CHANNELS_PER_DAY = 20

    fun apply(
        days: Map<String, DayWatch>,
        delta: WatchDelta,
    ): Map<String, DayWatch> {
        val key = delta.date.toString()
        val day = days[key] ?: DayWatch()
        val updated =
            day.copy(
                seconds = day.seconds + delta.seconds.coerceAtLeast(0L),
                videos = day.videos + if (delta.view != null) 1 else 0,
                channels = delta.view?.let { addChannel(day.channels, it) } ?: day.channels,
            )
        return prune(days + (key to updated), delta.date)
    }

    /** 오래된 날(기준일로부터 [MAX_DAYS] 일 전보다 이전)과 읽을 수 없는 키를 지운다. */
    fun prune(
        days: Map<String, DayWatch>,
        today: LocalDate,
    ): Map<String, DayWatch> {
        val oldest = today.minusDays(MAX_DAYS.toLong())
        return days.filterKeys { key -> parseDate(key)?.let { !it.isBefore(oldest) } == true }
    }

    /** 한 날에 세어 둘 id 의 상한(이상한 값으로 저장 공간이 커지지 않게). */
    const val MAX_HIDDEN_IDS = 2_000

    /** 오늘의 id 모음에 [ids] 를 더한다. 저장된 날짜가 오늘이 아니면 버리고 새로 시작한다. */
    fun mergeHidden(
        stored: HiddenToday?,
        date: LocalDate,
        ids: Collection<String>,
    ): HiddenToday {
        val key = date.toString()
        val base = if (stored?.date == key) stored.ids else emptyList()
        val merged = LinkedHashSet(base).apply { ids.filterTo(this) { it.isNotEmpty() } }
        return HiddenToday(key, merged.take(MAX_HIDDEN_IDS))
    }

    /** 그날의 "뺀 영상 수"를 [count] 로 정한다(줄어들지는 않는다 — 한 번 뺀 영상은 그날 센 것으로 둔다). */
    fun withHidden(
        days: Map<String, DayWatch>,
        date: LocalDate,
        count: Int,
    ): Map<String, DayWatch> {
        val key = date.toString()
        val day = days[key] ?: DayWatch()
        return prune(days + (key to day.copy(hidden = maxOf(day.hidden, count))), date)
    }

    fun parseDate(key: String): LocalDate? = runCatching { LocalDate.parse(key) }.getOrNull()

    private fun addChannel(
        channels: List<ChannelWatch>,
        view: ChannelWatch,
    ): List<ChannelWatch> {
        val id = view.id.ifBlank { view.name }
        if (id.isBlank()) return channels
        val merged =
            if (channels.any { it.id == id }) {
                channels.map { if (it.id == id) it.copy(count = it.count + 1) else it }
            } else {
                channels + view.copy(id = id, count = 1)
            }
        return merged.sortedByDescending { it.count }.take(MAX_CHANNELS_PER_DAY)
    }
}
