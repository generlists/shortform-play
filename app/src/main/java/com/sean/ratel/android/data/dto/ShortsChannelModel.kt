package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class ShortsChannelModel(
    var channelId: String,
    var channelThumbNail: String,
    var publishDate: String,
    var channelTitle: String,
    var channelDescription: String,
    var viewCount: String? = null,
    var subscriberCount: String? = null,
    var brandExternalUrl: String? = null,
    var subscriptionUp: Int? = null,
) : Parcelable
