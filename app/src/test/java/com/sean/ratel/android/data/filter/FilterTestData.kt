package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.ShortsChannelModel
import com.sean.ratel.android.data.dto.ShortsVideoModel

/** 필터 테스트용 영상 만들기. */
fun video(
    id: String,
    title: String = "제목",
    channelId: String = "ch-$id",
    channelTitle: String = "채널",
    description: String = "",
    category: String = "",
    categoryName: String = "",
    topicName: String? = null,
): MainShortsModel =
    MainShortsModel(
        shortsChannelModel =
            ShortsChannelModel(
                channelId = channelId,
                channelThumbNail = "",
                publishDate = "",
                channelTitle = channelTitle,
                channelDescription = "",
            ),
        shortsVideoModel =
            ShortsVideoModel(
                videoId = id,
                thumbNail = "",
                title = title,
                description = description,
                duration = "",
                category = category,
                topicName = topicName,
                categoryName = categoryName,
            ),
    )

fun compile(
    filter: ExcludeFilter = ExcludeFilter(),
    options: FilterOptions = FilterOptions(),
    signals: FilterSignals = FilterSignals(),
    topicTerms: Map<String, List<String>> = emptyMap(),
): CompiledFilter = CompiledFilter.compile(filter, options, signals, topicTerms)
