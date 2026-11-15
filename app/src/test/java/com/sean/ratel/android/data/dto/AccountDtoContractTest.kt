package com.sean.ratel.android.data.dto

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 서버(shortformplay_v2)와 주고받는 JSON 모양이 맞는지 확인한다.
 * 서버는 요청에 모르는 필드가 있으면 422 로 거절하므로(extra=forbid) 키 이름이 정확해야 한다.
 */
class AccountDtoContractTest {
    private val gson = Gson()

    @Test
    fun `save request uses exactly the keys the server accepts`() {
        val json =
            gson.toJsonTree(
                FiltersSaveRequest(
                    version = 3,
                    topics = listOf("mukbang"),
                    keywords = listOf("직캠"),
                    blockedChannels = listOf("UC1"),
                    exceptions = listOf("v1"),
                ),
            ) as JsonObject
        assertEquals(setOf("version", "topics", "keywords", "blocked_channels", "exceptions"), json.keySet())
        assertEquals(3, json.get("version").asInt)
        assertEquals("UC1", json.getAsJsonArray("blocked_channels")[0].asString)
    }

    @Test
    fun `login response from the server parses`() {
        // 서버 tests/test_api.py 가 확인하는 응답 모양
        val body =
            """{"user":{"user_id":7,"provider":"google","email":"a@b.c","display_name":"이름","created_at":"2026-11-13T00:00:00Z"},
               "filters":{"topics":["male_idol"],"keywords":["직캠"],"blocked_channels":["UC1"],"exceptions":["v1"],
               "version":4,"updated_at":"2026-11-13T01:00:00Z"}}"""
        val login = gson.fromJson(body, LoginDto::class.java)
        assertEquals(7L, login.user?.userId)
        assertEquals("이름", login.user?.displayName)
        assertEquals(listOf("male_idol"), login.filters?.topics)
        assertEquals(listOf("UC1"), login.filters?.blockedChannels)
        assertEquals(4, login.filters?.version)
    }

    @Test
    fun `empty filters from a new user parse`() {
        val body =
            """{"user":{"user_id":1,"created_at":"x"},
               "filters":{"topics":[],"keywords":[],"blocked_channels":[],"exceptions":[],"version":0,"updated_at":null}}"""
        val login = gson.fromJson(body, LoginDto::class.java)
        assertEquals(0, login.filters?.version)
        assertEquals(emptyList<String>(), login.filters?.keywords)
        assertNull(login.filters?.updatedAt)
    }

    @Test
    fun `conflict body carries the current filter`() {
        val body =
            """{"success":false,"code":409,"message":"conflict",
               "current":{"topics":["a"],"keywords":[],"blocked_channels":[],"exceptions":[],"version":9,"updated_at":null}}"""
        val conflict = gson.fromJson(body, FiltersConflictDto::class.java)
        assertEquals(409, conflict.code)
        assertEquals(9, conflict.current?.version)
        assertEquals(listOf("a"), conflict.current?.topics)
    }

    @Test
    fun `missing fields do not crash`() {
        val filters = gson.fromJson("""{"version":2}""", FiltersDto::class.java)
        assertEquals(2, filters.version)
        assertNull(filters.topics)
    }
}
