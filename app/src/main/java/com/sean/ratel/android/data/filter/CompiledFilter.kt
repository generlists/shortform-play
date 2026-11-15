package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.SearchResultModel

/** 영상이 제외된 이유. */
enum class FilterReason { TOPIC, KEYWORD, CHANNEL, DISLIKED, WATCHED }

/** [detail]: 걸린 주제 id / 키워드 / 채널 id / 영상 id. */
data class ExcludeMatch(
    val reason: FilterReason,
    val detail: String,
)

/**
 * 규칙을 미리 정규화·컴파일해 둔 필터. 영상 한 개가 제외 대상인지 [evaluate] 로 판단한다.
 * 아무 규칙도 없으면 [isNoOp] 이고, 이때는 어떤 영상도 거르지 않는다.
 */
class CompiledFilter private constructor(
    private val keywordRules: List<KeywordRule>,
    private val blockedChannels: Set<String>,
    private val exceptions: Set<String>,
    private val dislikedVideoIds: Set<String>,
    private val watchedVideoIds: Set<String>,
) {
    private class KeywordRule(
        val keyword: CompiledKeyword,
        val topicId: String?,
    )

    val isNoOp: Boolean =
        keywordRules.isEmpty() &&
            blockedChannels.isEmpty() &&
            dislikedVideoIds.isEmpty() &&
            watchedVideoIds.isEmpty()

    /** 제외 대상이면 이유를, 아니면 null. 예외(빼지 않기)로 둔 영상은 항상 null. */
    fun evaluate(item: MainShortsModel): ExcludeMatch? =
        evaluate(
            videoId = item.shortsVideoModel?.videoId.orEmpty(),
            channelId = item.shortsChannelModel?.channelId.orEmpty(),
            text = { videoSearchText(item) },
            topicText = { videoTopicText(item) },
        )

    /** 키워드 검색 결과(SearchResultModel)용. 제목과 채널명만 있어서 그 둘에서 찾는다. */
    fun evaluate(result: SearchResultModel): ExcludeMatch? =
        evaluate(
            videoId = result.videoId.orEmpty(),
            channelId = result.channelId.orEmpty(),
            // 검색 결과에는 제목·채널명만 있어서 키워드와 주제가 같은 글자를 본다
            text = { listOf(result.title, result.channelName).joinToString("\n") { TextNormalizer.normalize(it) }.trim() },
            topicText = { listOf(result.title, result.channelName).joinToString("\n") { TextNormalizer.normalize(it) }.trim() },
        )

    private fun evaluate(
        videoId: String,
        channelId: String,
        text: () -> String,
        topicText: () -> String,
    ): ExcludeMatch? {
        if (isNoOp) return null
        if (videoId.isNotEmpty() && videoId in exceptions) return null

        if (channelId.isNotEmpty() && channelId in blockedChannels) {
            return ExcludeMatch(FilterReason.CHANNEL, channelId)
        }
        if (videoId.isNotEmpty()) {
            if (videoId in dislikedVideoIds) return ExcludeMatch(FilterReason.DISLIKED, videoId)
            if (videoId in watchedVideoIds) return ExcludeMatch(FilterReason.WATCHED, videoId)
        }
        if (keywordRules.isEmpty()) return null

        // 사용자가 직접 쓴 키워드는 설명까지, 주제 단어는 설명을 빼고(홍보 문구에 걸리는 오탐이 많다) 찾는다
        val searchable = text()
        if (searchable.isEmpty()) return null
        val topicSearchable by lazy(LazyThreadSafetyMode.NONE) { topicText() }
        for (rule in keywordRules) {
            val target = if (rule.topicId != null) topicSearchable else searchable
            if (rule.keyword.isIn(target)) {
                return if (rule.topicId != null) {
                    ExcludeMatch(FilterReason.TOPIC, rule.topicId)
                } else {
                    ExcludeMatch(FilterReason.KEYWORD, rule.keyword.original)
                }
            }
        }
        return null
    }

    fun keep(item: MainShortsModel): Boolean = evaluate(item) == null

    fun keep(result: SearchResultModel): Boolean = evaluate(result) == null

    /**
     * 문장(예: 주제 이름)에 제외 키워드·주제 단어가 들어 있는지. 영상 규칙(채널, 싫어요, 본 영상)은 보지 않는다.
     * 주제 탭 자체가 제외 대상인지 판단할 때 쓴다.
     */
    fun matchesText(vararg texts: String?): Boolean {
        if (keywordRules.isEmpty()) return false
        val text = texts.joinToString("\n") { TextNormalizer.normalize(it) }
        return keywordRules.any { it.keyword.isIn(text) }
    }

    companion object {
        val NoOp: CompiledFilter = compile(ExcludeFilter(), FilterOptions(), FilterSignals(), emptyMap())

        /**
         * @param topicTerms 주제 id → 그 주제의 단어 목록(사용자 언어에 맞게 이미 풀어 둔 것)
         */
        fun compile(
            filter: ExcludeFilter,
            options: FilterOptions,
            signals: FilterSignals,
            topicTerms: Map<String, List<String>>,
        ): CompiledFilter {
            val rules = mutableListOf<KeywordRule>()
            // 사용자가 직접 입력한 키워드를 먼저 — 이유 표시에서 더 구체적이다
            filter.keywords.forEach { raw ->
                CompiledKeyword.of(raw)?.let { rules += KeywordRule(it, null) }
            }
            filter.topics.forEach { topicId ->
                topicTerms[topicId].orEmpty().forEach { raw ->
                    CompiledKeyword.of(raw)?.let { rules += KeywordRule(it, topicId) }
                }
            }
            return CompiledFilter(
                keywordRules = rules,
                blockedChannels = filter.blockedChannels.toSet(),
                exceptions = filter.exceptions.toSet(),
                dislikedVideoIds = if (options.hideDisliked) signals.dislikedVideoIds else emptySet(),
                watchedVideoIds = if (options.hideWatched) signals.watchedVideoIds else emptySet(),
            )
        }
    }
}

/** 제목, 채널명, 카테고리, 주제, 설명을 정규화해서 한 문장으로 합친다. 사용자가 쓴 키워드 판단에 쓴다. */
internal fun videoSearchText(item: MainShortsModel): String = normalizedJoin(videoTopicParts(item) + item.shortsVideoModel?.description)

/**
 * [videoSearchText] 에서 설명을 뺀 것. 주제 단어 판단과 주제 추천이 쓴다 —
 * 설명에는 "news", "official" 같은 홍보 문구가 많아서 주제 단어가 엉뚱한 영상에 걸린다.
 */
internal fun videoTopicText(item: MainShortsModel): String = normalizedJoin(videoTopicParts(item))

private fun videoTopicParts(item: MainShortsModel): List<String?> {
    val video = item.shortsVideoModel
    return listOf(video?.title, item.shortsChannelModel?.channelTitle, video?.categoryName, video?.topicName, video?.category)
}

private fun normalizedJoin(parts: List<String?>): String = parts.joinToString("\n") { TextNormalizer.normalize(it) }.trim()
