package com.sean.ratel.android.data.filter

/** 서버(shortformplay_v2 config)와 같은 한도. 넘기면 서버가 422 로 거절하므로 올리기 전에 맞춘다. */
object FilterLimits {
    const val TOPICS_MAX = 50
    const val KEYWORDS_MAX = 100
    const val KEYWORD_LEN_MAX = 50
    const val CHANNELS_MAX = 500
    const val EXCEPTIONS_MAX = 500
    const val ID_LEN_MAX = 64

    private val ID_REGEX = Regex("^[A-Za-z0-9_.\\-]{1,$ID_LEN_MAX}$")
    private val WHITESPACE = Regex("\\s+")

    /** 공백 정리, 중복 제거, 잘못된 값 제거, 한도까지만 남긴다. 서버가 거절하지 않을 모양으로 만든다. */
    fun sanitize(filter: ExcludeFilter): ExcludeFilter =
        filter.copy(
            topics = ids(filter.topics, TOPICS_MAX),
            keywords = keywords(filter.keywords),
            blockedChannels = ids(filter.blockedChannels, CHANNELS_MAX),
            exceptions = ids(filter.exceptions, EXCEPTIONS_MAX),
        )

    private fun ids(
        values: List<String>,
        max: Int,
    ): List<String> =
        values
            .map { it.trim() }
            .filter { ID_REGEX.matches(it) }
            .distinct()
            .take(max)

    private fun keywords(values: List<String>): List<String> {
        val seen = HashSet<String>()
        val out = ArrayList<String>()
        for (raw in values) {
            val keyword = raw.trim().replace(WHITESPACE, " ")
            if (keyword.isEmpty() || keyword.length > KEYWORD_LEN_MAX) continue
            if (keyword.any { it.code < SPACE_CODE }) continue
            if (seen.add(keyword.lowercase())) out += keyword
            if (out.size >= KEYWORDS_MAX) break
        }
        return out
    }

    private const val SPACE_CODE = 32
}

object FilterMerge {
    /**
     * 두 필터를 합친다 — 어느 한쪽에라도 있으면 남는다. [base] 의 순서가 먼저이고 version 도 [base] 것을 쓴다.
     * 다른 기기에서 바뀐 서버 필터와, 오프라인에서 바꾼 내 필터를 잃지 않고 합칠 때 쓴다.
     */
    fun union(
        base: ExcludeFilter,
        other: ExcludeFilter,
    ): ExcludeFilter =
        FilterLimits.sanitize(
            base.copy(
                topics = base.topics + other.topics,
                keywords = base.keywords + other.keywords,
                blockedChannels = base.blockedChannels + other.blockedChannels,
                exceptions = base.exceptions + other.exceptions,
            ),
        )
}
