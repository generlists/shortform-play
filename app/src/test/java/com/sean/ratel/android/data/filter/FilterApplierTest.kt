package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.ChannelSubscriptionList
import com.sean.ratel.android.data.dto.EditorPickList
import com.sean.ratel.android.data.dto.FilterList
import com.sean.ratel.android.data.dto.GroupItem
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.RecommendList
import com.sean.ratel.android.data.dto.ShortFormVideoList
import com.sean.ratel.android.data.dto.ShortFormVideoSearchList
import com.sean.ratel.android.data.dto.TopFiveList
import com.sean.ratel.android.data.dto.TopicItem
import com.sean.ratel.android.data.dto.TopicList
import com.sean.ratel.android.data.dto.TrendsShortFormList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterApplierTest {
    private val bad = compile(ExcludeFilter(keywords = listOf("직캠")))

    private fun ids(list: List<com.sean.ratel.android.data.dto.MainShortsModel>) = list.map { it.shortsVideoModel?.videoId }

    private fun topic(
        id: String,
        name: String,
        popular: List<List<com.sean.ratel.android.data.dto.MainShortsModel>> = emptyList(),
        viewed: List<List<com.sean.ratel.android.data.dto.MainShortsModel>> = emptyList(),
    ) = TopicItem(
        topicId = id,
        topicName = name,
        hookText = "",
        backgroundThumbNail = null,
        iconUrl = null,
        topBackgroundThumbNailUrl = null,
        channel_count = 1,
        popularlist = FilterList("p", 0L, popular.map { GroupItem("g", it) }),
        viewlist = FilterList("v", 0L, viewed.map { GroupItem("g", it) }),
        subscriberlist = null,
    )

    private fun groupVideoIds(list: FilterList?) = list?.topicList?.map { g -> ids(g.topicList) }

    @Test
    fun `no-op returns the same instances`() {
        val list = listOf(video("a", title = "직캠"))
        assertSame(list, CompiledFilter.NoOp.filterList(list).value)

        val main = MainShortFormList(editorPickList = EditorPickList("t", list))
        assertSame(main, CompiledFilter.NoOp.filterMain(main).value)

        val trends = TrendsShortFormList("t", mapOf("e" to list))
        assertSame(trends, CompiledFilter.NoOp.filterTrends(trends).value)
    }

    @Test
    fun `filterList removes excluded items and reports hidden ids`() {
        val result = bad.filterList(listOf(video("a", title = "직캠"), video("b"), video("c", title = "무대 직캠")))
        assertEquals(listOf("b"), ids(result.value))
        assertEquals(setOf("a", "c"), result.hiddenIds)
        assertEquals(2, result.hiddenCount)
    }

    @Test
    fun `filterList keeps the original instance when nothing is removed`() {
        val list = listOf(video("a"), video("b"))
        assertSame(list, bad.filterList(list).value)
        assertEquals(0, bad.filterList(list).hiddenCount)
    }

    @Test
    fun `filterMain filters every list and counts a video once`() {
        val dup = video("dup", title = "직캠")
        val main =
            MainShortFormList(
                editorPickList = EditorPickList("pick", listOf(dup, video("e1"))),
                shortformVideoList =
                    ShortFormVideoList(
                        videoSearchList = ShortFormVideoSearchList("s", listOf(dup, video("s1"))),
                    ),
                channelSubscriptionList = ChannelSubscriptionList("sub", 1L, listOf(video("c1", title = "직캠 모음"), video("c2"))),
                shortformRecommendList = RecommendList("rec", listOf(video("r1"), dup)),
            )
        val result = bad.filterMain(main)

        assertEquals(listOf("e1"), ids(result.value.editorPickList.pickList))
        assertEquals(listOf("s1"), ids(result.value.shortformVideoList.videoSearchList.searchList))
        assertEquals(listOf("c2"), ids(result.value.channelSubscriptionList.subscriptionList))
        assertEquals(listOf("r1"), ids(result.value.shortformRecommendList.recommendList))
        assertEquals(setOf("dup", "c1"), result.hiddenIds)
        // 제목 같은 나머지 값은 그대로
        assertEquals("pick", result.value.editorPickList.title)
        assertEquals(1L, result.value.channelSubscriptionList.period)
    }

    @Test
    fun `top five drops blocked channels but never becomes empty`() {
        val five =
            TopFiveList(
                "top",
                mapOf(
                    "ch1" to listOf(video("a1", channelId = "ch1")),
                    "ch2" to listOf(video("b1", channelId = "ch2")),
                ),
            )
        val blockOne = compile(ExcludeFilter(blockedChannels = listOf("ch1")))
        val one = blockOne.filterMain(MainShortFormList(topFiveList = five))
        assertEquals(listOf("ch2"), one.value.topFiveList.fiveList.keys.toList())
        assertEquals("top", one.value.topFiveList.title)

        // 전부 막으면 슬라이드가 비어 화면이 깨지므로 원본을 유지한다
        val blockAll = compile(ExcludeFilter(blockedChannels = listOf("ch1", "ch2")))
        val all = blockAll.filterMain(MainShortFormList(topFiveList = five))
        assertEquals(listOf("ch1", "ch2"), all.value.topFiveList.fiveList.keys.toList())
        assertEquals(0, all.hiddenCount)
    }

    @Test
    fun `trends map drops events that become empty`() {
        val trends =
            TrendsShortFormList(
                "t",
                mapOf(
                    "e1" to listOf(video("a", title = "직캠")),
                    "e2" to listOf(video("b", title = "직캠"), video("c")),
                ),
            )
        val result = bad.filterTrends(trends)
        assertEquals(listOf("e2"), result.value.event_list.keys.toList())
        assertEquals(listOf("c"), ids(result.value.event_list.getValue("e2")))
        assertEquals(setOf("a", "b"), result.hiddenIds)
    }

    @Test
    fun `topic lists are filtered and emptied groups are dropped`() {
        val topics =
            TopicList(
                "t",
                mapOf(
                    "food" to
                        topic(
                            "food",
                            "음식",
                            popular = listOf(listOf(video("f1"), video("f2", title = "직캠")), listOf(video("f3", title = "직캠"))),
                            viewed = listOf(listOf(video("f4"))),
                        ),
                ),
            )
        val result = bad.filterMain(MainShortFormList(topicList = topics))
        val food = result.value.topicList.topicList.getValue("food")
        assertEquals(listOf(listOf("f1")), groupVideoIds(food.popularlist))
        assertEquals(listOf(listOf("f4")), groupVideoIds(food.viewlist))
        assertEquals(setOf("f2", "f3"), result.hiddenIds)
    }

    @Test
    fun `a topic whose name matches or whose videos all go is removed`() {
        val politics = compile(ExcludeFilter(keywords = listOf("정치")))
        val topics =
            TopicList(
                "t",
                mapOf(
                    "news" to topic("news", "뉴스/정치", popular = listOf(listOf(video("n1")))),
                    "emptied" to topic("emptied", "논평", popular = listOf(listOf(video("e1", title = "정치 이야기")))),
                    "kept" to topic("kept", "음식", popular = listOf(listOf(video("k1")))),
                    "novideos" to topic("novideos", "새 주제"),
                ),
            )
        val result = politics.filterMain(MainShortFormList(topicList = topics))
        assertEquals(listOf("kept", "novideos"), result.value.topicList.topicList.keys.toList())
    }

    @Test
    fun `matchesText checks only the keyword rules`() {
        val filter = compile(ExcludeFilter(keywords = listOf("정치"), blockedChannels = listOf("ch")))
        assertTrue(filter.matchesText("뉴스/정치"))
        assertTrue(!filter.matchesText("음식"))
        assertTrue(!CompiledFilter.NoOp.matchesText("정치"))
    }

    @Test
    fun `empty lists stay empty`() {
        val result = bad.filterMain(MainShortFormList())
        assertTrue(result.value.editorPickList.pickList.isEmpty())
        assertEquals(0, result.hiddenCount)
    }
}
