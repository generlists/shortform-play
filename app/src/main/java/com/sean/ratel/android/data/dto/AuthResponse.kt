package com.sean.ratel.android.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

// 서버 응답: requestHash와 TTL
@Parcelize
@Keep
data class RequestHashResponse(
    val request_hash: String,
    val ttl: Int,
) : Parcelable

// 서버 요청: requestHash 포함
@Parcelize
@Keep
data class IntegrityExchangeReq(
    val packageName: String,
    val integrityToken: String,
    val requestHash: String,
) : Parcelable

// 서버 응답: 액세스 토큰 발급 결과
@Parcelize
@Keep
data class IntegrityExchangeRes(
    val success: Boolean,
    val access_token: String?,
    val expires_in: Long?,
    val message: String? = null,
    val code: Int? = null,
) : Parcelable
