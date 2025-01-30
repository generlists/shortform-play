package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class MainShortsModel(
    var itemPosition: Int = 0,
    var saveTime: Float = 0f,
    var shortsChannelModel: ShortsChannelModel? = null,
    var shortsVideoModel: ShortsVideoModel? = null,
) : Parcelable
