package com.sean.ratel.android.data.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryLogicTest {
    private val empty = LibraryLogic.withSystemFolders(LibraryData())

    private fun video(
        id: String,
        title: String = "제목 $id",
    ) = VideoSnapshot(id, title, "https://i.ytimg.com/vi/$id/hq.jpg", "UC1", "채널", "00:24")

    private fun add(
        d: LibraryData,
        folder: String,
        id: String,
        now: Long = 100L,
    ) = LibraryLogic.add(d, folder, video(id), now)

    private fun create(
        d: LibraryData,
        name: String,
        id: String = "f-$name",
        now: Long = 10L,
    ) = LibraryLogic.createFolder(d, name, id, now)

    @Test
    fun `system folders always exist first and are never pushed`() {
        assertEquals(listOf(LIKES_FOLDER_ID, WATCH_LATER_FOLDER_ID), LibraryLogic.visibleFolders(empty).map { it.id })
        assertTrue(LibraryLogic.dirtyRows(empty).first.isEmpty())
        // 서버가 같은 id 를 보내도 무시한다
        val merged =
            LibraryLogic.merge(
                empty,
                listOf(LibraryFolder(LIKES_FOLDER_ID, "다른 이름", FolderKind.LIKES, updatedAt = 999)),
                emptyList(),
                1,
                emptyMap(),
                emptyMap(),
            )
        assertEquals("Likes", merged.folders.first { it.id == LIKES_FOLDER_ID }.name)
    }

    @Test
    fun `adding a video marks it dirty and is idempotent`() {
        val (d1, r1) = add(empty, LIKES_FOLDER_ID, "vid00000001")
        assertEquals(AddResult.Added, r1)
        assertEquals(setOf("likes/vid00000001"), d1.dirtyItems)
        val (d2, r2) = add(d1, LIKES_FOLDER_ID, "vid00000001", now = 200)
        assertEquals(AddResult.AlreadySaved, r2)
        assertEquals(d1, d2)
        assertEquals(listOf("vid00000001"), LibraryLogic.itemsOf(d2, LIKES_FOLDER_ID).map { it.videoId })
    }

    @Test
    fun `a video can be in several folders and removing one leaves the others`() {
        val (withFolder, _) = create(empty, "레시피")
        var d = add(withFolder, LIKES_FOLDER_ID, "vid00000001").first
        d = add(d, "f-레시피", "vid00000001").first
        assertEquals(setOf(LIKES_FOLDER_ID, "f-레시피"), LibraryLogic.savedFolderIds(d, "vid00000001"))
        d = LibraryLogic.remove(d, LIKES_FOLDER_ID, "vid00000001", now = 300)
        assertEquals(setOf("f-레시피"), LibraryLogic.savedFolderIds(d, "vid00000001"))
        // 지운 것은 지운 표시로 남아 올라간다
        assertTrue(d.items.first { it.folderId == LIKES_FOLDER_ID }.deleted)
        assertTrue("likes/vid00000001" in d.dirtyItems)
    }

    @Test
    fun `saving again after removing revives the video with a newer time`() {
        var d = add(empty, LIKES_FOLDER_ID, "vid00000001", now = 100).first
        d = LibraryLogic.remove(d, LIKES_FOLDER_ID, "vid00000001", now = 200)
        val (back, result) = add(d, LIKES_FOLDER_ID, "vid00000001", now = 300)
        assertEquals(AddResult.Added, result)
        val item = back.items.single()
        assertFalse(item.deleted)
        assertEquals(300L, item.savedAt)
        assertEquals(300L, item.updatedAt)
    }

    @Test
    fun `items are listed newest first and removed items are hidden`() {
        var d = add(empty, LIKES_FOLDER_ID, "vid00000001", now = 100).first
        d = add(d, LIKES_FOLDER_ID, "vid00000002", now = 300).first
        d = add(d, LIKES_FOLDER_ID, "vid00000003", now = 200).first
        d = LibraryLogic.remove(d, LIKES_FOLDER_ID, "vid00000003", now = 400)
        assertEquals(listOf("vid00000002", "vid00000001"), LibraryLogic.itemsOf(d, LIKES_FOLDER_ID).map { it.videoId })
    }

    @Test
    fun `no video id or unknown folder saves nothing`() {
        assertEquals(AddResult.NoFolder, LibraryLogic.add(empty, "nope", video("vid00000001"), 1).second)
        assertEquals(AddResult.NoFolder, LibraryLogic.add(empty, LIKES_FOLDER_ID, VideoSnapshot(""), 1).second)
    }

    @Test
    fun `the item limit is enforced but removed items do not count`() {
        var d = empty
        repeat(LibraryLimits.ITEMS_MAX) { d = add(d, LIKES_FOLDER_ID, "vid%08d".format(it)).first }
        assertEquals(AddResult.LimitReached, add(d, LIKES_FOLDER_ID, "vid99999999").second)
        d = LibraryLogic.remove(d, LIKES_FOLDER_ID, "vid00000000", now = 500)
        assertEquals(AddResult.Added, add(d, LIKES_FOLDER_ID, "vid99999999", now = 600).second)
    }

    @Test
    fun `folder names are cleaned and must be unique`() {
        val (d1, r1) = create(empty, "  운동   루틴 ", id = "a")
        assertEquals("운동 루틴", (r1 as FolderResult.Done).folder.name)
        assertEquals(FolderResult.NameTaken, create(d1, "운동 루틴", id = "b").second)
        assertEquals(FolderResult.NameTaken, create(d1, "운동 루틴".uppercase(), id = "b").second)
        assertEquals(FolderResult.NameInvalid, create(d1, "   ", id = "c").second)
        assertEquals(FolderResult.NameInvalid, create(d1, "가".repeat(LibraryLimits.NAME_MAX + 1), id = "c").second)
        assertEquals(FolderResult.NameInvalid, create(d1, "a\u0000b", id = "c").second)
        assertEquals(setOf("a"), d1.dirtyFolders)
    }

    @Test
    fun `custom folders are limited`() {
        var d = empty
        repeat(LibraryLimits.FOLDERS_MAX) { d = create(d, "폴더$it", id = "f$it").first }
        assertEquals(FolderResult.LimitReached, create(d, "하나 더", id = "x").second)
    }

    @Test
    fun `rename keeps the rules and system folders cannot be renamed or deleted`() {
        val (d1, _) = create(empty, "하나", id = "a")
        val (d2, _) = create(d1, "둘", id = "b")
        assertEquals(FolderResult.NameTaken, LibraryLogic.rename(d2, "b", "하나", 50).second)
        assertEquals(FolderResult.NoFolder, LibraryLogic.rename(d2, LIKES_FOLDER_ID, "새 이름", 50).second)
        val (d3, r) = LibraryLogic.rename(d2, "b", "셋", 50)
        assertEquals("셋", (r as FolderResult.Done).folder.name)
        assertEquals(50L, d3.folders.first { it.id == "b" }.updatedAt)
        assertEquals(d3, LibraryLogic.deleteFolder(d3, WATCH_LATER_FOLDER_ID, 60))
    }

    @Test
    fun `deleting a folder marks it and its videos deleted and hides them`() {
        var d = create(empty, "레시피", id = "r").first
        d = add(d, "r", "vid00000001").first
        d = add(d, "r", "vid00000002").first
        d = add(d, LIKES_FOLDER_ID, "vid00000001").first
        d = LibraryLogic.deleteFolder(d, "r", now = 500)
        assertEquals(listOf(LIKES_FOLDER_ID, WATCH_LATER_FOLDER_ID), LibraryLogic.visibleFolders(d).map { it.id })
        assertTrue(d.items.filter { it.folderId == "r" }.all { it.deleted && it.updatedAt == 500L })
        assertEquals(setOf(LIKES_FOLDER_ID), LibraryLogic.savedFolderIds(d, "vid00000001"))
        assertTrue("r" in d.dirtyFolders && "r/vid00000002" in d.dirtyItems)
    }

    @Test
    fun `summaries show counts and a few covers`() {
        var d = empty
        listOf("vid00000001", "vid00000002", "vid00000003", "vid00000004").forEachIndexed { i, id ->
            d = add(d, LIKES_FOLDER_ID, id, now = 100L + i).first
        }
        val likes = LibraryLogic.summaries(d).first { it.folder.id == LIKES_FOLDER_ID }
        assertEquals(4, likes.count)
        assertEquals(3, likes.covers.size)
        assertTrue(likes.covers.first().contains("vid00000004")) // 가장 최근 것이 앞
        assertEquals(0, LibraryLogic.summaries(d).first { it.folder.id == WATCH_LATER_FOLDER_ID }.count)
    }

    @Test
    fun `dirty rows are limited per batch with folders first`() {
        var d = create(empty, "폴더", id = "a").first
        repeat(LibraryLimits.BATCH_MAX + 10) { d = add(d, LIKES_FOLDER_ID, "vid%08d".format(it)).first }
        val (folders, items) = LibraryLogic.dirtyRows(d)
        assertEquals(1, folders.size)
        assertEquals(LibraryLimits.BATCH_MAX - 1, items.size)
        assertEquals(LibraryLimits.BATCH_MAX, folders.size + items.size)
    }

    // ---- merge

    @Test
    fun `merge takes newer or equal server rows and keeps newer local ones`() {
        var d = add(empty, LIKES_FOLDER_ID, "vid00000001", now = 100).first
        d = add(d, LIKES_FOLDER_ID, "vid00000002", now = 300).first
        val server =
            listOf(
                LibraryItem(LIKES_FOLDER_ID, "vid00000001", title = "서버 새 제목", savedAt = 100, updatedAt = 200),
                LibraryItem(LIKES_FOLDER_ID, "vid00000002", title = "서버 옛 제목", savedAt = 100, updatedAt = 250),
                LibraryItem(LIKES_FOLDER_ID, "vid00000009", title = "다른 기기에서 담음", savedAt = 400, updatedAt = 400),
            )
        val merged = LibraryLogic.merge(d, emptyList(), server, serverRev = 5, pushedFolders = emptyMap(), pushedItems = emptyMap())
        val byId = merged.items.associateBy { it.videoId }
        assertEquals("서버 새 제목", byId.getValue("vid00000001").title)
        assertEquals("제목 vid00000002", byId.getValue("vid00000002").title) // 기기 것이 더 새롭다
        assertEquals("다른 기기에서 담음", byId.getValue("vid00000009").title)
        assertEquals(5L, merged.rev)
    }

    @Test
    fun `merge clears dirty for what was pushed and keeps what changed afterwards`() {
        var d = add(empty, LIKES_FOLDER_ID, "vid00000001", now = 100).first
        d = add(d, LIKES_FOLDER_ID, "vid00000002", now = 100).first
        val pushed = mapOf("likes/vid00000001" to 100L, "likes/vid00000002" to 100L)
        // 올리는 동안 두 번째를 지웠다(기기 값이 더 새롭다)
        d = LibraryLogic.remove(d, LIKES_FOLDER_ID, "vid00000002", now = 150)
        val echo =
            listOf(
                LibraryItem(LIKES_FOLDER_ID, "vid00000001", title = "제목 vid00000001", savedAt = 100, updatedAt = 100),
                LibraryItem(LIKES_FOLDER_ID, "vid00000002", title = "제목 vid00000002", savedAt = 100, updatedAt = 100),
            )
        val merged = LibraryLogic.merge(d, emptyList(), echo, 1, emptyMap(), pushed)
        assertEquals(setOf("likes/vid00000002"), merged.dirtyItems) // 지운 것은 아직 못 올렸다
        assertTrue(merged.items.first { it.videoId == "vid00000002" }.deleted)
    }

    @Test
    fun `when the server keeps a newer value the local row converges to it and stops being dirty`() {
        val d = add(empty, LIKES_FOLDER_ID, "vid00000001", now = 100).first
        val server = listOf(LibraryItem(LIKES_FOLDER_ID, "vid00000001", title = "서버 값", savedAt = 100, updatedAt = 900, deleted = true))
        val merged = LibraryLogic.merge(d, emptyList(), server, 3, emptyMap(), mapOf("likes/vid00000001" to 100L))
        assertTrue(merged.items.single().deleted)
        assertTrue(merged.dirtyItems.isEmpty())
    }

    @Test
    fun `mark all dirty after a server rollback pushes everything again`() {
        var d = create(empty, "폴더", id = "a").first
        d = add(d, LIKES_FOLDER_ID, "vid00000001").first
        val clean =
            LibraryLogic.merge(
                d,
                emptyList(),
                emptyList(),
                7,
                d.dirtyFolders.associateWith { 10L },
                d.dirtyItems.associateWith { 100L },
            )
        assertFalse(LibraryLogic.hasDirty(clean))
        val again = LibraryLogic.markAllDirty(clean)
        assertEquals(0L, again.rev)
        assertEquals(setOf("a"), again.dirtyFolders)
        assertEquals(setOf("likes/vid00000001"), again.dirtyItems)
    }

    @Test
    fun `dropping bad dirty rows`() {
        val d = add(empty, LIKES_FOLDER_ID, "vid00000001").first
        assertNull(LibraryLogic.dropDirty(d, emptySet(), setOf("likes/vid00000001")).dirtyItems.firstOrNull())
    }
}
