package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class MainShortsResponse(
    val shortformList: MainShortFormList,
    val itemSize: Int = 7,
) : Parcelable

@Keep
@Parcelize
data class MainShortFormList(
    val topFiveList: TopFiveList = TopFiveList(),
    val editorPickList: EditorPickList = EditorPickList(),
    val shortformVideoList: ShortFormVideoList = ShortFormVideoList(),
    val channelVideoList: ChannelVideoList = ChannelVideoList(),
    val channelSubscriptionList: ChannelSubscriptionList = ChannelSubscriptionList(),
    val channelSubscriptionUpList: ChannelSubscriptionUpList = ChannelSubscriptionUpList(),
    val shortformRecommendList: RecommendList = RecommendList(),
) : Parcelable

@Keep
@Parcelize
data class TopFiveList(
    val title: String = "",
    val fiveList: Map<String, List<MainShortsModel>> = emptyMap(),
) : Parcelable

@Keep
@Parcelize
data class EditorPickList(
    val title: String = "",
    val pickList: List<MainShortsModel> = emptyList(),
) : Parcelable

@Keep
@Parcelize
data class ShortFormVideoList(
    val videoSearchList: ShortFormVideoSearchList = ShortFormVideoSearchList(),
    val videoLikeList: ShortFormVideoLikeList = ShortFormVideoLikeList(),
    val videoCommentList: ShortFormVideoCommentList = ShortFormVideoCommentList(),
) : Parcelable

@Keep
@Parcelize
data class ShortFormVideoSearchList(
    val title: String = "",
    val searchList: List<MainShortsModel> = emptyList(),
) : Parcelable

@Keep
@Parcelize
data class ShortFormVideoLikeList(
    val title: String = "",
    val likeList: List<MainShortsModel> = emptyList(),
) : Parcelable

@Keep
@Parcelize
data class ShortFormVideoCommentList(
    val title: String = "",
    val commentList: List<MainShortsModel> = emptyList(),
) : Parcelable

@Keep
@Parcelize
data class ChannelVideoList(
    val channelSearchList: ChannelVideoSearchList = ChannelVideoSearchList(),
    val channelLikeList: ChannelVideoLikeList = ChannelVideoLikeList(),
) : Parcelable

@Keep
@Parcelize
data class ChannelVideoSearchList(
    val title: String = "",
    val period: Long? = System.currentTimeMillis(),
    val searchList: List<MainShortsModel> = emptyList(),
) : Parcelable

@Keep
@Parcelize
data class ChannelVideoLikeList(
    val title: String = "",
    val period: Long? = System.currentTimeMillis(),
    val likeList: List<MainShortsModel> = emptyList(),
) : Parcelable

@Keep
@Parcelize
data class ChannelSubscriptionList(
    val title: String = "",
    val period: Long? = System.currentTimeMillis(),
    val subscriptionList: List<MainShortsModel> = emptyList(),
) : Parcelable

@Keep
@Parcelize
data class ChannelSubscriptionUpList(
    val title: String = "",
    val period: Long? = System.currentTimeMillis(),
    val subscriptionUpList: List<MainShortsModel> = emptyList(),
) : Parcelable

@Keep
@Parcelize
data class RecommendList(
    val title: String = "",
    val recommendList: List<MainShortsModel> = emptyList(),
) : Parcelable
