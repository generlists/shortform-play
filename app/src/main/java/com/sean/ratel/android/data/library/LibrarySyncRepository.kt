package com.sean.ratel.android.data.library

import com.sean.ratel.android.data.account.AccountSession
import com.sean.ratel.android.data.api.youtube.ShortFormPlayV2Api
import com.sean.ratel.android.data.dto.LibrarySyncRequest
import com.sean.ratel.android.data.filter.SyncResult
import com.sean.ratel.android.data.repository.YouTubeEndUserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import so.smartlab.common.utils.log.RLog
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 보관함을 서버(/shortformplay/v2/me/library/sync)와 맞춘다.
 *
 * - 로그인하지 않으면 기기 안에서만 쓴다. 로그인(또는 앱 시작 때 로그인 상태)하면 올리지 못한 변경을 올리고 서버 변경을 받아 합친다.
 * - 보관함을 바꾸면 잠깐(2초) 모았다가 올린다. 실패하면 변경은 dirty 로 남아 다음에 다시 올린다.
 * - 행마다 더 최근에 바꾼 쪽이 이긴다(서버와 같은 규칙, [LibraryLogic.merge]).
 * - 로그아웃할 때는 마지막으로 한 번 올려 본 뒤 기기의 보관함을 지운다(다른 계정으로 로그인했을 때 올라가지 않게).
 */
@Singleton
class LibrarySyncRepository
    @Inject
    constructor(
        private val api: ShortFormPlayV2Api,
        private val account: AccountSession,
        private val library: LibraryRepository,
        private val likes: YouTubeEndUserRepository,
    ) {
        private val mutex = Mutex()

        /** 앱을 막 켰을 때는 Integrity 토큰이 아직 없어 서버가 411 로 거절하므로 잠시 뒤 다시 한다(필터 동기화와 같다). 테스트에서 줄여 쓴다. */
        internal var retryDelaysMs: List<Long> = listOf(3_000L, 8_000L, 20_000L, 45_000L)
        internal var debounceMs: Long = 2_000L

        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
        fun start(scope: CoroutineScope): Job =
            scope.launch {
                launch { importLikes() }
                launch {
                    account.user.collectLatest { user -> if (user != null) syncWithRetry() }
                }
                launch {
                    library.changes.debounce(debounceMs).collect { if (account.currentUser != null) sync() }
                }
            }

        /** 이 기기의 예전 좋아요를 "좋아요" 폴더로 옮긴다(처음 한 번). */
        suspend fun importLikes() {
            runCatching {
                library.importLikesOnce(likes.likedVideos().mapNotNull { it.toSnapshot() })
            }.onFailure { RLog.e(TAG, "좋아요를 보관함으로 옮기지 못했다: ${it.message}") }
        }

        private suspend fun syncWithRetry() {
            var result = sync()
            for (delayMs in retryDelaysMs) {
                if (result !is SyncResult.Failure) return
                delay(delayMs)
                result = sync()
            }
        }

        /** 올릴 것을 올리고 서버의 변경을 받아 합친다. 한 번에 [LibraryLimits.BATCH_MAX] 행씩 올리고, 남으면 이어서 올린다. */
        suspend fun sync(): SyncResult =
            mutex.withLock {
                if (account.currentUser == null) return@withLock SyncResult.NotSignedIn
                var rolledBack = false
                while (true) {
                    val data = library.snapshot()
                    val (folders, items) = LibraryLogic.dirtyRows(data)

                    // 서버가 받아 주지 않는 형식의 행은 dirty 에서 빼서, 계속 걸려 다른 행을 막지 않게 한다
                    val badFolders = folders.filterNot { it.isValidForServer() }.map { it.id }.toSet()
                    val badItems = items.filterNot { it.isValidForServer() }.map { it.key }.toSet()
                    if (badFolders.isNotEmpty() || badItems.isNotEmpty()) {
                        library.applySync { LibraryLogic.dropDirty(it, badFolders, badItems) }
                        continue
                    }

                    val response =
                        try {
                            api.syncLibrary(LibrarySyncRequest(data.rev, folders.map { it.toDto() }, items.map { it.toDto() }))
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            RLog.e(TAG, "보관함 동기화 실패: ${e.javaClass.simpleName} ${e.message}")
                            return@withLock SyncResult.Failure(e.javaClass.simpleName)
                        }
                    if (!response.isSuccessful) {
                        RLog.e(TAG, "보관함 동기화 실패: http ${response.code()}")
                        return@withLock SyncResult.Failure("http ${response.code()}")
                    }
                    val body = response.body() ?: return@withLock SyncResult.Failure("empty body")

                    // 서버 번호가 우리가 받은 번호보다 작으면 서버가 되돌려진 것이다 — 가진 것을 전부 다시 올린다(한 번만)
                    if (body.rev < data.rev && !rolledBack) {
                        rolledBack = true
                        library.applySync { LibraryLogic.markAllDirty(it) }
                        continue
                    }

                    library.applySync {
                        LibraryLogic.merge(
                            it,
                            body.folders.orEmpty().map { f -> f.toFolder() },
                            body.items.orEmpty().map { i -> i.toItem() },
                            body.rev,
                            folders.associate { f -> f.id to f.updatedAt },
                            items.associate { i -> i.key to i.updatedAt },
                        )
                    }

                    // 남은 변경이 있고 이번에 줄었으면 이어서 올린다
                    val after = library.snapshot()
                    val before = data.dirtyFolders.size + data.dirtyItems.size
                    val now = after.dirtyFolders.size + after.dirtyItems.size
                    if (!LibraryLogic.hasDirty(after) || now >= before) break
                }
                SyncResult.Success
            }

        /** 로그아웃 직전: 올리지 못한 변경을 한 번 올려 본다(실패해도 로그아웃은 진행). */
        suspend fun flushBeforeSignOut(): SyncResult = sync()

        /** 로그아웃·탈퇴 뒤: 기기의 보관함을 지운다. */
        suspend fun clearLocal() = mutex.withLock { library.clearLocal() }

        private companion object {
            const val TAG = "LibrarySync"
        }
    }
