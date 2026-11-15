package com.sean.ratel.android.data.library

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sean.ratel.android.data.dto.LibrarySyncDto
import com.sean.ratel.android.data.dto.LibrarySyncRequest
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.ShortsChannelModel
import com.sean.ratel.android.data.dto.ShortsVideoModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 서버(shortformplay_v2)와 주고받는 보관함 JSON 모양과 변환이 맞는지 확인한다. 서버는 요청의 모르는 키를 422 로 거절한다(extra=forbid). */
class LibraryDtoContractTest {
    private val gson = Gson()

    @Test
    fun `request uses exactly the keys the server accepts`() {
        val item = LibraryItem("f1", "vid00000001", "제목", "https://i.ytimg.com/x.jpg", "UC1", "채널", "00:24", 1_000L, false, 2_000L)
        val folder = LibraryFolder("f1", "폴더", FolderKind.CUSTOM, 2, false, 3_000L)
        val json = gson.toJsonTree(LibrarySyncRequest(7L, listOf(folder.toDto()), listOf(item.toDto()))) as JsonObject
        assertEquals(setOf("since", "folders", "items"), json.keySet())
        assertEquals(
            setOf("id", "name", "kind", "position", "deleted", "updated_at"),
            json.getAsJsonArray("folders")[0].asJsonObject.keySet(),
        )
        assertEquals(
            setOf(
                "folder_id", "video_id", "title", "thumbnail", "channel_id",
                "channel_title", "duration", "saved_at", "deleted", "updated_at",
            ),
            json.getAsJsonArray("items")[0].asJsonObject.keySet(),
        )
        assertEquals("custom", json.getAsJsonArray("folders")[0].asJsonObject.get("kind").asString)
        assertEquals("1970-01-01T00:00:01Z", json.getAsJsonArray("items")[0].asJsonObject.get("saved_at").asString)
    }

    @Test
    fun `server response parses including both time formats and missing lists`() {
        val body =
            """{"rev":4,"folders":[{"id":"f1","name":"폴더","kind":"custom","position":1,"deleted":false,"updated_at":"2026-11-14T10:00:00Z"}],
               "items":[{"folder_id":"f1","video_id":"vid00000001","title":"t","thumbnail":"","channel_id":"UC","channel_title":"c",
               "duration":"00:10","saved_at":"2026-11-14T10:00:00.123456Z","deleted":true,"updated_at":"2026-11-14T19:00:00+09:00"}]}"""
        val dto = gson.fromJson(body, LibrarySyncDto::class.java)
        assertEquals(4L, dto.rev)
        val folder = dto.folders!![0].toFolder()
        assertEquals(FolderKind.CUSTOM, folder.kind)
        assertEquals(1_794_650_400_000L, folder.updatedAt)
        val item = dto.items!![0].toItem()
        assertTrue(item.deleted)
        assertEquals(1_794_650_400_000L, item.updatedAt) // +09:00 도 같은 순간
        assertEquals(1_794_650_400_123L, item.savedAt)
        assertEquals(0L, gson.fromJson("""{"rev":1}""", LibrarySyncDto::class.java).folders.orEmpty().size.toLong())
        assertEquals(0L, "not a date".toEpochMs())
        assertEquals(0L, null.toEpochMs())
    }

    @Test
    fun `a video row is cleaned so the server never rejects the whole batch`() {
        val dirty =
            LibraryItem(
                "f1", "vid00000001", "  두   칸\u0000제목 " + "가".repeat(400), "javascript:alert(1)", "UC" + "9".repeat(100),
                "채널 " + "나".repeat(200), "00:24:00:00:00:00:00:00", 1L, false, 2L,
            ).toDto()
        assertEquals(300, dirty.title.length)
        assertFalse(dirty.title.contains('\u0000'))
        assertTrue(dirty.title.startsWith("두 칸제목"))
        assertEquals("", dirty.thumbnail) // http(s) 가 아니면 비운다
        assertEquals(64, dirty.channelId.length)
        assertEquals(120, dirty.channelTitle.length)
        assertEquals(16, dirty.duration.length)
    }

    @Test
    fun `rows the server could never accept are recognised`() {
        assertTrue(LibraryItem("f1", "vid00000001").isValidForServer())
        assertFalse(LibraryItem("f1", "a b c d e").isValidForServer())
        assertFalse(LibraryItem("f1", "abc").isValidForServer())
        assertFalse(LibraryItem("한글", "vid00000001").isValidForServer())
        assertTrue(LibraryFolder("f1", "폴더", FolderKind.CUSTOM).isValidForServer())
        assertFalse(LibraryFolder("f1", " ", FolderKind.CUSTOM).isValidForServer())
    }

    @Test
    fun `playback models convert both ways`() {
        val model =
            MainShortsModel(
                shortsChannelModel = ShortsChannelModel("UC1", "https://thumb", "2020", "채널", "설명"),
                shortsVideoModel = ShortsVideoModel("vid00000001", "https://t.jpg", "제목", "설명", "00:24", "c", null, "cn"),
            )
        val snapshot = model.toSnapshot()!!
        assertEquals(VideoSnapshot("vid00000001", "제목", "https://t.jpg", "UC1", "채널", "00:24"), snapshot)
        val item =
            LibraryItem(
                "f1",
                snapshot.videoId,
                snapshot.title,
                snapshot.thumbnail,
                snapshot.channelId,
                snapshot.channelTitle,
                snapshot.duration,
            )
        val back = item.toMainShortsModel()
        assertEquals("vid00000001", back.shortsVideoModel?.videoId)
        assertEquals("채널", back.shortsChannelModel?.channelTitle)
        assertNull(MainShortsModel().toSnapshot())
        assertNull(MainShortsModel(shortsVideoModel = ShortsVideoModel("", "", "", "", "", "", null, "")).toSnapshot())
    }
}
