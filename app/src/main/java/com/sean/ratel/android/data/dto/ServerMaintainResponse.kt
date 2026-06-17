package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class ServerMaintainResponse(
    val maintain: Boolean,
    val code: Int,
    val title: String,
    val message: String,
    val startTime: Long,
    val endTime: Long,
) : Parcelable
