package com.sean.ratel.android.data.library

import androidx.annotation.Keep

/** 폴더 종류. [LIKES]·[WATCH_LATER] 는 모든 기기에 있는 고정 폴더(이름·삭제 불가)이고 서버에는 사용자 폴더([CUSTOM])만 올린다. */
@Keep
enum class FolderKind(
    val wire: String,
) {
    LIKES("likes"),
    WATCH_LATER("watch_later"),
    CUSTOM("custom"),
    ;

    companion object {
        fun fromWire(value: String?): FolderKind = entries.firstOrNull { it.wire == value } ?: CUSTOM
    }
}

const val LIKES_FOLDER_ID = "likes"
const val WATCH_LATER_FOLDER_ID = "watch_later"

object LibraryLimits {
    const val FOLDERS_MAX = 50 // 사용자 폴더 (서버 한도와 같게)
    const val ITEMS_MAX = 2000 // 폴더에 담은 영상 전체
    const val NAME_MAX = 30
    const val BATCH_MAX = 500 // 한 번의 sync 에 실을 수 있는 행
}

/** [deleted] 는 지운 표시(tombstone) — 다른 기기가 지웠다는 걸 알 수 있게 남긴다. 시각은 epoch ms. */
@Keep
data class LibraryFolder(
    val id: String,
    val name: String,
    val kind: FolderKind,
    val position: Int = 0,
    val deleted: Boolean = false,
    val updatedAt: Long = 0L,
)

@Keep
data class LibraryItem(
    val folderId: String,
    val videoId: String,
    val title: String = "",
    val thumbnail: String = "",
    val channelId: String = "",
    val channelTitle: String = "",
    val duration: String = "",
    val savedAt: Long = 0L,
    val deleted: Boolean = false,
    val updatedAt: Long = 0L,
) {
    val key: String get() = keyOf(folderId, videoId)

    companion object {
        fun keyOf(
            folderId: String,
            videoId: String,
        ) = "$folderId/$videoId"
    }
}

/** 기기에 저장되는 보관함 전체. [dirtyFolders]·[dirtyItems]: 아직 서버에 올리지 못한 변경(항목 key). */
@Keep
data class LibraryData(
    val folders: List<LibraryFolder> = emptyList(),
    val items: List<LibraryItem> = emptyList(),
    val rev: Long = 0L,
    val dirtyFolders: Set<String> = emptySet(),
    val dirtyItems: Set<String> = emptySet(),
    val likesSeeded: Boolean = false,
)

/** 폴더에 담을 영상의 스냅샷(다른 기기에서 YouTube 를 다시 부르지 않고 보여주려고 같이 저장한다). */
data class VideoSnapshot(
    val videoId: String,
    val title: String = "",
    val thumbnail: String = "",
    val channelId: String = "",
    val channelTitle: String = "",
    val duration: String = "",
)

/** 폴더 카드(마이 탭)에 보여 줄 요약: 담은 영상 수와 앞쪽 썸네일 몇 개. */
data class FolderSummary(
    val folder: LibraryFolder,
    val count: Int,
    val covers: List<String>,
)

sealed interface AddResult {
    data object Added : AddResult

    data object AlreadySaved : AddResult

    data object LimitReached : AddResult

    data object NoFolder : AddResult
}

sealed interface FolderResult {
    data class Done(
        val folder: LibraryFolder,
    ) : FolderResult

    data object NameInvalid : FolderResult

    data object NameTaken : FolderResult

    data object LimitReached : FolderResult

    data object NoFolder : FolderResult
}
