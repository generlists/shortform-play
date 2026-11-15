package com.sean.ratel.android.data.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TopicPackTest {
    private val json =
        """
        {"version": 3, "topics": [
          {"id": "mukbang", "names": {"ko": "먹방", "en": "Mukbang"},
           "terms": {"ko": ["먹방"], "en": ["mukbang", "eating show"], "ja": ["モッパン"]}},
          {"id": "politics", "names": {"en": "Politics"}, "terms": {"ko": ["정치"]}},
          {"id": "", "names": {}, "terms": {}},
          "garbage"
        ]}
        """.trimIndent()

    @Test
    fun `parse reads valid topics and skips broken entries`() {
        val set = TopicPackParser.parse(json)
        assertEquals(3, set.version)
        assertEquals(listOf("mukbang", "politics"), set.topics.map { it.id })
    }

    @Test
    fun `broken or empty json gives an empty set`() {
        assertTrue(TopicPackParser.parse(null).topics.isEmpty())
        assertTrue(TopicPackParser.parse("").topics.isEmpty())
        assertTrue(TopicPackParser.parse("{ not json").topics.isEmpty())
        assertTrue(TopicPackParser.parse("[1,2]").topics.isEmpty())
        assertTrue(TopicPackParser.parse("""{"topics": "nope"}""").topics.isEmpty())
    }

    @Test
    fun `termsFor resolves the requested languages and skips unknown topics`() {
        val set = TopicPackParser.parse(json)
        val terms = set.termsFor(listOf("mukbang", "politics", "missing"), listOf("ko", "en"))
        assertEquals(listOf("먹방", "mukbang", "eating show"), terms["mukbang"])
        assertEquals(listOf("정치"), terms["politics"])
        assertTrue("missing" !in terms)
        // 요청한 언어에 단어가 없으면 그 주제는 비워 둔다
        assertTrue("politics" !in set.termsFor(listOf("politics"), listOf("en")))
    }

    @Test
    fun `nameOf falls back to english then to the id`() {
        val set = TopicPackParser.parse(json)
        assertEquals("먹방", set.nameOf("mukbang", "ko"))
        assertEquals("Mukbang", set.nameOf("mukbang", "fr"))
        assertEquals("Politics", set.nameOf("politics", "ko"))
        assertEquals("unknown_id", set.nameOf("unknown_id", "ko"))
    }

    @Test
    fun `languages follow the selected country`() {
        assertEquals(listOf("ko", "en"), FilterLanguages.forCountry("KR"))
        assertEquals(listOf("ja", "en"), FilterLanguages.forCountry("jp"))
        assertEquals(listOf("zh", "en"), FilterLanguages.forCountry("TW"))
        assertEquals(listOf("th", "en"), FilterLanguages.forCountry("TH"))
        assertEquals(listOf("id", "en"), FilterLanguages.forCountry("ID"))
        assertEquals(listOf("en"), FilterLanguages.forCountry("US"))
        assertEquals(listOf("en"), FilterLanguages.forCountry("CA"))
        assertEquals(listOf("en"), FilterLanguages.forCountry(null))
    }

    @Test
    fun `bundled topics json is valid`() {
        // 단위 테스트의 작업 폴더는 app 모듈이다
        val file = File("src/main/assets/topics.json")
        assertTrue("assets/topics.json 이 없다: ${file.absolutePath}", file.exists())
        val set = TopicPackParser.parse(file.readText())

        assertTrue(set.topics.isNotEmpty())
        assertEquals("주제 id 가 중복됐다", set.topics.size, set.topics.map { it.id }.toSet().size)
        set.topics.forEach { topic ->
            assertTrue("${topic.id}: 한국어 이름 없음", topic.names["ko"].orEmpty().isNotBlank())
            assertTrue("${topic.id}: 영어 이름 없음", topic.names["en"].orEmpty().isNotBlank())
            listOf("ko", "en").forEach { lang ->
                assertTrue("${topic.id}: $lang 단어 없음", topic.terms[lang].orEmpty().isNotEmpty())
            }
            topic.terms.forEach { (lang, terms) ->
                terms.forEach { term ->
                    assertTrue("${topic.id}/$lang: 너무 짧은 단어 '$term'", CompiledKeyword.of(term) != null)
                }
                assertEquals("${topic.id}/$lang: 중복 단어", terms.size, terms.map { TextNormalizer.normalize(it) }.toSet().size)
            }
        }
    }

    @Test
    fun `bundled topics catch the examples they exist for`() {
        val set = TopicPackParser.parse(File("src/main/assets/topics.json").readText())
        val terms = set.termsFor(set.topics.map { it.id }, listOf("ko", "en"))
        val filter =
            CompiledFilter.compile(
                ExcludeFilter(topics = set.topics.map { it.id }),
                FilterOptions(),
                FilterSignals(),
                terms,
            )
        assertEquals(FilterReason.TOPIC, filter.evaluate(video("a", title = "라면 먹방 ASMR"))?.reason)
        assertEquals(FilterReason.TOPIC, filter.evaluate(video("b", title = "Stray Kids comeback stage"))?.reason)
        assertEquals(FilterReason.TOPIC, filter.evaluate(video("c", title = "대통령 기자회견"))?.reason)
        // 일반 영상은 걸리지 않는다
        assertEquals(null, filter.evaluate(video("d", title = "고양이가 문을 여는 방법")))
        assertEquals(null, filter.evaluate(video("e", title = "Live concert vlog")))
    }

    @Test
    fun `bundled topics ignore descriptions and common words that used to collide`() {
        val set = TopicPackParser.parse(File("src/main/assets/topics.json").readText())
        val terms = set.termsFor(set.topics.map { it.id }, listOf("ko", "en"))
        val filter =
            CompiledFilter.compile(ExcludeFilter(topics = set.topics.map { it.id }), FilterOptions(), FilterSignals(), terms)
        // 설명의 홍보 문구("news", "president …")에는 주제가 걸리지 않는다
        val promo = "Latest news from the president of our label, election of the month"
        assertNull(filter.evaluate(video("a", title = "MV Teaser", description = promo)))
        // 일반어와 겹치던 단어
        assertNull(filter.evaluate(video("b", title = "I've never done this twice")))
        assertNull(filter.evaluate(video("c", title = "서프라이즈 파티 아이브로우 메이크업 에스파냐 여행")))
        // 그래도 제목에 있으면 걸린다
        assertEquals(FilterReason.TOPIC, filter.evaluate(video("d", title = "오늘의 속보 정리"))?.reason)
        assertEquals(FilterReason.TOPIC, filter.evaluate(video("e", title = "x", channelTitle = "G1 News"))?.reason)
    }

    @Test
    fun `user keywords still search the description but topics do not`() {
        val desc = video("a", title = "그냥 영상", description = "정치 이야기 포함")
        assertEquals(FilterReason.KEYWORD, compile(ExcludeFilter(keywords = listOf("정치"))).evaluate(desc)?.reason)
        val topic = compile(ExcludeFilter(topics = listOf("politics")), topicTerms = mapOf("politics" to listOf("정치")))
        assertNull(topic.evaluate(desc))
        assertEquals(FilterReason.TOPIC, topic.evaluate(video("b", title = "정치 이야기"))?.reason)
    }
}
