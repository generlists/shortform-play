package com.sean.ratel.android.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkInterceptor
    @Inject
    constructor() : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
    }
