package com.sean.ratel.android.data.library

import android.app.Activity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sean.ratel.android.data.account.AccountSession
import com.sean.ratel.android.data.account.AccountUser
import com.sean.ratel.android.data.account.SignInResult
import com.sean.ratel.android.data.api.youtube.ShortFormPlayV2Api
import com.sean.ratel.android.data.dto.FiltersDto
import com.sean.ratel.android.data.dto.FiltersSaveRequest
import com.sean.ratel.android.data.dto.LibraryFolderDto
import com.sean.ratel.android.data.dto.LibraryItemDto
import com.sean.ratel.android.data.dto.LibrarySyncDto
import com.sean.ratel.android.data.dto.LibrarySyncRequest
import com.sean.ratel.android.data.dto.LoginDto
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.MeDto
import com.sean.ratel.android.data.dto.ShortsChannelModel
import com.sean.ratel.android.data.dto.ShortsVideoModel
import com.sean.ratel.android.data.filter.SyncResult
import com.sean.ratel.android.data.local.pref.YouTubeEndPreference
import com.sean.ratel.android.data.repository.YouTubeEndUserRepository
import com.sean.ratel.android.utils.UIUtil.serializeToJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File

/** 서버와 같은 규칙(행마다 새로운 쪽이 이김·삭제 표시·변경 번호)을 가진 가짜 서버로 두 기기의 동기화를 확인한다. */
class LibrarySyncRepositoryTest {
    private class FakeAccount : AccountSession {
        val users = MutableStateFlow<AccountUser?>(null)
        var signOutCalls = 0

        override val user: Flow<AccountUser?> = users
        override val currentUser: AccountUser? get() = users.value

        override suspend fun idToken(forceRefresh: Boolean): String? = if (users.value == null) null else "token"

        override suspend fun signInWithGoogle(activity: Activity): SignInResult = SignInResult.Cancelled

        override suspend fun signOut() {
            signOutCalls++
            users.value = null
        }

        fun signIn() {
            users.value = AccountUser("uid", "a@b.c", "이름", null, "google.com")
        }
    }

    /** 서버 services/library.py 와 같은 규칙. 두 기기가 같은 서버를 본다. */
    private class FakeServer {
        var rev = 0L
        val requests = mutableListOf<LibrarySyncRequest>()
        private val folders = mutableMapOf<String, Pair<LibraryFolderDto, Long>>()
        private val items = mutableMapOf<String, Pair<LibraryItemDto, Long>>()
        var failWith: Exception? = null
        var httpError: Int? = null

        fun itemsLive() = items.values.map { it.first }.filter { !it.deleted }.map { it.videoId }.sorted()

        fun sync(req: LibrarySyncRequest): Response<LibrarySyncDto> {
            failWith?.let { throw it }
            httpError?.let { return Response.error(it, "".toResponseBody(null)) }
            requests += req
            val newRev = rev + 1
            var changed = false
            req.folders.forEach { f ->
                val old = folders[f.id]
                if (old == null || f.updatedAt.toEpochMs() > old.first.updatedAt.toEpochMs()) {
                    folders[f.id] = f to newRev
                    changed = true
                }
            }
            req.items.forEach { i ->
                val key = "${i.folderId}/${i.videoId}"
                val old = items[key]
                if (old == null || i.updatedAt.toEpochMs() > old.first.updatedAt.toEpochMs()) {
                    items[key] = i to newRev
                    changed = true
                }
            }
            if (changed) rev = newRev
            val sentFolders = req.folders.map { it.id }.toSet()
            val sentItems = req.items.map { "${it.folderId}/${it.videoId}" }.toSet()
            val outFolders = folders.filter { (id, v) -> v.second > req.since || id in sentFolders }.values.map { it.first }
            val outItems = items.filter { (k, v) -> v.second > req.since || k in sentItems }.values.map { it.first }
            return Response.success(LibrarySyncDto(rev, outFolders, outItems))
        }
    }

    private class FakeApi(
        val server: FakeServer,
    ) : ShortFormPlayV2Api {
        override suspend fun login(): LoginDto = LoginDto(MeDto(userId = 1), FiltersDto())

        override suspend fun me(): MeDto = MeDto(userId = 1)

        override suspend fun deleteMe(): Response<Unit> = Response.success(204, Unit)

        override suspend fun getFilters(): FiltersDto = FiltersDto()

        override suspend fun putFilters(body: FiltersSaveRequest): Response<FiltersDto> = Response.success(FiltersDto())

        override suspend fun syncLibrary(body: LibrarySyncRequest): Response<LibrarySyncDto> = server.sync(body)
    }

    /** 한 기기: 자기 기기 저장소·계정·시계를 가진다. */
    private inner class Device(
        val server: FakeServer,
        startTime: Long,
    ) {
        var now = startTime
        val account = FakeAccount()
        private var ids = 0
        val library = LibraryRepository(MemoryStore(), { now }) { "dev${System.identityHashCode(this)}-${ids++}" }
        val dataStore: DataStore<Preferences> = newDataStore()
        val likes = YouTubeEndUserRepository(YouTubeEndPreference(dataStore), library)
        val sync =
            LibrarySyncRepository(FakeApi(server), account, library, likes).also {
                it.retryDelaysMs = listOf(10L, 10L)
                it.debounceMs = 40L
            }

        suspend fun items(folder: String) = library.snapshot().let { LibraryLogic.itemsOf(it, folder) }.map { it.videoId }
    }

    private class MemoryStore : LibraryStore {
        var saved: LibraryData? = null

        override suspend fun load(): LibraryData = saved ?: LibraryData()

        override suspend fun save(data: LibraryData) {
            saved = data
        }

        override suspend fun clear() {
            saved = null
        }
    }

    private lateinit var scope: CoroutineScope
    private lateinit var dir: File
    private var storeCounter = 0

    @Before
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dir =
            File.createTempFile("library-sync-test", "").also {
                it.delete()
                it.mkdirs()
            }
    }

    @After
    fun tearDown() {
        scope.cancel()
        dir.deleteRecursively()
    }

    private fun newDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) { File(dir, "prefs-${storeCounter++}.preferences_pb") }

    private fun video(id: String) = VideoSnapshot(id, "제목 $id", "https://i.ytimg.com/vi/$id/hq.jpg", "UC1", "채널", "00:24")

    @Test
    fun `without a login nothing is sent`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            a.library.add(LIKES_FOLDER_ID, video("vid00000001"))
            assertEquals(SyncResult.NotSignedIn, a.sync.sync())
            assertTrue(server.requests.isEmpty())
            assertTrue(LibraryLogic.hasDirty(a.library.snapshot()))
        }

    @Test
    fun `signed in changes are pushed and the dirty marks clear`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            a.account.signIn()
            a.library.add(LIKES_FOLDER_ID, video("vid00000001"))
            val folder = a.library.createFolder("레시피") as FolderResult.Done
            a.library.add(folder.folder.id, video("vid00000002"))
            assertEquals(SyncResult.Success, a.sync.sync())
            assertEquals(listOf("vid00000001", "vid00000002"), server.itemsLive())
            assertFalse(LibraryLogic.hasDirty(a.library.snapshot()))
            assertEquals(1L, a.library.snapshot().rev)
            // 올릴 것이 없으면 받기만 한다
            assertEquals(SyncResult.Success, a.sync.sync())
            assertTrue(server.requests.last().items.isEmpty() && server.requests.last().folders.isEmpty())
            assertEquals(1L, a.library.snapshot().rev)
        }

    @Test
    fun `a second device gets everything, and later changes flow both ways including deletes`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            val b = Device(server, 1_000)
            a.account.signIn()
            b.account.signIn()

            a.library.add(LIKES_FOLDER_ID, video("vid00000001"))
            a.now = 2_000
            a.library.add(LIKES_FOLDER_ID, video("vid00000002"))
            a.sync.sync()

            b.sync.sync()
            assertEquals(listOf("vid00000002", "vid00000001"), b.items(LIKES_FOLDER_ID))
            assertEquals("제목 vid00000001", b.library.snapshot().items.first { it.videoId == "vid00000001" }.title)

            // B 가 하나를 지우고, A 가 받는다
            b.now = 3_000
            b.library.remove(LIKES_FOLDER_ID, "vid00000001")
            b.sync.sync()
            a.sync.sync()
            assertEquals(listOf("vid00000002"), a.items(LIKES_FOLDER_ID))

            // A 가 더 이른 시각으로 같은 영상을 다시 담으려 해도(시계가 느린 기기) 서버의 삭제가 이긴다
            a.now = 1_500
            a.library.add(LIKES_FOLDER_ID, video("vid00000001"))
            a.sync.sync()
            assertEquals(listOf("vid00000002"), a.items(LIKES_FOLDER_ID))
            assertFalse(LibraryLogic.hasDirty(a.library.snapshot())) // 서버 값으로 맞춰지고 더는 dirty 가 아니다
            assertEquals(listOf("vid00000002"), server.itemsLive())
        }

    @Test
    fun `renaming and deleting a folder on one device reaches the other`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            val b = Device(server, 1_000)
            a.account.signIn()
            b.account.signIn()
            val folder = a.library.createFolder("운동") as FolderResult.Done
            a.library.add(folder.folder.id, video("vid00000001"))
            a.sync.sync()
            b.sync.sync()
            assertEquals(
                listOf("운동"),
                b.library.snapshot().let {
                    LibraryLogic.visibleFolders(it)
                }.filter { it.kind == FolderKind.CUSTOM }.map { it.name },
            )

            a.now = 5_000
            a.library.rename(folder.folder.id, "운동 루틴")
            a.sync.sync()
            b.sync.sync()
            assertEquals("운동 루틴", b.library.snapshot().folders.first { it.id == folder.folder.id }.name)

            b.now = 6_000
            b.library.deleteFolder(folder.folder.id)
            b.sync.sync()
            a.sync.sync()
            assertTrue(LibraryLogic.visibleFolders(a.library.snapshot()).none { it.id == folder.folder.id })
            assertTrue(LibraryLogic.itemsOf(a.library.snapshot(), folder.folder.id).isEmpty())
        }

    @Test
    fun `a network failure keeps the changes and the next sync sends them`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            a.account.signIn()
            a.library.add(LIKES_FOLDER_ID, video("vid00000001"))
            server.failWith = java.io.IOException("offline")
            assertTrue(a.sync.sync() is SyncResult.Failure)
            assertTrue(LibraryLogic.hasDirty(a.library.snapshot()))
            server.httpError = 503
            assertTrue(a.sync.sync() is SyncResult.Failure)
            server.failWith = null
            server.httpError = null
            assertEquals(SyncResult.Success, a.sync.sync())
            assertEquals(listOf("vid00000001"), server.itemsLive())
        }

    @Test
    fun `a row the server could never accept does not block the others`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            a.account.signIn()
            a.library.add(LIKES_FOLDER_ID, video("vid00000001"))
            a.library.add(LIKES_FOLDER_ID, video("bad id!"))
            assertEquals(SyncResult.Success, a.sync.sync())
            assertEquals(listOf("vid00000001"), server.itemsLive())
            assertFalse(LibraryLogic.hasDirty(a.library.snapshot()))
        }

    @Test
    fun `more than one batch is sent in several requests`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            a.account.signIn()
            repeat(LibraryLimits.BATCH_MAX * 2 + 100) { a.library.add(LIKES_FOLDER_ID, video("vid%08d".format(it))) }
            assertEquals(SyncResult.Success, a.sync.sync())
            assertEquals(LibraryLimits.BATCH_MAX * 2 + 100, server.itemsLive().size)
            assertEquals(3, server.requests.size)
            assertTrue(server.requests.all { it.items.size + it.folders.size <= LibraryLimits.BATCH_MAX })
            assertFalse(LibraryLogic.hasDirty(a.library.snapshot()))
        }

    @Test
    fun `when the server was rolled back everything is pushed again`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            a.account.signIn()
            a.library.add(LIKES_FOLDER_ID, video("vid00000001"))
            a.sync.sync()
            assertEquals(1L, a.library.snapshot().rev)

            // 서버가 새것으로 바뀌었다(번호 0, 데이터 없음)
            val fresh = FakeServer()
            val b = LibrarySyncRepository(FakeApi(fresh), a.account, a.library, a.likes).also { it.retryDelaysMs = emptyList() }
            // 서버 번호가 더 작은 상황을 만들려고 기기가 큰 번호를 가진 것처럼 둔다
            a.library.applySync { it.copy(rev = 50) }
            assertEquals(SyncResult.Success, b.sync())
            assertEquals(listOf("vid00000001"), fresh.itemsLive())
            assertEquals(1L, a.library.snapshot().rev)
        }

    @Test
    fun `likes feed the likes folder and old likes are imported once`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            // 이 기기에 예전부터 있던 좋아요
            val old =
                MainShortsModel(
                    shortsChannelModel = ShortsChannelModel("UC1", "", "", "채널", ""),
                    shortsVideoModel = ShortsVideoModel("vid00000007", "https://t.jpg", "예전 좋아요", "", "00:10", "", null, ""),
                )
            a.dataStore.edit { it[stringPreferencesKey("likevid00000007")] = serializeToJson(old) }
            a.dataStore.edit { it[stringPreferencesKey("disLikevid00000008")] = serializeToJson(old) } // 싫어요는 해당 없음
            a.dataStore.edit { it[stringPreferencesKey("likeSomethingElse")] = "not a video" } // 읽을 수 없는 값
            a.sync.importLikes()
            assertEquals(listOf("vid00000007"), a.items(LIKES_FOLDER_ID))
            a.sync.importLikes()
            assertEquals(1, a.library.snapshot().items.size)

            // 새로 누른 좋아요/취소
            val fresh =
                MainShortsModel(shortsVideoModel = ShortsVideoModel("vid00000009", "https://u.jpg", "새 영상", "", "00:20", "", null, ""))
            a.likes.likeDisLike("likevid00000009", fresh)
            assertEquals(setOf("vid00000009", "vid00000007"), a.items(LIKES_FOLDER_ID).toSet())
            a.likes.likeDisLike("disLikevid00000009", fresh) // 싫어요 키는 좋아요 폴더에 영향 없음
            assertEquals(2, a.items(LIKES_FOLDER_ID).size)
            a.likes.cancelLikeDisLike("likevid00000009")
            assertEquals(listOf("vid00000007"), a.items(LIKES_FOLDER_ID))
        }

    @Test
    fun `signing out flushes once and then clears the device`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            a.account.signIn()
            a.library.add(LIKES_FOLDER_ID, video("vid00000001"))
            assertEquals(SyncResult.Success, a.sync.flushBeforeSignOut())
            a.sync.clearLocal()
            assertTrue(a.library.snapshot().items.isEmpty())
            assertEquals(listOf("vid00000001"), server.itemsLive()) // 서버에는 남아 있다
            // 다시 로그인하면 서버에서 돌려받는다
            a.account.signIn()
            a.sync.sync()
            assertEquals(listOf("vid00000001"), a.items(LIKES_FOLDER_ID))
        }

    @Test
    fun `start syncs on login and after changes with a short delay`() =
        runBlocking {
            val server = FakeServer()
            val a = Device(server, 1_000)
            val job = a.sync.start(scope)
            try {
                a.library.add(LIKES_FOLDER_ID, video("vid00000001")) // 로그인 전: 기기에만
                delay(150)
                assertTrue(server.requests.isEmpty())

                a.account.signIn() // 로그인하면 올라간다
                withTimeout(3_000) { while (server.itemsLive().isEmpty()) delay(20) }
                assertEquals(listOf("vid00000001"), server.itemsLive())

                a.library.add(LIKES_FOLDER_ID, video("vid00000002")) // 바꾸면 잠깐 뒤 올라간다
                withTimeout(3_000) { while (server.itemsLive().size < 2) delay(20) }
                assertEquals(listOf("vid00000001", "vid00000002"), server.itemsLive())
            } finally {
                job.cancel()
            }
        }
}
