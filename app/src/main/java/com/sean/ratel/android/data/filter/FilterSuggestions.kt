package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.MainShortsModel

/**
 * "관심없음" 시트가 보여 주는 추천. 사용자는 이 중에서 골라 내 필터에 추가한다.
 */
object TopicSuggester {
    /**
     * 이 영상에 걸리는 주제 id. 주제 묶음의 단어가 영상 글자에 많이 나올수록 앞에 둔다(최대 [max]개).
     * 영상이 어느 주제에도 안 걸리면 빈 목록.
     */
    fun suggest(
        item: MainShortsModel,
        packs: TopicPackSet,
        languages: List<String>,
        max: Int = DEFAULT_MAX_TOPICS,
    ): List<String> {
        val text = videoTopicText(item)
        if (text.isEmpty()) return emptyList()
        val terms = packs.termsFor(packs.topics.map { it.id }, languages)
        return terms
            .mapValues { (_, words) -> words.mapNotNull { CompiledKeyword.of(it) }.count { it.isIn(text) } }
            .filterValues { it > 0 }
            .entries
            .sortedByDescending { it.value }
            .take(max)
            .map { it.key }
    }

    private const val DEFAULT_MAX_TOPICS = 3
}

object KeywordSuggester {
    private val SPLIT = Regex("[^\\p{L}\\p{N}]+")
    private val STOP_WORDS =
        setOf(
            "the", "and", "for", "with", "from", "this", "that", "you", "your", "are", "was", "how", "what", "why",
            "shorts", "short", "video", "official", "new",
            "쇼츠", "영상", "오늘", "진짜", "정말", "너무", "그리고",
        )

    /** 제목에서 뺄 만한 단어 후보(등장 순서, 중복 제거, 한 글자·숫자뿐인 단어·흔한 단어 제외). 최대 [max]개. */
    fun fromTitle(
        title: String?,
        max: Int = DEFAULT_MAX_KEYWORDS,
    ): List<String> {
        if (title.isNullOrBlank()) return emptyList()
        val seen = HashSet<String>()
        val out = ArrayList<String>()
        for (raw in title.split(SPLIT)) {
            val word = raw.trim()
            if (word.length < CompiledKeyword.MIN_LENGTH) continue
            if (word.all { it.isDigit() }) continue
            val key = TextNormalizer.normalize(word)
            if (key in STOP_WORDS || !seen.add(key)) continue
            out += word
            if (out.size >= max) break
        }
        return out
    }

    /**
     * 여러 제목에서 자주 나오는 단어(온보딩의 "많이 나오는 단어" 추천). 두 번 이상 나온 단어만, 많이 나온 순.
     * [exclude] 에 있는 단어(이미 고른 키워드)는 뺀다.
     */
    fun frequent(
        titles: List<String?>,
        exclude: Collection<String> = emptyList(),
        max: Int = DEFAULT_MAX_FREQUENT,
    ): List<String> {
        val excluded = exclude.map { TextNormalizer.normalize(it) }.toHashSet()
        val counts = LinkedHashMap<String, Int>()
        val original = HashMap<String, String>()
        titles.forEach { title ->
            if (title.isNullOrBlank()) return@forEach
            val inTitle = HashSet<String>()
            for (raw in title.split(SPLIT)) {
                val word = raw.trim()
                if (word.length < CompiledKeyword.MIN_LENGTH || word.all { it.isDigit() }) continue
                val key = TextNormalizer.normalize(word)
                if (key in STOP_WORDS || key in excluded || !inTitle.add(key)) continue
                counts[key] = (counts[key] ?: 0) + 1
                original.putIfAbsent(key, word)
            }
        }
        return counts.entries
            .filter { it.value >= MIN_FREQUENT_COUNT }
            .sortedByDescending { it.value }
            .take(max)
            .map { original.getValue(it.key) }
    }

    private const val DEFAULT_MAX_KEYWORDS = 5
    private const val DEFAULT_MAX_FREQUENT = 8
    private const val MIN_FREQUENT_COUNT = 2
}
