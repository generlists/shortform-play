package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class SearchResultModel(
    var itemPosition: Int = 0,
    val searchKeyword: String = "",
    val title: String? = null,
    val url: String? = null,
    val videoId: String? = null,
    val thumbnail: String? = null,
    val channelName: String? = null,
    val channelId: String? = null,
    val channelThumbnail: String? = null,
    val views: String? = null,
    val uploadedTime: String? = null,
) : Parcelable
