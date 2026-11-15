package com.sean.ratel.android.data.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** 서버 /shortformplay/v2 의 요청·응답. 값이 빠져 올 수 있어 목록은 nullable 로 받고 변환할 때 비운다. */
@Keep
data class FiltersDto(
    val topics: List<String>? = null,
    val keywords: List<String>? = null,
    @SerializedName("blocked_channels") val blockedChannels: List<String>? = null,
    val exceptions: List<String>? = null,
    val version: Int = 0,
    @SerializedName("updated_at") val updatedAt: String? = null,
)

/** PUT /me/filters 본문. [version]: 앱이 마지막으로 받은 서버 version(저장한 적 없으면 0). */
@Keep
data class FiltersSaveRequest(
    val version: Int,
    val topics: List<String>,
    val keywords: List<String>,
    @SerializedName("blocked_channels") val blockedChannels: List<String>,
    val exceptions: List<String>,
)

@Keep
data class MeDto(
    @SerializedName("user_id") val userId: Long = 0,
    val provider: String? = null,
    val email: String? = null,
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

@Keep
data class LoginDto(
    val user: MeDto? = null,
    val filters: FiltersDto? = null,
)

/** 409 — 다른 기기가 먼저 저장했다. [current] 가 서버의 최신 필터. */
@Keep
data class FiltersConflictDto(
    val success: Boolean = false,
    val code: Int = 409,
    val message: String? = null,
    val current: FiltersDto? = null,
)
