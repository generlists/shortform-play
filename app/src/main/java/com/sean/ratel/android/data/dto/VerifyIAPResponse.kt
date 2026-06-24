package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class VerifyIAPRequest(
    val packageName: String,
    val productId: String,
    val purchaseToken: String,
) : Parcelable

@Parcelize
@Keep
data class VerifyIAPResponse(
    val success: Boolean,
    val data: VerifyPurchaseData?,
    val error: ApiError?,
) : Parcelable

@Parcelize
@Keep
data class VerifyPurchaseData(
    val active: Boolean,
    val purchaseState: Int?,
    val acknowledgementState: Int?,
    val orderId: String?,
    val productId: String?,
    val packageName: String?,
) : Parcelable

@Parcelize
@Keep
data class ApiError(
    val code: String,
    val message: String,
) : Parcelable
