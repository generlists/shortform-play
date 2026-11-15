package com.sean.ratel.android.data.library

import com.sean.ratel.android.data.dto.LibraryFolderDto
import com.sean.ratel.android.data.dto.LibraryItemDto
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.ShortsChannelModel
import com.sean.ratel.android.data.dto.ShortsVideoModel
import java.time.Instant
import java.time.OffsetDateTime

private val VIDEO_ID_RE = Regex("^[A-Za-z0-9_\\-]{5,32}$")
private val FOLDER_ID_RE = Regex("^[A-Za-z0-9_\\-]{1,36}$")

fun Long.toIso(): String = Instant.ofEpochMilli(this).toString()

/** ISO-8601(Z 또는 +00:00) → epoch ms. 읽을 수 없으면 0. */
fun String?.toEpochMs(): Long = runCatching { OffsetDateTime.parse(this.orEmpty()).toInstant().toEpochMilli() }.getOrDefault(0L)

/** 서버가 422 로 거절하지 않게 길이·제어 문자·주소 형식을 미리 정리한다. */
private fun String?.clean(max: Int): String = this.orEmpty().filter { !it.isISOControl() }.trim().replace(Regex("\\s+"), " ").take(max)

fun LibraryFolder.toDto(): LibraryFolderDto = LibraryFolderDto(id, name, kind.wire, position.coerceIn(0, 1000), deleted, updatedAt.toIso())

fun LibraryItem.toDto(): LibraryItemDto =
    LibraryItemDto(
        folderId = folderId,
        videoId = videoId,
        title = title.clean(300),
        thumbnail =
            thumbnail.trim().takeIf {
                it.length <= 500 &&
                    it.lowercase().let {
                            u ->
                        u.startsWith("https://") || u.startsWith("http://")
                    }
            }.orEmpty(),
        channelId = channelId.clean(64),
        channelTitle = channelTitle.clean(120),
        duration = duration.clean(16),
        savedAt = savedAt.toIso(),
        deleted = deleted,
        updatedAt = updatedAt.toIso(),
    )

/** 서버가 받아 주는 형식인지(올릴 수 있는 행인지). */
fun LibraryItem.isValidForServer(): Boolean = VIDEO_ID_RE.matches(videoId) && FOLDER_ID_RE.matches(folderId)

fun LibraryFolder.isValidForServer(): Boolean = FOLDER_ID_RE.matches(id) && LibraryLogic.normalizeName(name) != null

fun LibraryFolderDto.toFolder(): LibraryFolder =
    LibraryFolder(id, name, FolderKind.fromWire(kind), position, deleted, updatedAt.toEpochMs())

fun LibraryItemDto.toItem(): LibraryItem =
    LibraryItem(
        folderId = folderId,
        videoId = videoId,
        title = title,
        thumbnail = thumbnail,
        channelId = channelId,
        channelTitle = channelTitle,
        duration = duration,
        savedAt = savedAt.toEpochMs(),
        deleted = deleted,
        updatedAt = updatedAt.toEpochMs(),
    )

/** 재생 화면에서 쓰는 영상 모델 → 보관함에 담을 스냅샷. 영상 id 가 없으면 null. */
fun MainShortsModel.toSnapshot(): VideoSnapshot? {
    val video = shortsVideoModel ?: return null
    if (video.videoId.isBlank()) return null
    return VideoSnapshot(
        videoId = video.videoId,
        title = video.title,
        thumbnail = video.thumbNail,
        channelId = shortsChannelModel?.channelId.orEmpty(),
        channelTitle = shortsChannelModel?.channelTitle.orEmpty(),
        duration = video.duration,
    )
}

/** 보관함의 영상을 재생 화면에 넘길 모델로 만든다(스냅샷에 없는 값은 비어 있다). */
fun LibraryItem.toMainShortsModel(): MainShortsModel =
    MainShortsModel(
        shortsChannelModel =
            ShortsChannelModel(
                channelId = channelId,
                channelThumbNail = "",
                publishDate = "",
                channelTitle = channelTitle,
                channelDescription = "",
            ),
        shortsVideoModel =
            ShortsVideoModel(
                videoId = videoId,
                thumbNail = thumbnail,
                title = title,
                description = "",
                duration = duration,
                category = "",
                topicName = null,
                categoryName = "",
            ),
    )
