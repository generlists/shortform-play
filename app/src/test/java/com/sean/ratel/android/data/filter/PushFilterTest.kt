package com.sean.ratel.android.data.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushFilterTest {
    private val recommend =
        mapOf(
            "type" to "Recommend",
            "title" to "[🎯 지금 인기 영상] 라면 먹방 도전",
            "body" to "구독과 좋아요",
            "videoId" to "v1",
            "videoTitle" to "라면 먹방 도전",
            "channelId" to "UCfood",
            "channelTitle" to "푸드채널",
            "category" to "24",
            "categoryName" to "엔터테인먼트",
        )

    private fun compile(
        filter: ExcludeFilter,
        topicTerms: Map<String, List<String>> = emptyMap(),
    ) = CompiledFilter.compile(filter, FilterOptions(), FilterSignals(), topicTerms)

    @Test
    fun `push fields map to the video model the filter reads`() {
        val target = recommend.toFilterTarget()
        assertEquals("v1", target.shortsVideoModel?.videoId)
        assertEquals("라면 먹방 도전", target.shortsVideoModel?.title)
        assertEquals("구독과 좋아요", target.shortsVideoModel?.description)
        assertEquals("UCfood", target.shortsChannelModel?.channelId)
        assertEquals("푸드채널", target.shortsChannelModel?.channelTitle)
        assertEquals("엔터테인먼트", target.shortsVideoModel?.categoryName)
    }

    @Test
    fun `topic keyword and channel rules exclude the push`() {
        val target = recommend.toFilterTarget()
        assertEquals(
            ExcludeMatch(FilterReason.TOPIC, "mukbang"),
            compile(ExcludeFilter(topics = listOf("mukbang")), mapOf("mukbang" to listOf("먹방"))).evaluate(target),
        )
        assertEquals(ExcludeMatch(FilterReason.KEYWORD, "라면"), compile(ExcludeFilter(keywords = listOf("라면"))).evaluate(target))
        assertEquals(
            ExcludeMatch(FilterReason.CHANNEL, "UCfood"),
            compile(ExcludeFilter(blockedChannels = listOf("UCfood"))).evaluate(target),
        )
        assertEquals(
            ExcludeMatch(FilterReason.KEYWORD, "푸드"),
            compile(ExcludeFilter(keywords = listOf("푸드"))).evaluate(target),
        )
    }

    @Test
    fun `exception keeps the push and unrelated rules do not exclude it`() {
        val target = recommend.toFilterTarget()
        assertNull(compile(ExcludeFilter(keywords = listOf("라면"), exceptions = listOf("v1"))).evaluate(target))
        assertNull(compile(ExcludeFilter(keywords = listOf("정치"))).evaluate(target))
    }

    @Test
    fun `old payload without videoTitle strips the server prefix from title`() {
        val old = recommend - "videoTitle"
        assertEquals("라면 먹방 도전", old.toFilterTarget().shortsVideoModel?.title)
        // 머리말의 "인기 영상" 이 키워드에 걸리지 않는다
        assertNull(compile(ExcludeFilter(keywords = listOf("인기 영상"))).evaluate(old.toFilterTarget()))
    }
}
