package com.sean.ratel.android.data.api.interceptor

import com.sean.ratel.android.data.api.RequestHeaders
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestHeaderInterceptor
    @Inject
    constructor(
        private val requestHeaders: RequestHeaders,
    ) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val builder = request.newBuilder()
            builder.addHeader(RequestHeaders.Param.ContentType.value, requestHeaders.contentType)
            builder.addHeader(RequestHeaders.Param.CacheControl.value, "public, max-age=" + 2)

            return chain.proceed(builder.build())
        }
    }
