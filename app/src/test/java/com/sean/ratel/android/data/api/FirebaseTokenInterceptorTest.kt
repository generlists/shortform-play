package com.sean.ratel.android.data.api

import android.app.Activity
import com.sean.ratel.android.data.account.AccountSession
import com.sean.ratel.android.data.account.AccountUser
import com.sean.ratel.android.data.account.SignInResult
import com.sean.ratel.android.data.api.interceptor.FirebaseTokenInterceptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 네트워크 없이, 서버 역할을 하는 가짜 인터셉터를 뒤에 붙여서 헤더와 재시도를 확인한다. */
class FirebaseTokenInterceptorTest {
    private class Account(
        private val token: String?,
        private val refreshedToken: String? = null,
    ) : AccountSession {
        var tokenCalls = 0
        var refreshCalls = 0

        override val user: Flow<AccountUser?> = flowOf(null)
        override val currentUser: AccountUser? = null

        override suspend fun idToken(forceRefresh: Boolean): String? {
            if (forceRefresh) {
                refreshCalls++
                return refreshedToken
            }
            tokenCalls++
            return token
        }

        override suspend fun signInWithGoogle(activity: Activity): SignInResult = SignInResult.Cancelled

        override suspend fun signOut() = Unit
    }

    /** 서버 흉내: 받은 요청을 기록하고, 요청 순서대로 정해 둔 코드를 돌려준다. */
    private class FakeServer(
        private vararg val codes: Int,
    ) : Interceptor {
        val seen = mutableListOf<Request>()

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            seen += request
            val code = codes.getOrElse(seen.size - 1) { codes.last() }
            return Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("fake")
                .body("".toResponseBody(null))
                .build()
        }
    }

    private fun call(
        account: Account,
        server: FakeServer,
        url: String,
    ): Response {
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(FirebaseTokenInterceptor(account))
                .addInterceptor(server)
                .build()
        return client.newCall(Request.Builder().url(url).build()).execute()
    }

    private val v2Url = "https://example.com/shortformplay/v2/me/filters"
    private val otherUrl = "https://example.com/shortformplay/search?q=a"

    @Test
    fun `other apis never get the token`() {
        val account = Account("tok")
        val server = FakeServer(200)
        call(account, server, otherUrl).close()
        assertNull(server.seen.single().header(FirebaseTokenInterceptor.HEADER))
        assertEquals(0, account.tokenCalls)
    }

    @Test
    fun `v2 api gets the token header`() {
        val server = FakeServer(200)
        call(Account("tok"), server, v2Url).close()
        assertEquals("tok", server.seen.single().header("X-Firebase-Token"))
    }

    @Test
    fun `signed out requests go out without the header`() {
        val server = FakeServer(401)
        val account = Account(null)
        call(account, server, v2Url).use { assertEquals(401, it.code) }
        assertNull(server.seen.single().header(FirebaseTokenInterceptor.HEADER))
        assertEquals(0, account.refreshCalls)
    }

    @Test
    fun `a 401 refreshes the token and retries once`() {
        val server = FakeServer(401, 200)
        val account = Account("old", refreshedToken = "new")
        call(account, server, v2Url).use { assertEquals(200, it.code) }
        assertEquals(listOf("old", "new"), server.seen.map { it.header(FirebaseTokenInterceptor.HEADER) })
        assertEquals(1, account.refreshCalls)
    }

    @Test
    fun `no retry when the refreshed token is the same or missing`() {
        val same = FakeServer(401)
        call(Account("tok", refreshedToken = "tok"), same, v2Url).use { assertEquals(401, it.code) }
        assertEquals(1, same.seen.size)

        val missing = FakeServer(401)
        call(Account("tok", refreshedToken = null), missing, v2Url).use { assertEquals(401, it.code) }
        assertEquals(1, missing.seen.size)
    }

    @Test
    fun `only one retry even if the server keeps answering 401`() {
        val server = FakeServer(401)
        call(Account("old", refreshedToken = "new"), server, v2Url).use { assertEquals(401, it.code) }
        assertEquals(2, server.seen.size)
    }

    @Test
    fun `other errors are passed through without a retry`() {
        val server = FakeServer(409)
        val account = Account("tok", refreshedToken = "new")
        call(account, server, v2Url).use { assertEquals(409, it.code) }
        assertEquals(1, server.seen.size)
        assertEquals(0, account.refreshCalls)
    }
}
