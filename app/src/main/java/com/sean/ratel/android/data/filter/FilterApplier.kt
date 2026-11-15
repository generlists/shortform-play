package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.ChannelSubscriptionList
import com.sean.ratel.android.data.dto.ChannelSubscriptionUpList
import com.sean.ratel.android.data.dto.ChannelVideoLikeList
import com.sean.ratel.android.data.dto.ChannelVideoList
import com.sean.ratel.android.data.dto.ChannelVideoSearchList
import com.sean.ratel.android.data.dto.EditorPickList
import com.sean.ratel.android.data.dto.FilterList
import com.sean.ratel.android.data.dto.GroupItem
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.RecommendList
import com.sean.ratel.android.data.dto.ShortFormVideoCommentList
import com.sean.ratel.android.data.dto.ShortFormVideoLikeList
import com.sean.ratel.android.data.dto.ShortFormVideoList
import com.sean.ratel.android.data.dto.ShortFormVideoSearchList
import com.sean.ratel.android.data.dto.TopFiveList
import com.sean.ratel.android.data.dto.TopicItem
import com.sean.ratel.android.data.dto.TopicList
import com.sean.ratel.android.data.dto.TrendsShortFormList

/** 필터를 적용한 결과. [hiddenIds]: 빠진 영상 id (여러 목록에 같은 영상이 있어도 한 번만 센다). */
data class FilterResult<T>(
    val value: T,
    val hiddenIds: Set<String> = emptySet(),
) {
    val hiddenCount: Int get() = hiddenIds.size
}

/**
 * 홈 데이터(MainShortFormList)와 목록에 필터를 적용한다.
 * 필터가 없으면([CompiledFilter.isNoOp]) 입력을 그대로(같은 인스턴스로) 돌려준다 — 기존 동작과 완전히 같다.
 */
fun CompiledFilter.filterList(items: List<MainShortsModel>): FilterResult<List<MainShortsModel>> = filterList(items, mutableSetOf())

private fun CompiledFilter.filterList(
    items: List<MainShortsModel>,
    hidden: MutableSet<String>,
): FilterResult<List<MainShortsModel>> {
    if (isNoOp || items.isEmpty()) return FilterResult(items)
    val kept = ArrayList<MainShortsModel>(items.size)
    items.forEach { item ->
        if (keep(item)) {
            kept += item
        } else {
            item.shortsVideoModel?.videoId?.let { hidden += it }
        }
    }
    return if (kept.size == items.size) FilterResult(items) else FilterResult(kept, hidden)
}

/** 채널별(또는 이벤트별) 목록 맵. 목록이 모두 빠진 항목은 맵에서 뺀다 — 화면이 `list[0]` 을 읽기 때문이다. */
private fun CompiledFilter.filterMap(
    map: Map<String, List<MainShortsModel>>,
    hidden: MutableSet<String>,
): Map<String, List<MainShortsModel>> {
    if (isNoOp || map.isEmpty()) return map
    val out = LinkedHashMap<String, List<MainShortsModel>>(map.size)
    map.forEach { (key, items) ->
        val kept = filterList(items, hidden).value
        if (kept.isNotEmpty()) out[key] = kept
    }
    return out
}

fun CompiledFilter.filterTrends(trends: TrendsShortFormList): FilterResult<TrendsShortFormList> {
    if (isNoOp) return FilterResult(trends)
    val hidden = mutableSetOf<String>()
    return FilterResult(trends.copy(event_list = filterMap(trends.event_list, hidden)), hidden)
}

fun CompiledFilter.filterMain(main: MainShortFormList): FilterResult<MainShortFormList> {
    if (isNoOp) return FilterResult(main)
    val hidden = mutableSetOf<String>()

    // 상단 인기 채널 슬라이드: 전부 빠지면 화면이 비거나 0으로 나누게 되므로 원본을 유지한다
    val five = filterMap(main.topFiveList.fiveList, hidden)
    val topFive = if (five.isEmpty()) main.topFiveList else TopFiveList(main.topFiveList.title, five)
    if (five.isEmpty()) hidden.clear()

    val video = main.shortformVideoList
    val channel = main.channelVideoList
    val filtered =
        main.copy(
            topFiveList = topFive,
            topicList = filterTopicList(main.topicList, hidden),
            editorPickList = EditorPickList(main.editorPickList.title, filterList(main.editorPickList.pickList, hidden).value),
            shortformVideoList =
                ShortFormVideoList(
                    videoSearchList =
                        ShortFormVideoSearchList(
                            video.videoSearchList.title,
                            filterList(video.videoSearchList.searchList, hidden).value,
                        ),
                    videoLikeList =
                        ShortFormVideoLikeList(
                            video.videoLikeList.title,
                            filterList(video.videoLikeList.likeList, hidden).value,
                        ),
                    videoCommentList =
                        ShortFormVideoCommentList(
                            video.videoCommentList.title,
                            filterList(video.videoCommentList.commentList, hidden).value,
                        ),
                ),
            channelVideoList =
                ChannelVideoList(
                    channelSearchList =
                        ChannelVideoSearchList(
                            channel.channelSearchList.title,
                            channel.channelSearchList.period,
                            filterList(channel.channelSearchList.searchList, hidden).value,
                        ),
                    channelLikeList =
                        ChannelVideoLikeList(
                            channel.channelLikeList.title,
                            channel.channelLikeList.period,
                            filterList(channel.channelLikeList.likeList, hidden).value,
                        ),
                ),
            channelSubscriptionList =
                ChannelSubscriptionList(
                    main.channelSubscriptionList.title,
                    main.channelSubscriptionList.period,
                    filterList(main.channelSubscriptionList.subscriptionList, hidden).value,
                ),
            channelSubscriptionUpList =
                ChannelSubscriptionUpList(
                    main.channelSubscriptionUpList.title,
                    main.channelSubscriptionUpList.period,
                    filterList(main.channelSubscriptionUpList.subscriptionUpList, hidden).value,
                ),
            shortformRecommendList =
                RecommendList(
                    main.shortformRecommendList.title,
                    filterList(main.shortformRecommendList.recommendList, hidden).value,
                ),
            trendShortsList = main.trendShortsList.copy(event_list = filterMap(main.trendShortsList.event_list, hidden)),
        )
    return FilterResult(filtered, hidden)
}

/**
 * 주제 탭(먹방, 정치 …)의 목록. 숏폼 탭과 주제 상세 화면이 여기서 영상을 읽는다.
 * - 주제 이름 자체가 제외 규칙에 걸리면 주제를 통째로 뺀다.
 * - 영상을 거르고 나서 하나도 남지 않으면 그 주제도 뺀다(빈 탭이 남지 않게). 원래부터 영상이 없던 주제는 그대로 둔다.
 */
private fun CompiledFilter.filterTopicList(
    topics: TopicList,
    hidden: MutableSet<String>,
): TopicList {
    if (isNoOp || topics.topicList.isEmpty()) return topics
    val out = LinkedHashMap<String, TopicItem>(topics.topicList.size)
    topics.topicList.forEach { (key, item) ->
        if (matchesText(item.topicName)) return@forEach
        val popular = filterGroups(item.popularlist, hidden)
        val viewed = filterGroups(item.viewlist, hidden)
        val subscribed = filterGroups(item.subscriberlist, hidden)
        val hadVideos = listOf(item.popularlist, item.viewlist, item.subscriberlist).any { it.videoCount() > 0 }
        val hasVideos = listOf(popular, viewed, subscribed).any { it.videoCount() > 0 }
        if (hadVideos && !hasVideos) return@forEach
        out[key] = item.copy(popularlist = popular, viewlist = viewed, subscriberlist = subscribed)
    }
    return topics.copy(topicList = out)
}

private fun CompiledFilter.filterGroups(
    list: FilterList?,
    hidden: MutableSet<String>,
): FilterList? {
    if (list == null) return null
    val groups =
        list.topicList.mapNotNull { group: GroupItem ->
            val kept = filterList(group.topicList, hidden).value
            // 거르고 비게 된 묶음은 뺀다(화면이 묶음 단위로 그리므로 빈 묶음은 제목만 남는다)
            if (kept.isEmpty() && group.topicList.isNotEmpty()) null else group.copy(topicList = kept)
        }
    return list.copy(topicList = groups)
}

private fun FilterList?.videoCount(): Int = this?.topicList?.sumOf { it.topicList.size } ?: 0
