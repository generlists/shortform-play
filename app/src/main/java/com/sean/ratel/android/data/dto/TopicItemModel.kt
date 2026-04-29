package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class TopicItem(
    val topicId: String,
    val topicName: String,
    val hookText: String,
    val backgroundThumbNail: String?,
    val iconUrl: String?,
    val topBackgroundThumbNailUrl: String?,
    val channel_count: Int,
    val popularlist: FilterList?,
    val viewlist: FilterList?,
    val subscriberlist: FilterList?,
) : Parcelable

@Keep
@Parcelize
data class FilterList(
    val title: String,
    val period: Long,
    val topicList: List<GroupItem>,
) : Parcelable

@Keep
@Parcelize
data class GroupItem(
    val hookText: String,
    val topicList: List<MainShortsModel>,
) : Parcelable
