package com.sean.ratel.android.data.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FilterSuggestionsTest {
    private val packs =
        TopicPackParser.parse(
            """{"topics":[
              {"id":"mukbang","names":{"ko":"먹방","en":"Mukbang"},"terms":{"ko":["먹방","리얼사운드"],"en":["mukbang"]}},
              {"id":"politics","names":{"ko":"정치","en":"Politics"},"terms":{"ko":["정치","대통령","국회"],"en":["politics"]}}
            ]}""",
        )
    private val langs = listOf("ko", "en")

    @Test
    fun `topics that match the video are suggested, best match first`() {
        val v = video("a", title = "대통령 국회 연설 정치 이야기", channelTitle = "뉴스")
        assertEquals(listOf("politics"), TopicSuggester.suggest(v, packs, langs))

        val both = video("b", title = "정치 먹방 리얼사운드")
        assertEquals(listOf("mukbang", "politics"), TopicSuggester.suggest(both, packs, langs))
    }

    @Test
    fun `no match or no text means no suggestion`() {
        assertTrue(TopicSuggester.suggest(video("c", title = "고양이"), packs, langs).isEmpty())
        assertTrue(TopicSuggester.suggest(video("d", title = ""), TopicPackSet(), langs).isEmpty())
    }

    @Test
    fun `suggestions are limited`() {
        val many =
            TopicPackParser.parse(
                """{"topics":[""" + (1..6).joinToString(",") { """{"id":"t$it","names":{},"terms":{"en":["alpha"]}}""" } + "]}",
            )
        assertEquals(3, TopicSuggester.suggest(video("e", title = "alpha beta"), many, langs).size)
        assertEquals(2, TopicSuggester.suggest(video("e", title = "alpha beta"), many, langs, max = 2).size)
    }

    @Test
    fun `bundled topics suggest something for a typical video`() {
        val set = TopicPackParser.parse(File("src/main/assets/topics.json").readText())
        val ids = TopicSuggester.suggest(video("f", title = "라면 먹방 ASMR"), set, langs)
        assertEquals(listOf("mukbang"), ids)
    }

    @Test
    fun `keywords come from the title without noise`() {
        assertEquals(
            listOf("라면", "먹방", "ASMR"),
            KeywordSuggester.fromTitle("라면 먹방 ASMR 쇼츠 2026 #shorts"),
        )
        assertEquals(listOf("Spicy", "noodle", "challenge"), KeywordSuggester.fromTitle("The Spicy noodle, challenge! the"))
    }

    @Test
    fun `keywords are deduped and capped`() {
        assertEquals(listOf("aaa", "bbb"), KeywordSuggester.fromTitle("aaa AAA bbb Bbb"))
        assertEquals(5, KeywordSuggester.fromTitle("a1 b2 c3 d4 e5 f6 g7 h8", max = 5).size.coerceAtMost(5))
        assertEquals(emptyList<String>(), KeywordSuggester.fromTitle(null))
        assertEquals(emptyList<String>(), KeywordSuggester.fromTitle("  "))
        assertEquals(emptyList<String>(), KeywordSuggester.fromTitle("a 1 22"))
    }

    @Test
    fun `frequent words across titles are ranked by count and skip picked keywords`() {
        val titles =
            listOf(
                "남돌 직캠 무대",
                "직캠 리액션 모음",
                "리액션 직캠 2026",
                "고양이 브이로그",
                null,
                "   ",
            )
        assertEquals(listOf("직캠", "리액션"), KeywordSuggester.frequent(titles))
        assertEquals(listOf("리액션"), KeywordSuggester.frequent(titles, exclude = listOf("직캠")))
        assertEquals(listOf("직캠"), KeywordSuggester.frequent(titles, max = 1))
        // 한 제목에 같은 단어가 여러 번 나와도 한 번만 센다
        assertEquals(emptyList<String>(), KeywordSuggester.frequent(listOf("직캠 직캠 직캠")))
        assertEquals(emptyList<String>(), KeywordSuggester.frequent(emptyList()))
    }
}
