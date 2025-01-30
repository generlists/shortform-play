package com.sean.ratel.android.data.api

import androidx.annotation.Keep
import com.sean.ratel.android.di.qualifier.ContentType
import javax.inject.Inject

class RequestHeaders
    @Inject
    constructor(
        @ContentType val contentType: String,
    ) {
        companion object Key {
            const val CONTENT_TYPE = "application/json"
        }

        @Keep
        enum class Param(
            val value: String,
        ) {
            ContentType("Content-Type"),
            CacheControl("Cache-Control"),
        }
    }
