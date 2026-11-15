package com.sean.ratel.android.data.filter

import android.app.Activity
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.sean.ratel.android.data.account.AccountSession
import com.sean.ratel.android.data.account.AccountUser
import com.sean.ratel.android.data.account.SignInResult
import com.sean.ratel.android.data.api.youtube.ShortFormPlayV2Api
import com.sean.ratel.android.data.dto.FiltersDto
import com.sean.ratel.android.data.dto.FiltersSaveRequest
import com.sean.ratel.android.data.dto.LibrarySyncDto
import com.sean.ratel.android.data.dto.LibrarySyncRequest
import com.sean.ratel.android.data.dto.LoginDto
import com.sean.ratel.android.data.dto.MeDto
import com.sean.ratel.android.data.local.pref.FilterPreference
import com.sean.ratel.android.data.local.pref.RecentVideoPreference
import com.sean.ratel.android.data.local.pref.YouTubeEndPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File
import java.io.IOException

/** 로그인·서버 없이 가짜 계정과 가짜 API 로 동기화 규칙을 확인한다. 필터 저장은 실제 DataStore(임시 파일). */
class FilterSyncRepositoryTest {
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

    private class FakeApi : ShortFormPlayV2Api {
        var server = FiltersDto(version = 0)
        var loginError: Exception? = null
        var putError: Exception? = null
        var conflictsToReturn = 0
        var deleteCode = 204
        val puts = mutableListOf<FiltersSaveRequest>()
        var logins = 0

        override suspend fun login(): LoginDto {
            logins++
            loginError?.let { throw it }
            return LoginDto(MeDto(userId = 1), server)
        }

        override suspend fun me(): MeDto = MeDto(userId = 1)

        override suspend fun deleteMe(): Response<Unit> =
            if (deleteCode in 200..299) {
                Response.success(deleteCode, Unit)
            } else {
                Response.error<Unit>(deleteCode, "".toResponseBody(null))
            }

        override suspend fun getFilters(): FiltersDto = server

        override suspend fun syncLibrary(body: LibrarySyncRequest): Response<LibrarySyncDto> = Response.success(LibrarySyncDto())

        override suspend fun putFilters(body: FiltersSaveRequest): Response<FiltersDto> {
            puts += body
            putError?.let { throw it }
            if (body.version != server.version || conflictsToReturn > 0) {
                if (conflictsToReturn > 0) conflictsToReturn--
                val json = """{"success":false,"code":409,"message":"conflict","current":${currentJson()}}"""
                return Response.error(409, json.toResponseBody("application/json".toMediaType()))
            }
            server =
                FiltersDto(
                    topics = body.topics,
                    keywords = body.keywords,
                    blockedChannels = body.blockedChannels,
                    exceptions = body.exceptions,
                    version = server.version + 1,
                )
            return Response.success(server)
        }

        private fun currentJson() =
            """{"topics":${arr(server.topics)},"keywords":${arr(server.keywords)},"blocked_channels":${arr(server.blockedChannels)},""" +
                """"exceptions":${arr(server.exceptions)},"version":${server.version}}"""

        private fun arr(list: List<String>?) = (list ?: emptyList()).joinToString(",", "[", "]") { "\"$it\"" }
    }

    private lateinit var scope: CoroutineScope
    private lateinit var file: File
    private lateinit var filters: FilterRepository
    private lateinit var account: FakeAccount
    private lateinit var api: FakeApi
    private lateinit var sync: FilterSyncRepository

    @Before
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        file = File.createTempFile("filter-sync-test", ".preferences_pb").also { it.delete() }
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        filters =
            FilterRepository(
                FilterPreference(dataStore),
                TopicPackRepository { null },
                RecentVideoPreference(dataStore),
                YouTubeEndPreference(dataStore),
            )
        account = FakeAccount()
        api = FakeApi()
        sync = FilterSyncRepository(api, account, filters)
    }

    @After
    fun tearDown() {
        scope.cancel()
        file.delete()
    }

    private fun <T> run(block: suspend () -> T): T = runBlocking { withTimeout(10_000) { block() } }

    private fun state() = run { filters.state.first() }

    @Test
    fun `nothing is saved or synced when signed out`() =
        run {
            assertEquals(SyncResult.NotSignedIn, sync.reconcile())
            assertEquals(SyncResult.NotSignedIn, sync.saveFromUser(ExcludeFilter(keywords = listOf("직캠")), FilterOptions()))
            assertEquals(FilterState(), filters.state.first())
            assertTrue(api.puts.isEmpty())
            assertEquals(SyncResult.NotSignedIn, sync.deleteAccount())
        }

    @Test
    fun `reconcile adopts the server filter and keeps local options`() =
        run {
            account.signIn()
            filters.save(FilterState(options = FilterOptions(hideDisliked = true)))
            api.server = FiltersDto(topics = listOf("mukbang"), keywords = listOf("직캠"), version = 3)

            assertEquals(SyncResult.Success, sync.reconcile())
            val s = filters.state.first()
            assertEquals(ExcludeFilter(topics = listOf("mukbang"), keywords = listOf("직캠"), version = 3), s.filter)
            assertEquals(FilterOptions(hideDisliked = true), s.options)
            assertEquals(false, s.dirty)
        }

    @Test
    fun `saveFromUser stores locally and uploads with the known version`() =
        run {
            account.signIn()
            api.server = FiltersDto(keywords = listOf("old"), version = 2)
            sync.reconcile()

            val wanted = ExcludeFilter(topics = listOf("politics"), keywords = listOf("직캠"))
            val result = sync.saveFromUser(wanted, FilterOptions(hideDisliked = true))
            assertEquals(SyncResult.Success, result)
            assertEquals(2, api.puts.single().version)
            val s = filters.state.first()
            assertEquals(3, s.filter.version)
            assertEquals(listOf("politics"), s.filter.topics)
            assertEquals(false, s.dirty)
            assertEquals(true, s.options.hideDisliked)
        }

    @Test
    fun `failed upload keeps the change and the next reconcile uploads it`() =
        run {
            account.signIn()
            api.putError = IOException("offline")
            val first = sync.saveFromUser(ExcludeFilter(keywords = listOf("직캠")), FilterOptions())
            assertTrue(first is SyncResult.Failure)
            val s = filters.state.first()
            assertEquals(listOf("직캠"), s.filter.keywords) // 기기에서는 바로 적용된다
            assertEquals(true, s.dirty)

            api.putError = null
            assertEquals(SyncResult.Success, sync.reconcile())
            assertEquals(false, filters.state.first().dirty)
            assertEquals(listOf("직캠"), api.server.keywords)
        }

    @Test
    fun `conflict retries once with the latest version and the last save wins`() =
        run {
            account.signIn()
            sync.reconcile() // 로컬 version 0
            // 다른 기기가 먼저 저장해 서버가 version 4 가 됐다
            api.server = FiltersDto(keywords = listOf("other-device"), version = 4)

            val result = sync.saveFromUser(ExcludeFilter(keywords = listOf("mine")), FilterOptions())
            assertEquals(SyncResult.Success, result)
            assertEquals(listOf(0, 4), api.puts.map { it.version })
            assertEquals(listOf("mine"), api.server.keywords)
            assertEquals(5, filters.state.first().filter.version)
        }

    @Test
    fun `repeated conflicts give up and stay dirty`() =
        run {
            account.signIn()
            sync.reconcile()
            api.conflictsToReturn = 2
            val result = sync.saveFromUser(ExcludeFilter(keywords = listOf("mine")), FilterOptions())
            assertTrue(result is SyncResult.Failure)
            assertEquals(2, api.puts.size)
            assertEquals(true, filters.state.first().dirty)
        }

    @Test
    fun `dirty local changes are merged with a newer server filter`() =
        run {
            account.signIn()
            api.putError = IOException("offline")
            sync.saveFromUser(ExcludeFilter(keywords = listOf("local-only")), FilterOptions())
            api.putError = null
            // 그사이 다른 기기가 저장했다
            api.server = FiltersDto(keywords = listOf("from-server"), version = 7)

            assertEquals(SyncResult.Success, sync.reconcile())
            assertEquals(listOf("from-server", "local-only"), api.server.keywords)
            val s = filters.state.first()
            assertEquals(8, s.filter.version)
            assertEquals(false, s.dirty)
        }

    @Test
    fun `login failure leaves the local filter untouched`() =
        run {
            account.signIn()
            api.server = FiltersDto(keywords = listOf("직캠"), version = 1)
            sync.reconcile()
            api.loginError = IOException("offline")
            assertTrue(sync.reconcile() is SyncResult.Failure)
            assertEquals(listOf("직캠"), filters.state.first().filter.keywords)
        }

    @Test
    fun `signOut clears the local filter and signs out`() =
        run {
            account.signIn()
            sync.saveFromUser(ExcludeFilter(keywords = listOf("직캠")), FilterOptions(hideDisliked = true))
            sync.signOut()
            assertEquals(FilterState(), filters.state.first())
            assertEquals(1, account.signOutCalls)
            // 서버의 필터는 그대로 — 다시 로그인하면 돌아온다
            assertEquals(listOf("직캠"), api.server.keywords)
        }

    @Test
    fun `deleteAccount clears on success and keeps everything on failure`() =
        run {
            account.signIn()
            sync.saveFromUser(ExcludeFilter(keywords = listOf("직캠")), FilterOptions())

            api.deleteCode = 500
            assertTrue(sync.deleteAccount() is SyncResult.Failure)
            assertEquals(listOf("직캠"), filters.state.first().filter.keywords)
            assertEquals(0, account.signOutCalls)

            api.deleteCode = 204
            assertEquals(SyncResult.Success, sync.deleteAccount())
            assertEquals(FilterState(), filters.state.first())
            assertEquals(1, account.signOutCalls)
        }

    @Test
    fun `start clears a stale filter when signed out and reconciles when signed in`() =
        run {
            // 로그인 상태가 아닌데 기기에 필터가 남아 있으면 지운다
            filters.save(FilterState(ExcludeFilter(keywords = listOf("stale"))))
            val job = sync.start(scope)
            filters.state.first { it == FilterState() }

            // 로그인하면 서버에서 받아온다
            api.server = FiltersDto(topics = listOf("news"), version = 1)
            account.signIn()
            filters.state.first { it.filter.topics == listOf("news") }
            assertEquals(1, api.logins)
            job.cancel()
        }

    @Test
    fun `start retries a failed login until the server accepts`() =
        run {
            // 앱을 막 켰을 때처럼 처음 몇 번은 서버가 거절한다(Integrity 토큰이 아직 없음)
            account.signIn()
            api.loginError = IOException("not ready")
            api.server = FiltersDto(topics = listOf("news"), version = 2)
            sync.retryDelaysMs = List(6) { 40L }
            val job = sync.start(scope)

            delay(50)
            api.loginError = null
            filters.state.first { it.filter.topics == listOf("news") }
            assertTrue(api.logins >= 2)
            job.cancel()
        }

    @Test
    fun `uploaded values are sanitized to the server limits`() =
        run {
            account.signIn()
            sync.saveFromUser(
                ExcludeFilter(topics = listOf("ok", "not valid"), keywords = listOf(" 직캠 ", "직캠")),
                FilterOptions(),
            )
            val sent = api.puts.single()
            assertEquals(listOf("ok"), sent.topics)
            assertEquals(listOf("직캠"), sent.keywords)
        }
}
