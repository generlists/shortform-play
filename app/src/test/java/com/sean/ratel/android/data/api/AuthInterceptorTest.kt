package com.sean.ratel.android.data.api

import com.sean.ratel.android.data.api.interceptor.AuthInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 가짜 서버를 뒤에 붙여서 토큰 헤더, 갱신, 재시도, 갱신 실패 때 동작을 확인한다. */
class AuthInterceptorTest {
    private class FakeServer(
        private vararg val codes: Int,
    ) : Interceptor {
        val seen = mutableListOf<Request>()

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            seen += request
            return Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(codes.getOrElse(seen.size - 1) { codes.last() })
                .message("fake")
                .body("body-${seen.size}".toResponseBody(null))
                .build()
        }
    }

    private var refreshCalls = 0

    private fun call(
        server: FakeServer,
        token: String? = "old",
        refresh: suspend () -> String?,
        url: String = "https://example.com/shortformplay/v2/me",
        headers: Map<String, String> = emptyMap(),
    ): Response {
        val interceptor =
            AuthInterceptor(
                accessToken = { token },
                refreshToken = {
                    refreshCalls++
                    refresh()
                },
            )
        val client = OkHttpClient.Builder().addInterceptor(interceptor).addInterceptor(server).build()
        val request = Request.Builder().url(url)
        headers.forEach { (k, v) -> request.header(k, v) }
        return client.newCall(request.build()).execute()
    }

    @Test
    fun `adds the stored token and leaves a good response alone`() {
        val server = FakeServer(200)
        call(server, refresh = { "new" }).use { assertEquals(200, it.code) }
        assertEquals("Bearer old", server.seen.single().header("Authorization"))
        assertEquals(0, refreshCalls)
    }

    @Test
    fun `no stored token means no header`() {
        val server = FakeServer(200)
        call(server, token = null, refresh = { "new" }).close()
        assertNull(server.seen.single().header("Authorization"))
    }

    @Test
    fun `an expired token (412) is refreshed and the request retried once`() {
        val server = FakeServer(412, 200)
        call(server, refresh = { "new" }).use {
            assertEquals(200, it.code)
            assertEquals("body-2", it.body?.string())
        }
        assertEquals(1, refreshCalls)
        assertEquals(listOf("Bearer old", "Bearer new"), server.seen.map { it.header("Authorization") })
    }

    @Test
    fun `a 401 also refreshes`() {
        val server = FakeServer(401, 200)
        call(server, refresh = { "new" }).use { assertEquals(200, it.code) }
        assertEquals(1, refreshCalls)
    }

    @Test
    fun `a 401 from the Firebase login api does not refresh the integrity token`() {
        val server = FakeServer(401)
        call(server, refresh = { "new" }, headers = mapOf("X-Firebase-Token" to "t")).use { assertEquals(401, it.code) }
        assertEquals(0, refreshCalls)
        assertEquals(1, server.seen.size)
    }

    @Test
    fun `when the refresh gives nothing the original response comes back unread-able not closed`() {
        val server = FakeServer(412)
        call(server, refresh = { null }).use {
            assertEquals(412, it.code)
            // 닫힌 응답이면 여기서 IllegalStateException 이 난다
            assertEquals("body-1", it.body?.string())
        }
        assertEquals(1, server.seen.size)
    }

    @Test
    fun `when the refresh throws the original response comes back and nothing is thrown`() {
        val server = FakeServer(412)
        call(server, refresh = { throw IllegalStateException("integrity unavailable") }).use {
            assertEquals(412, it.code)
            assertEquals("body-1", it.body?.string())
        }
        assertEquals(1, server.seen.size)
    }

    @Test
    fun `the retry is not refreshed again if it expires again`() {
        val server = FakeServer(412, 412)
        call(server, refresh = { "new" }).use { assertEquals(412, it.code) }
        assertEquals(1, refreshCalls)
        assertEquals(2, server.seen.size)
    }
}
