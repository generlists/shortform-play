package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.SearchResultModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SearchFilterPolicyTest {
    private fun result(
        id: String,
        title: String = "제목",
        channelId: String = "ch-$id",
        channelName: String = "채널",
    ) = SearchResultModel(videoId = id, title = title, channelId = channelId, channelName = channelName)

    private val filter = compile(ExcludeFilter(keywords = listOf("정치"), blockedChannels = listOf("UCbad")))

    private fun ids(view: SearchFilterView<SearchResultModel>) = view.visible.map { it.videoId }

    @Test
    fun `no filter or no results means off and the same list`() {
        val items = listOf(result("a", "정치"))
        val off = SearchFilterPolicy.keyword(items, CompiledFilter.NoOp, "아무거나", fromDeepLink = false, revealed = false)
        assertEquals(SearchFilterMode.OFF, off.mode)
        assertSame(items, off.visible)
        assertEquals(SearchFilterMode.OFF, SearchFilterPolicy.keyword(emptyList(), filter, "x", false, false).mode)
    }

    @Test
    fun `normal search hides matches and reports the count`() {
        val view =
            SearchFilterPolicy.keyword(
                listOf(result("a", "정치 이야기"), result("b"), result("c", channelId = "UCbad")),
                filter,
                "고양이",
                fromDeepLink = false,
                revealed = false,
            )
        assertEquals(SearchFilterMode.FILTERED, view.mode)
        assertEquals(listOf("b"), ids(view))
        assertEquals(2, view.hiddenCount)
    }

    @Test
    fun `nothing to hide stays filtered with zero hidden`() {
        val items = listOf(result("a"), result("b"))
        val view = SearchFilterPolicy.keyword(items, filter, "고양이", false, false)
        assertEquals(SearchFilterMode.FILTERED, view.mode)
        assertEquals(0, view.hiddenCount)
        assertSame(items, view.visible)
    }

    @Test
    fun `a query that overlaps the exclusions is never filtered`() {
        val items = listOf(result("a", "정치 토론"), result("b"))
        val view = SearchFilterPolicy.keyword(items, filter, "정치 토론", fromDeepLink = false, revealed = false)
        assertEquals(SearchFilterMode.QUERY_OVERLAP, view.mode)
        assertEquals(listOf("a", "b"), ids(view))
        assertEquals(0, view.hiddenCount)
    }

    @Test
    fun `deep link searches are never filtered`() {
        val items = listOf(result("a", "정치"), result("b"))
        val view = SearchFilterPolicy.keyword(items, filter, "고양이", fromDeepLink = true, revealed = false)
        assertEquals(SearchFilterMode.DEEP_LINK, view.mode)
        assertEquals(listOf("a", "b"), ids(view))
    }

    @Test
    fun `revealing shows everything but remembers how many were hidden`() {
        val items = listOf(result("a", "정치"), result("b"))
        val view = SearchFilterPolicy.keyword(items, filter, "고양이", fromDeepLink = false, revealed = true)
        assertEquals(SearchFilterMode.REVEALED, view.mode)
        assertEquals(listOf("a", "b"), ids(view))
        assertEquals(1, view.hiddenCount)
    }

    @Test
    fun `when everything would be hidden the full results are shown instead`() {
        val items = listOf(result("a", "정치 1"), result("b", "정치 2"))
        val view = SearchFilterPolicy.keyword(items, filter, "고양이", fromDeepLink = false, revealed = false)
        assertEquals(SearchFilterMode.ALL_HIDDEN, view.mode)
        assertEquals(listOf("a", "b"), ids(view))
        assertEquals(2, view.hiddenCount)
    }

    @Test
    fun `exceptions keep a video even when it matches`() {
        val withException = compile(ExcludeFilter(keywords = listOf("정치"), exceptions = listOf("a")))
        val view = SearchFilterPolicy.keyword(listOf(result("a", "정치"), result("b", "정치")), withException, "고양이", false, false)
        assertEquals(listOf("a"), ids(view))
        assertEquals(1, view.hiddenCount)
    }

    @Test
    fun `topic words also trigger the query overlap rule`() {
        val topicFilter = compile(ExcludeFilter(topics = listOf("politics")), topicTerms = mapOf("politics" to listOf("대통령", "국회")))
        val view = SearchFilterPolicy.keyword(listOf(result("a", "대통령 연설")), topicFilter, "대통령 연설", false, false)
        assertEquals(SearchFilterMode.QUERY_OVERLAP, view.mode)
    }

    @Test
    fun `daily list is filtered once and never emptied`() {
        val some = listOf(video("a", title = "정치"), video("b"))
        val view = SearchFilterPolicy.daily(some, filter, fromDeepLink = false)
        assertEquals(SearchFilterMode.FILTERED, view.mode)
        assertEquals(listOf("b"), view.visible.map { it.shortsVideoModel?.videoId })
        assertEquals(1, view.hiddenCount)

        val all = listOf(video("a", title = "정치 1"), video("b", title = "정치 2"))
        val full = SearchFilterPolicy.daily(all, filter, fromDeepLink = false)
        assertEquals(SearchFilterMode.ALL_HIDDEN, full.mode)
        assertEquals(2, full.visible.size)

        assertEquals(SearchFilterMode.DEEP_LINK, SearchFilterPolicy.daily(some, filter, fromDeepLink = true).mode)
        assertSame(some, SearchFilterPolicy.daily(some, CompiledFilter.NoOp, false).visible)
    }

    @Test
    fun `search results are evaluated on title and channel name only`() {
        val f = compile(ExcludeFilter(keywords = listOf("직캠")))
        assertEquals(FilterReason.KEYWORD, f.evaluate(result("a", title = "무대 직캠"))?.reason)
        assertEquals(FilterReason.KEYWORD, f.evaluate(result("b", channelName = "직캠TV"))?.reason)
        assertEquals(null, f.evaluate(result("c")))
        assertEquals(
            FilterReason.CHANNEL,
            compile(ExcludeFilter(blockedChannels = listOf("x"))).evaluate(result("d", channelId = "x"))?.reason,
        )
    }
}
