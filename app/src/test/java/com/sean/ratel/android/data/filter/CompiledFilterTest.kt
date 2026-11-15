package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.MainShortsModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompiledFilterTest {
    @Test
    fun `default filter is a no-op and keeps everything`() {
        val filter = compile()
        assertTrue(filter.isNoOp)
        assertTrue(CompiledFilter.NoOp.isNoOp)
        assertNull(filter.evaluate(video("v1", title = "먹방 직캠")))
    }

    @Test
    fun `blocked channel is excluded`() {
        val filter = compile(ExcludeFilter(blockedChannels = listOf("UCbad")))
        assertEquals(
            ExcludeMatch(FilterReason.CHANNEL, "UCbad"),
            filter.evaluate(video("v1", channelId = "UCbad")),
        )
        assertNull(filter.evaluate(video("v2", channelId = "UCok")))
    }

    @Test
    fun `keyword matches title description channel title and category`() {
        val filter = compile(ExcludeFilter(keywords = listOf("직캠")))
        assertEquals(ExcludeMatch(FilterReason.KEYWORD, "직캠"), filter.evaluate(video("a", title = "무대 직캠")))
        assertEquals(ExcludeMatch(FilterReason.KEYWORD, "직캠"), filter.evaluate(video("b", description = "오늘의 직캠 모음")))
        assertEquals(ExcludeMatch(FilterReason.KEYWORD, "직캠"), filter.evaluate(video("c", channelTitle = "직캠TV")))
        assertEquals(ExcludeMatch(FilterReason.KEYWORD, "직캠"), filter.evaluate(video("d", categoryName = "직캠")))
        assertEquals(ExcludeMatch(FilterReason.KEYWORD, "직캠"), filter.evaluate(video("e", topicName = "직캠 채널")))
        assertNull(filter.evaluate(video("f", title = "요리 레시피")))
    }

    @Test
    fun `topic terms report the topic id`() {
        val filter =
            compile(
                ExcludeFilter(topics = listOf("mukbang", "politics")),
                topicTerms = mapOf("mukbang" to listOf("먹방", "mukbang"), "politics" to listOf("정치")),
            )
        assertEquals(ExcludeMatch(FilterReason.TOPIC, "mukbang"), filter.evaluate(video("a", title = "라면 먹방")))
        assertEquals(ExcludeMatch(FilterReason.TOPIC, "politics"), filter.evaluate(video("b", title = "오늘의 정치 뉴스")))
        assertNull(filter.evaluate(video("c", title = "고양이")))
    }

    @Test
    fun `unknown topic ids and empty term lists do not filter anything`() {
        val filter = compile(ExcludeFilter(topics = listOf("unknown")), topicTerms = emptyMap())
        assertTrue(filter.isNoOp)
    }

    @Test
    fun `explicit keyword wins over topic in the reported reason`() {
        val filter =
            compile(
                ExcludeFilter(topics = listOf("mukbang"), keywords = listOf("라면")),
                topicTerms = mapOf("mukbang" to listOf("먹방")),
            )
        assertEquals(FilterReason.KEYWORD, filter.evaluate(video("a", title = "라면 먹방"))?.reason)
    }

    @Test
    fun `exception overrides every rule`() {
        val filter =
            compile(
                ExcludeFilter(
                    keywords = listOf("직캠"),
                    blockedChannels = listOf("UCbad"),
                    exceptions = listOf("keep-me"),
                ),
                FilterOptions(hideDisliked = true, hideWatched = true),
                FilterSignals(dislikedVideoIds = setOf("keep-me"), watchedVideoIds = setOf("keep-me")),
            )
        assertNull(filter.evaluate(video("keep-me", title = "직캠", channelId = "UCbad")))
        assertTrue(filter.evaluate(video("other", title = "직캠")) != null)
    }

    @Test
    fun `disliked and watched are excluded only when their options are on`() {
        val signals = FilterSignals(dislikedVideoIds = setOf("d1"), watchedVideoIds = setOf("w1"))

        val off = compile(ExcludeFilter(keywords = listOf("직캠")), FilterOptions(), signals)
        assertNull(off.evaluate(video("d1")))
        assertNull(off.evaluate(video("w1")))

        val on = compile(ExcludeFilter(), FilterOptions(hideDisliked = true, hideWatched = true), signals)
        assertFalse(on.isNoOp)
        assertEquals(ExcludeMatch(FilterReason.DISLIKED, "d1"), on.evaluate(video("d1")))
        assertEquals(ExcludeMatch(FilterReason.WATCHED, "w1"), on.evaluate(video("w1")))
        assertNull(on.evaluate(video("x")))
    }

    @Test
    fun `only the enabled signal counts`() {
        val signals = FilterSignals(dislikedVideoIds = setOf("d1"), watchedVideoIds = setOf("w1"))
        val onlyDislike = compile(ExcludeFilter(), FilterOptions(hideDisliked = true), signals)
        assertEquals(FilterReason.DISLIKED, onlyDislike.evaluate(video("d1"))?.reason)
        assertNull(onlyDislike.evaluate(video("w1")))
    }

    @Test
    fun `items without data are never excluded and never crash`() {
        val filter = compile(ExcludeFilter(keywords = listOf("직캠"), blockedChannels = listOf("UCbad")))
        assertNull(filter.evaluate(MainShortsModel()))
    }

    @Test
    fun `keep is the inverse of evaluate`() {
        val filter = compile(ExcludeFilter(keywords = listOf("직캠")))
        assertFalse(filter.keep(video("a", title = "직캠")))
        assertTrue(filter.keep(video("b", title = "요리")))
    }
}
