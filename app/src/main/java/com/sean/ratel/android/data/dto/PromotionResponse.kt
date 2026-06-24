package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class PromotionResponse(
    val success: Boolean,
    val code: Int,
    val productId: String,
    val isPromotionActive: Boolean,
    val discountPercent: Int?,
    val dDayText: String?,
    val promoTitle: String,
    val features: List<String>,
    val promoDesc: String,
    val promoNoti: String?,
    val endTimestamp: Int,
) : Parcelable
