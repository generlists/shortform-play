package com.sean.ratel.android.data.api.interceptor

import com.sean.ratel.android.data.account.AccountSession
import com.sean.ratel.android.data.api.EndPoint
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 로그인 API(/shortformplay/v2/)에만 Firebase ID 토큰을 `X-Firebase-Token` 으로 붙인다.
 * 다른 API 로는 토큰이 나가지 않는다. 서버가 401 을 주면 토큰을 새로 받아 한 번만 다시 보낸다.
 * (`Authorization` 은 기존처럼 AuthInterceptor 가 Integrity 토큰을 붙인다)
 */
@Singleton
class FirebaseTokenInterceptor
    @Inject
    constructor(
        private val account: AccountSession,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (!request.url.encodedPath.contains(EndPoint.V2_PATH)) return chain.proceed(request)

            val token = fetchToken(forceRefresh = false) ?: return chain.proceed(request)
            val first = chain.proceed(request.withToken(token))
            if (first.code != HTTP_UNAUTHORIZED) return first

            // 만료된 토큰일 수 있다 — 새로 받아서 한 번만 다시 시도
            val refreshed = fetchToken(forceRefresh = true)
            if (refreshed == null || refreshed == token) return first
            first.close()
            return chain.proceed(request.withToken(refreshed))
        }

        private fun fetchToken(forceRefresh: Boolean): String? =
            runBlocking { withTimeoutOrNull(TOKEN_TIMEOUT_MS) { account.idToken(forceRefresh) } }

        private fun Request.withToken(token: String): Request = newBuilder().header(HEADER, token).build()

        companion object {
            const val HEADER = "X-Firebase-Token"
            private const val HTTP_UNAUTHORIZED = 401
            private const val TOKEN_TIMEOUT_MS = 10_000L
        }
    }
