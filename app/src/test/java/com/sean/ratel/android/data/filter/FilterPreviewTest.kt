package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.EditorPickList
import com.sean.ratel.android.data.dto.FilterList
import com.sean.ratel.android.data.dto.GroupItem
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.RecommendList
import com.sean.ratel.android.data.dto.TopFiveList
import com.sean.ratel.android.data.dto.TopicItem
import com.sean.ratel.android.data.dto.TopicList
import com.sean.ratel.android.data.dto.TrendsShortFormList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterPreviewTest {
    private val dup = video("dup", title = "직캠 모음")

    private val main =
        MainShortFormList(
            topFiveList = TopFiveList("top", mapOf("ch" to listOf(video("t1", channelId = "ch")))),
            editorPickList = EditorPickList("pick", listOf(dup, video("e1"))),
            shortformRecommendList = RecommendList("rec", listOf(dup, video("r1", title = "무대 직캠"))),
            trendShortsList = TrendsShortFormList("t", mapOf("e" to listOf(video("tr1")))),
            topicList =
                TopicList(
                    "topics",
                    mapOf(
                        "food" to
                            TopicItem(
                                topicId = "food",
                                topicName = "음식",
                                hookText = "",
                                backgroundThumbNail = null,
                                iconUrl = null,
                                topBackgroundThumbNailUrl = null,
                                channel_count = 1,
                                popularlist = FilterList("p", 0L, listOf(GroupItem("g", listOf(video("f1", title = "먹방 직캠"))))),
                                viewlist = null,
                                subscriberlist = null,
                            ),
                    ),
                ),
        )

    @Test
    fun `allVideos walks every list and counts a video once`() {
        assertEquals(listOf("t1", "dup", "e1", "r1", "tr1", "f1"), main.allVideos().map { it.shortsVideoModel?.videoId })
    }

    @Test
    fun `preview reports hidden samples with reasons out of the total`() {
        val preview = compile(ExcludeFilter(keywords = listOf("직캠"), blockedChannels = listOf("ch"))).preview(main)
        assertEquals(6, preview.total)
        assertEquals(listOf("t1", "dup", "r1", "f1"), preview.hidden.map { it.videoId })
        assertEquals(FilterReason.CHANNEL, preview.hidden[0].match.reason)
        assertEquals(FilterReason.KEYWORD, preview.hidden[1].match.reason)
        assertEquals("직캠", preview.hidden[1].match.detail)
    }

    @Test
    fun `no-op filter hides nothing but still counts the total`() {
        val preview = CompiledFilter.NoOp.preview(main)
        assertEquals(6, preview.total)
        assertTrue(preview.hidden.isEmpty())
        assertEquals(FilterPreview.Empty, CompiledFilter.NoOp.preview(MainShortFormList()))
    }

    @Test
    fun `exceptions are honoured in the preview`() {
        val preview = compile(ExcludeFilter(keywords = listOf("직캠"), exceptions = listOf("dup"))).preview(main)
        assertEquals(listOf("r1", "f1"), preview.hidden.map { it.videoId })
    }
}
