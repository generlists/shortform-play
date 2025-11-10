package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class SearchShortsSuggestResponse(
    val query: String = "",
    val suggestions: List<String> = emptyList(),
) : Parcelable
