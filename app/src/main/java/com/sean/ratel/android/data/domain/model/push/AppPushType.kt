package com.sean.ratel.android.data.domain.model.push

import kotlinx.serialization.Serializable

@Serializable
enum class AppPushType {
    Update,
    Upload,
    Recommend,
}
