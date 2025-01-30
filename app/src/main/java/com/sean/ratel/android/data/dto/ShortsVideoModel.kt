package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class ShortsVideoModel(
    var videoId: String,
    var thumbNail: String,
    var title: String,
    var description: String,
    var duration: String,
    var category: String,
    var categoryName: String,
    var viewCount: String? = "0",
    var likeCount: String? = "0",
    var commentCount: String? = "0",
) : Parcelable
