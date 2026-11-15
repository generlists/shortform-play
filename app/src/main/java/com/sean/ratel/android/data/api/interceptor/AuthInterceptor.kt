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

/**
 * 서버 접근 토큰(Integrity JWT)을 붙이고, 만료되면(412, 또는 401) 갱신해서 한 번 다시 보낸다.
 * 갱신이 실패해도 예외를 던지지 않고 원래 응답을 돌려준다.
 *
 * @param accessToken 지금 저장된 접근 토큰
 * @param refreshToken 새 접근 토큰을 받아 온다. 못 받으면 null (예외를 던져도 된다)
 */
@Singleton
class AuthInterceptor internal constructor(
    private val accessToken: () -> String?,
    private val refreshToken: suspend () -> String?,
) : Interceptor {
    @Inject
    constructor(
        preference: AuthTokenPreference,
        integrityManager: IntegrityManager,
    ) : this(
        accessToken = { preference.getAccessToken() },
        refreshToken = { integrityManager.headerRefreshToken()?.first },
    )

    private val tokenMutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val token = accessToken()

        token?.let {
            request =
                request
                    .newBuilder()
                    .addHeader("Authorization", "Bearer $it")
                    .build()
        }

        val response = chain.proceed(request)

        // 412 = 서버가 알려 주는 "접근 토큰 만료". 401 도 같이 갱신하되,
        // 로그인 API(X-Firebase-Token)의 401 은 Firebase 토큰 문제라 Integrity 토큰을 갱신하지 않는다
        val tokenExpired =
            response.code == TOKEN_EXPIRED ||
                (response.code == 401 && request.header(FirebaseTokenInterceptor.HEADER) == null)
        if (tokenExpired) {
            // 갱신이 실패해도 인터셉터에서 예외를 던지면 앱이 죽는다 → 실패하면 원래 응답을 그대로 돌려준다
            val newToken =
                runCatching {
                    runBlocking {
                        tokenMutex.withLock { refreshToken() }
                    }
                }.getOrNull()
            // 갱신에 성공해 다시 요청할 때만 원래 응답을 닫는다(실패했는데 닫힌 응답을 돌려주면 읽는 쪽이 터진다)
            return if (newToken != null) {
                response.close()
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

    private companion object {
        /** 서버(auth/auth.py)가 접근 토큰이 만료됐을 때 보내는 코드. */
        const val TOKEN_EXPIRED = 412
    }
}
