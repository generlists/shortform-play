package com.sean.ratel.android.ui.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.library.AddResult
import com.sean.ratel.android.data.library.FolderResult
import com.sean.ratel.android.data.library.FolderSummary
import com.sean.ratel.android.data.library.LIKES_FOLDER_ID
import com.sean.ratel.android.data.library.LibraryFolder
import com.sean.ratel.android.data.library.LibraryItem
import com.sean.ratel.android.data.library.LibraryRepository
import com.sean.ratel.android.data.library.WATCH_LATER_FOLDER_ID
import com.sean.ratel.android.data.library.toSnapshot
import com.sean.ratel.android.data.log.GAAccountAnalytics
import com.sean.ratel.android.data.log.GALog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 보관함 화면(마이 탭의 폴더, 폴더 상세, 재생 화면의 저장 시트)이 같이 쓴다. 안내 문구는 [messages] 로 한 번씩 내보낸다. */
enum class LibraryMessage { ITEM_LIMIT, FOLDER_LIMIT, NAME_TAKEN, NAME_INVALID }

@HiltViewModel
class LibraryViewModel
    @Inject
    constructor(
        private val library: LibraryRepository,
        private val gaLog: GALog,
    ) : ViewModel() {
        val folders: StateFlow<List<FolderSummary>> =
            library.summaries.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        private val _messages = MutableSharedFlow<LibraryMessage>(extraBufferCapacity = MESSAGE_BUFFER)
        val messages: SharedFlow<LibraryMessage> = _messages.asSharedFlow()

        fun items(folderId: String): Flow<List<LibraryItem>> = library.itemsOf(folderId)

        fun folder(folderId: String): Flow<LibraryFolder?> = library.folder(folderId)

        fun savedFolderIds(videoId: String): Flow<Set<String>> = library.savedFolderIds(videoId)

        /** [video] 를 [folderId] 폴더에 담거나(없으면) 뺀다. */
        fun toggle(
            folderId: String,
            video: MainShortsModel?,
        ) {
            val snapshot = video?.toSnapshot() ?: return
            viewModelScope.launch {
                if (folderId in library.savedFolderIds(snapshot.videoId).first()) {
                    library.remove(folderId, snapshot.videoId)
                    track(GAAccountAnalytics.Event.LIBRARY_REMOVE, folderType(folderId))
                } else if (library.add(folderId, snapshot) == AddResult.LimitReached) {
                    _messages.emit(LibraryMessage.ITEM_LIMIT)
                } else {
                    track(GAAccountAnalytics.Event.LIBRARY_SAVE, folderType(folderId))
                }
            }
        }

        fun remove(
            folderId: String,
            videoId: String,
        ) {
            viewModelScope.launch {
                library.remove(folderId, videoId)
                track(GAAccountAnalytics.Event.LIBRARY_REMOVE, folderType(folderId))
            }
        }

        /** 새 폴더. 만들면 [onCreated] 로 알려 준다(저장 시트에서 바로 담을 때 쓴다). */
        fun createFolder(
            name: String,
            onCreated: (LibraryFolder) -> Unit = {},
        ) {
            viewModelScope.launch {
                when (val result = library.createFolder(name)) {
                    is FolderResult.Done -> {
                        track(GAAccountAnalytics.Event.LIBRARY_FOLDER_CREATE, "custom")
                        onCreated(result.folder)
                    }
                    else -> report(result)
                }
            }
        }

        fun rename(
            folderId: String,
            name: String,
        ) {
            viewModelScope.launch {
                val result = library.rename(folderId, name)
                if (result is FolderResult.Done) track(GAAccountAnalytics.Event.LIBRARY_FOLDER_RENAME, "custom")
                report(result)
            }
        }

        fun deleteFolder(folderId: String) {
            viewModelScope.launch {
                library.deleteFolder(folderId)
                track(GAAccountAnalytics.Event.LIBRARY_FOLDER_DELETE, "custom")
            }
        }

        /** 보관함 이벤트를 GA 로 보낸다. 폴더 이름은 보내지 않고 종류(좋아요/나중에/사용자)만 보낸다. */
        fun track(
            event: String,
            folderType: String,
        ) {
            gaLog.sendEvent(
                GAAccountAnalytics.Screen.LIBRARY_FOLDER,
                event,
                GAAccountAnalytics.Action.CLICK,
                mapOf(GAAccountAnalytics.Param.FOLDER_TYPE to folderType),
            )
        }

        fun folderType(folderId: String): String =
            when (folderId) {
                LIKES_FOLDER_ID -> "likes"
                WATCH_LATER_FOLDER_ID -> "watch_later"
                else -> "custom"
            }

        private suspend fun report(result: FolderResult) {
            when (result) {
                FolderResult.NameTaken -> _messages.emit(LibraryMessage.NAME_TAKEN)
                FolderResult.NameInvalid -> _messages.emit(LibraryMessage.NAME_INVALID)
                FolderResult.LimitReached -> _messages.emit(LibraryMessage.FOLDER_LIMIT)
                else -> Unit
            }
        }

        private companion object {
            const val MESSAGE_BUFFER = 4
        }
    }
