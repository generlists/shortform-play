package com.sean.ratel.android.data.domain.model.youtube

// YouTube API 응답 데이터 모델
// 유튜브 API 응답 모델
data class YouTubePopularResponse(
    val kind: String,
    val etag: String,
    val items: List<VideoItem>,
    val nextPageToken: String?,
    val pageInfo: PageInfo,
)

// 각 동영상 항목
data class VideoItem(
    val kind: String,
    val etag: String,
    val id: String,
    val snippet: Snippet,
    val contentDetails: ContentDetails,
)

// 동영상 정보 (Snippet)
data class Snippet(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: Thumbnails,
    val channelTitle: String,
    val tags: List<String>?,
    val categoryId: String,
    val liveBroadcastContent: String,
    val localized: Localized,
    val defaultAudioLanguage: String?,
)

// 썸네일 정보
data class Thumbnails(
    val default: Thumbnail,
    val medium: Thumbnail,
    val high: Thumbnail,
    val standard: Thumbnail?,
    val maxres: Thumbnail?,
)

// 각 썸네일 사이즈 정보
data class Thumbnail(
    val url: String,
    val width: Int,
    val height: Int,
)

// 로컬라이즈된 제목 및 설명
data class Localized(
    val title: String,
    val description: String,
)

data class ContentDetails(
    val duration: String,
    val dimension: String,
    val definition: String,
    val caption: String,
    val licensedContent: Boolean,
)

// 페이지 정보
data class PageInfo(
    val totalResults: Int,
    val resultsPerPage: Int,
)
