package com.sean.ratel.android.data.library

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 내 보관함(폴더와 담은 영상) — 기기에 저장하고, 로그인하면 [LibrarySyncRepository] 가 서버와 맞춘다.
 * 비로그인 사용자도 기기 안에서는 쓸 수 있다. 모든 변경은 한 줄로 처리하고 바꾼 뒤 바로 저장한다.
 */
@Singleton
class LibraryRepository internal constructor(
    private val store: LibraryStore,
    private val clock: () -> Long,
    private val newId: () -> String,
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : this(FileLibraryStore(File(context.filesDir, FILE_NAME)), System::currentTimeMillis, { UUID.randomUUID().toString() })

    private val mutex = Mutex()
    private val state = MutableStateFlow<LibraryData?>(null)

    private val _changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** 사용자가 보관함을 바꿀 때마다 한 번씩 내보낸다(서버와 맞추는 쪽이 모아서 쓴다). 서버와 맞추는 변경은 내보내지 않는다. */
    val changes: SharedFlow<Unit> = _changes

    val data: Flow<LibraryData> =
        flow {
            snapshot()
            emitAll(state.filterNotNull())
        }

    val summaries: Flow<List<FolderSummary>> = data.map { LibraryLogic.summaries(it) }.distinctUntilChanged()

    fun itemsOf(folderId: String): Flow<List<LibraryItem>> = data.map { LibraryLogic.itemsOf(it, folderId) }.distinctUntilChanged()

    fun folder(folderId: String): Flow<LibraryFolder?> =
        data.map { d -> d.folders.firstOrNull { it.id == folderId && !it.deleted } }.distinctUntilChanged()

    /** 이 영상이 담긴 폴더 id 들. */
    fun savedFolderIds(videoId: String): Flow<Set<String>> = data.map { LibraryLogic.savedFolderIds(it, videoId) }.distinctUntilChanged()

    suspend fun snapshot(): LibraryData = mutex.withLock { loadedLocked() }

    private suspend fun loadedLocked(): LibraryData {
        state.value?.let { return it }
        val loaded = LibraryLogic.withSystemFolders(store.load())
        state.value = loaded
        return loaded
    }

    private suspend fun <T> transaction(
        notify: Boolean = true,
        block: (LibraryData) -> Pair<LibraryData, T>,
    ): T =
        mutex.withLock {
            val current = loadedLocked()
            val (next, result) = block(current)
            if (next != current) {
                state.value = next
                store.save(next)
                if (notify) _changes.tryEmit(Unit)
            }
            result
        }

    suspend fun add(
        folderId: String,
        video: VideoSnapshot,
    ): AddResult = transaction { LibraryLogic.add(it, folderId, video, clock()) }

    suspend fun remove(
        folderId: String,
        videoId: String,
    ) = transaction { LibraryLogic.remove(it, folderId, videoId, clock()) to Unit }

    /** 이 영상을 [folderId] 폴더에 담거나(없으면) 빼낸다(있으면). 담겼으면 true. */
    suspend fun toggle(
        folderId: String,
        video: VideoSnapshot,
    ): Boolean =
        transaction { data ->
            val saved = LibraryLogic.savedFolderIds(data, video.videoId)
            if (folderId in saved) {
                LibraryLogic.remove(data, folderId, video.videoId, clock()) to false
            } else {
                val (next, _) = LibraryLogic.add(data, folderId, video, clock())
                next to true
            }
        }

    suspend fun createFolder(name: String): FolderResult = transaction { LibraryLogic.createFolder(it, name, newId(), clock()) }

    suspend fun rename(
        folderId: String,
        name: String,
    ): FolderResult = transaction { LibraryLogic.rename(it, folderId, name, clock()) }

    suspend fun deleteFolder(folderId: String) = transaction { LibraryLogic.deleteFolder(it, folderId, clock()) to Unit }

    /**
     * 처음 한 번, 이 기기에 있던 좋아요를 "좋아요" 폴더로 옮긴다. 이미 옮겼으면 아무것도 하지 않는다.
     * 로그아웃해도 이 표시는 지우지 않는다 — 다른 계정으로 로그인했을 때 이 기기의 예전 좋아요가 그 계정으로 올라가지 않게.
     */
    suspend fun importLikesOnce(likes: List<VideoSnapshot>) =
        transaction { data ->
            if (data.likesSeeded) return@transaction data to Unit
            var next = data
            val now = clock()
            likes.forEachIndexed { index, video ->
                // 최근에 누른 순서를 알 수 없으니 받은 순서를 시간 차로 남긴다
                next = LibraryLogic.add(next, LIKES_FOLDER_ID, video, now - index).first
            }
            next.copy(likesSeeded = true) to Unit
        }

    /** 서버와 맞춘 결과를 반영한다(사용자 변경이 아니므로 다시 동기화를 부르지 않는다). */
    internal suspend fun applySync(block: (LibraryData) -> LibraryData) = transaction(notify = false) { block(it) to Unit }

    /** 로그아웃·탈퇴 — 기기의 보관함을 지운다(좋아요를 옮겼다는 표시는 남긴다). */
    suspend fun clearLocal() =
        mutex.withLock {
            val seeded = loadedLocked().likesSeeded
            val empty = LibraryLogic.withSystemFolders(LibraryData(likesSeeded = seeded))
            state.value = empty
            store.save(empty)
        }

    companion object {
        const val FILE_NAME = "library_v1.json"
    }
}
