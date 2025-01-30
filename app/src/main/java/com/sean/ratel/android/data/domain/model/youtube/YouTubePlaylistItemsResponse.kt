package com.sean.ratel.android.data.domain.model.youtube

data class PlaylistItemsResponse(
    val kind: String,
    val etag: String,
    val items: List<PPlaylistItem>,
    val pageInfo: PPageInfo,
    // 다음 페이지 토큰이 있을 수도 있습니다.
    val nextPageToken: String? = null,
)

data class PPlaylistItem(
    val kind: String,
    val etag: String,
    val id: String,
    val snippet: PSnippet,
    val contentDetails: PContentDetails,
)

data class PSnippet(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: PThumbnails,
    val channelTitle: String,
    val playlistId: String,
    val position: Int,
    val resourceId: PResourceId,
)

data class PResourceId(
    val kind: String,
    val videoId: String,
)

data class PContentDetails(
    val videoId: String,
    val videoPublishedAt: String,
    val duration: String,
)

data class PThumbnails(
    val default: PThumbnail,
    val medium: PThumbnail,
    val high: PThumbnail,
)

data class PThumbnail(
    val url: String,
    val width: Int,
    val height: Int,
)

data class PPageInfo(
    val totalResults: Int,
    val resultsPerPage: Int,
)
