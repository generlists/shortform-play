package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class SearchShortsResponse(
    val results: List<SearchResultModel> = emptyList(),
    val hasNext: Boolean = true,
    val cache: Boolean = false,
) : Parcelable

@Parcelize
@Keep
data class SessionResetRes(
    val status: Boolean,
    val message: String,
) : Parcelable
