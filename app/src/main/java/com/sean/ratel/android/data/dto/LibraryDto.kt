package com.sean.ratel.android.data.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** 서버 /shortformplay/v2/me/library 의 요청·응답. 요청은 모르는 필드가 있으면 서버가 422 로 거절하므로 키 이름이 정확해야 한다. */
@Keep
data class LibraryFolderDto(
    val id: String,
    val name: String,
    val kind: String,
    val position: Int = 0,
    val deleted: Boolean = false,
    @SerializedName("updated_at") val updatedAt: String,
)

@Keep
data class LibraryItemDto(
    @SerializedName("folder_id") val folderId: String,
    @SerializedName("video_id") val videoId: String,
    val title: String = "",
    val thumbnail: String = "",
    @SerializedName("channel_id") val channelId: String = "",
    @SerializedName("channel_title") val channelTitle: String = "",
    val duration: String = "",
    @SerializedName("saved_at") val savedAt: String,
    val deleted: Boolean = false,
    @SerializedName("updated_at") val updatedAt: String,
)

/** [since]: 앱이 마지막으로 받은 서버 변경 번호(처음이면 0). 올릴 것이 없으면 두 목록을 비워 받기만 한다. */
@Keep
data class LibrarySyncRequest(
    val since: Long,
    val folders: List<LibraryFolderDto>,
    val items: List<LibraryItemDto>,
)

/** 응답: 새 변경 번호 [rev] 와 since 이후 바뀐 폴더·영상(삭제 표시 포함). 값이 빠져 올 수 있어 nullable 로 받는다. */
@Keep
data class LibrarySyncDto(
    val rev: Long = 0L,
    val folders: List<LibraryFolderDto>? = null,
    val items: List<LibraryItemDto>? = null,
)
