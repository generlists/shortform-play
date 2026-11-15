package com.sean.ratel.android.data.library

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class LibraryStoreAndRepositoryTest {
    private lateinit var dir: File

    @Before
    fun setUp() {
        dir =
            File.createTempFile("library-test", "").also {
                it.delete()
                it.mkdirs()
            }
    }

    @After
    fun tearDown() {
        dir.deleteRecursively()
    }

    private class MemoryStore : LibraryStore {
        var saved: LibraryData? = null
        var saves = 0

        override suspend fun load(): LibraryData = saved ?: LibraryData()

        override suspend fun save(data: LibraryData) {
            saved = data
            saves++
        }

        override suspend fun clear() {
            saved = null
        }
    }

    private fun repo(
        store: LibraryStore,
        now: () -> Long = { 1_000L },
    ): LibraryRepository {
        var counter = 0
        return LibraryRepository(store, now) { "id-${counter++}" }
    }

    private fun video(id: String) = VideoSnapshot(id, "제목 $id", "https://i.ytimg.com/vi/$id/hq.jpg", "UC1", "채널", "00:24")

    // ---- 파일 저장소

    @Test
    fun `the file store round trips and starts empty`() =
        runBlocking {
            val store = FileLibraryStore(File(dir, "lib.json"))
            assertEquals(LibraryData(), store.load())
            val data =
                LibraryData(
                    folders = listOf(LibraryFolder("a", "폴더", FolderKind.CUSTOM, 1, false, 10L)),
                    items = listOf(LibraryItem("a", "vid00000001", "제목", "https://x/y.jpg", "UC", "채널", "00:10", 5L, false, 6L)),
                    rev = 9L,
                    dirtyFolders = setOf("a"),
                    dirtyItems = setOf("a/vid00000001"),
                    likesSeeded = true,
                )
            store.save(data)
            assertEquals(data, FileLibraryStore(File(dir, "lib.json")).load())
            assertFalse(File(dir, "lib.json.tmp").exists())
        }

    @Test
    fun `a corrupted or empty file reads as an empty library`() =
        runBlocking {
            val file = File(dir, "lib.json")
            file.writeText("{not json")
            assertEquals(LibraryData(), FileLibraryStore(file).load())
            file.writeText("")
            assertEquals(LibraryData(), FileLibraryStore(file).load())
            FileLibraryStore(file).save(LibraryData(rev = 3))
            FileLibraryStore(file).clear()
            assertFalse(file.exists())
        }

    // ---- 저장소

    @Test
    fun `changes are saved immediately and survive a new repository`() =
        runBlocking {
            val store = MemoryStore()
            val first = repo(store)
            assertEquals(AddResult.Added, first.add(LIKES_FOLDER_ID, video("vid00000001")))
            assertEquals(1, store.saves)
            val second = repo(store)
            assertEquals(listOf("vid00000001"), second.itemsOf(LIKES_FOLDER_ID).first().map { it.videoId })
            assertEquals(setOf(LIKES_FOLDER_ID), second.savedFolderIds("vid00000001").first())
        }

    @Test
    fun `no write happens when nothing changed`() =
        runBlocking {
            val store = MemoryStore()
            val r = repo(store)
            r.add(LIKES_FOLDER_ID, video("vid00000001"))
            val saves = store.saves
            assertEquals(AddResult.AlreadySaved, r.add(LIKES_FOLDER_ID, video("vid00000001")))
            r.remove(LIKES_FOLDER_ID, "nothing-here")
            assertEquals(saves, store.saves)
        }

    @Test
    fun `toggle saves then removes`() =
        runBlocking {
            val r = repo(MemoryStore())
            assertTrue(r.toggle(WATCH_LATER_FOLDER_ID, video("vid00000001")))
            assertEquals(setOf(WATCH_LATER_FOLDER_ID), r.savedFolderIds("vid00000001").first())
            assertFalse(r.toggle(WATCH_LATER_FOLDER_ID, video("vid00000001")))
            assertTrue(r.savedFolderIds("vid00000001").first().isEmpty())
        }

    @Test
    fun `folders can be created renamed and deleted through the repository`() =
        runBlocking {
            val r = repo(MemoryStore())
            val created = r.createFolder("레시피") as FolderResult.Done
            assertEquals(FolderResult.NameTaken, r.createFolder("레시피"))
            r.add(created.folder.id, video("vid00000001"))
            assertEquals(1, r.summaries.first().first { it.folder.id == created.folder.id }.count)
            assertEquals("요리", ((r.rename(created.folder.id, "요리")) as FolderResult.Done).folder.name)
            r.deleteFolder(created.folder.id)
            assertNull(r.folder(created.folder.id).first())
            assertTrue(r.summaries.first().none { it.folder.id == created.folder.id })
        }

    @Test
    fun `user changes announce themselves but sync merges do not`() =
        runBlocking {
            val r = repo(MemoryStore())
            val received = mutableListOf<Unit>()
            // changes 는 이미 지나간 이벤트를 다시 주지 않으므로 먼저 구독해 둔다
            val job = launch(Dispatchers.Unconfined) { r.changes.collect { received += it } }
            try {
                r.add(LIKES_FOLDER_ID, video("vid00000001"))
                assertEquals(1, received.size)
                r.applySync { it.copy(rev = 5) }
                assertEquals(1, received.size)
                r.remove(LIKES_FOLDER_ID, "vid00000001")
                assertEquals(2, received.size)
            } finally {
                job.cancel()
            }
        }

    @Test
    fun `old likes are imported once and never again even after sign out`() =
        runBlocking {
            val r = repo(MemoryStore())
            r.importLikesOnce(listOf(video("vid00000001"), video("vid00000002")))
            assertEquals(
                listOf("vid00000001", "vid00000002"),
                r.itemsOf(LIKES_FOLDER_ID).first().map { it.videoId },
            ) // 받은 순서가 최근순으로 남는다
            r.importLikesOnce(listOf(video("vid00000003")))
            assertEquals(2, r.itemsOf(LIKES_FOLDER_ID).first().size)

            // 로그아웃으로 지워도, 예전 좋아요를 옮겼다는 표시는 남아서 다른 계정으로 다시 올라가지 않는다
            r.clearLocal()
            assertTrue(r.itemsOf(LIKES_FOLDER_ID).first().isEmpty())
            r.importLikesOnce(listOf(video("vid00000004")))
            assertTrue(r.itemsOf(LIKES_FOLDER_ID).first().isEmpty())
            assertTrue(r.snapshot().likesSeeded)
        }

    @Test
    fun `clearing the local library keeps only the system folders`() =
        runBlocking {
            val store = MemoryStore()
            val r = repo(store)
            val folder = r.createFolder("폴더") as FolderResult.Done
            r.add(folder.folder.id, video("vid00000001"))
            r.clearLocal()
            val data = store.saved!!
            assertEquals(listOf(LIKES_FOLDER_ID, WATCH_LATER_FOLDER_ID), data.folders.map { it.id })
            assertTrue(data.items.isEmpty() && data.dirtyFolders.isEmpty() && data.dirtyItems.isEmpty())
        }
}
