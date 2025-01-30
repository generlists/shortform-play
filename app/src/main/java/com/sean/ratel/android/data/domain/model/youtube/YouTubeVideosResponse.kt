package com.sean.ratel.android.data.domain.model.youtube

data class YouTubeVideoResponse(
    val kind: String,
    val etag: String,
    val items: List<VVideoItem>,
    val pageInfo: VPageInfo,
)

data class VVideoItem(
    val kind: String,
    val etag: String,
    val id: String,
    val snippet: VSnippet,
    val contentDetails: VContentDetails,
    val status: VStatus,
    val statistics: VStatistics,
)

data class VSnippet(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: VThumbnails,
    val channelTitle: String,
    val tags: List<String>?,
    val categoryId: String,
    val liveBroadcastContent: String,
    val defaultLanguage: String?,
    val localized: VLocalized,
    val defaultAudioLanguage: String?,
)

data class VThumbnails(
    val default: VThumbnail,
    val medium: VThumbnail,
    val high: VThumbnail,
    val standard: VThumbnail?,
    val maxres: VThumbnail?,
)

data class VThumbnail(
    val url: String,
    val width: Int,
    val height: Int,
)

data class VLocalized(
    val title: String,
    val description: String,
)

data class VContentDetails(
    val duration: String,
    val dimension: String,
    val definition: String,
    val caption: String,
    val licensedContent: Boolean,
    // val contentRating: ContentRating,
    val projection: String,
    val regionRestriction: VRegionRestriction?,
)

data class VRegionRestriction(
    val allowed: List<String>?,
    val blocked: List<String>?,
)

// data class ContentRating(
//    // Additional fields for content rating can be added if needed
// )

data class VStatus(
    val uploadStatus: String,
    val privacyStatus: String,
    val license: String,
    val embeddable: Boolean,
    val publicStatsViewable: Boolean,
    val madeForKids: Boolean,
)

data class VStatistics(
    val viewCount: String,
    val likeCount: String,
    val favoriteCount: String,
    val commentCount: String,
)

data class VPageInfo(
    val totalResults: Int,
    val resultsPerPage: Int,
)
