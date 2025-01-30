package com.sean.ratel.android.data.domain.model.youtube

data class YouTubeChannelResponse(
    val kind: String,
    val etag: String,
    val pageInfo: CPageInfo,
    val items: List<CChannelItem>,
)

data class CPageInfo(
    val totalResults: Int,
    val resultsPerPage: Int,
)

data class CChannelItem(
    val kind: String,
    val etag: String,
    val id: String,
    val snippet: CSnippet,
    val contentDetails: CContentDetails,
)

data class CSnippet(
    val title: String,
    val description: String,
    val customUrl: String?,
    val publishedAt: String,
    val thumbnails: CThumbnails,
    val localized: CLocalized,
    val country: String?,
)

data class CThumbnails(
    val default: CThumbnailInfo,
    val medium: CThumbnailInfo,
    val high: CThumbnailInfo,
)

data class CThumbnailInfo(
    val url: String,
    val width: Int,
    val height: Int,
)

data class CLocalized(
    val title: String,
    val description: String,
)

data class CContentDetails(
    val relatedPlaylists: CRelatedPlaylists,
)

data class CRelatedPlaylists(
    val likes: String?,
    val uploads: String,
)
