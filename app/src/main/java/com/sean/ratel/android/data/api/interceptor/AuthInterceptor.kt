package com.sean.ratel.android.data.api.interceptor

import com.sean.ratel.android.data.common.IntegrityManager
import com.sean.ratel.android.data.local.pref.AuthTokenPreference
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor
    @Inject
    constructor(
        private val preference: AuthTokenPreference,
        private val integrityManager: IntegrityManager,
    ) : Interceptor {
        private val tokenMutex = Mutex()

        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            val token = preference.getAccessToken()

            token?.let {
                request =
                    request
                        .newBuilder()
                        .addHeader("Authorization", "Bearer $it")
                        .build()
            }

            val response = chain.proceed(request)

            if (response.code == 401) {
                response.close()
                val newToken =
                    runBlocking {
                        tokenMutex.withLock {
                            integrityManager.headerRefreshToken()?.first
                        }
                    }
                return if (newToken != null) {
                    val retry =
                        request
                            .newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    chain.proceed(retry)
                } else {
                    response
                }
            }

            return response
        }
    }
