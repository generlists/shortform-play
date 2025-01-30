package com.sean.ratel.android.data.domain.model.youtube

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Main response class
@Parcelize
data class SearchListResponse(
    val kind: String,
    val etag: String,
    val nextPageToken: String?,
    val regionCode: String,
    val pageInfo: SPageInfo,
    val items: List<SSearchResult>,
) : Parcelable

// PageInfo class for pagination
@Parcelize
data class SPageInfo(
    val totalResults: Int,
    val resultsPerPage: Int,
) : Parcelable

// SearchResult class representing each item in the items array
@Parcelize
data class SSearchResult(
    val kind: String,
    val etag: String,
    val id: SVideoId,
    val snippet: SSnippet,
) : Parcelable

// VideoId class for the video information
@Parcelize
data class SVideoId(
    val kind: String,
    val videoId: String,
) : Parcelable

// Snippet class containing video details
@Parcelize
data class SSnippet(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: SThumbnails,
    val channelTitle: String,
    val liveBroadcastContent: String,
    val publishTime: String,
) : Parcelable

// Thumbnails class representing thumbnail image URLs
@Parcelize
data class SThumbnails(
    val default: SThumbnail,
    val medium: SThumbnail,
    val high: SThumbnail,
) : Parcelable

// Thumbnail class for different resolution thumbnail images
@Parcelize
data class SThumbnail(
    val url: String,
    val width: Int,
    val height: Int,
) : Parcelable
