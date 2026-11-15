package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel

/** 미리보기에서 빠지는 영상 하나와 그 이유. */
data class HiddenSample(
    val item: MainShortsModel,
    val match: ExcludeMatch,
) {
    val videoId: String get() = item.shortsVideoModel?.videoId.orEmpty()
}

/**
 * "이렇게 바뀌어요" 미리보기 — 홈 데이터 전체에 필터를 적용해 보면 몇 개 중 몇 개가 빠지는지.
 * 여러 목록에 같은 영상이 있어도 한 번만 센다.
 */
data class FilterPreview(
    val total: Int,
    val hidden: List<HiddenSample>,
) {
    val hiddenCount: Int get() = hidden.size

    companion object {
        val Empty = FilterPreview(0, emptyList())
    }
}

/** 홈 데이터의 모든 영상(영상 id 기준으로 중복 제거, 처음 나온 순서). id 가 없는 영상은 뺀다. */
fun MainShortFormList.allVideos(): List<MainShortsModel> {
    val seen = HashSet<String>()
    val out = ArrayList<MainShortsModel>()

    fun add(items: List<MainShortsModel>?) {
        items?.forEach { item ->
            val id = item.shortsVideoModel?.videoId
            if (!id.isNullOrEmpty() && seen.add(id)) out += item
        }
    }
    topFiveList.fiveList.values.forEach { add(it) }
    editorPickList.pickList.let { add(it) }
    add(shortformVideoList.videoSearchList.searchList)
    add(shortformVideoList.videoLikeList.likeList)
    add(shortformVideoList.videoCommentList.commentList)
    add(channelVideoList.channelSearchList.searchList)
    add(channelVideoList.channelLikeList.likeList)
    add(channelSubscriptionList.subscriptionList)
    add(channelSubscriptionUpList.subscriptionUpList)
    add(shortformRecommendList.recommendList)
    trendShortsList.event_list.values.forEach { add(it) }
    topicList.topicList.values.forEach { topic ->
        listOf(topic.popularlist, topic.viewlist, topic.subscriberlist).forEach { list ->
            list?.topicList?.forEach { group -> add(group.topicList) }
        }
    }
    return out
}

/** 홈 데이터에 이 필터를 적용해 보면 어떻게 되는지. 필터가 없으면 아무것도 빠지지 않는다. */
fun CompiledFilter.preview(main: MainShortFormList): FilterPreview {
    val videos = main.allVideos()
    if (isNoOp || videos.isEmpty()) return FilterPreview(videos.size, emptyList())
    val hidden = videos.mapNotNull { item -> evaluate(item)?.let { HiddenSample(item, it) } }
    return FilterPreview(videos.size, hidden)
}

/** 채널 id → 채널 이름(홈 데이터에 나온 것만). 차단 목록을 id 대신 이름으로 보여줄 때 쓴다. */
fun MainShortFormList.channelNames(): Map<String, String> {
    val out = LinkedHashMap<String, String>()
    allVideos().forEach { item ->
        val channel = item.shortsChannelModel ?: return@forEach
        val id = channel.channelId
        if (id.isNotEmpty() && !out.containsKey(id) && channel.channelTitle.isNotBlank()) out[id] = channel.channelTitle
    }
    return out
}
