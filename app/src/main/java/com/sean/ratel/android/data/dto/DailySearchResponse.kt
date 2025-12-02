package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class DailySearchResponse(
    val startDate: String,
    val legacyEndDate: String,
    val shortformList: MainShortFormLegacyList,
    val itemSize: Int = 0,
) : Parcelable
