package com.sean.ratel.android.data.domain.model.youtube

/**
 * {
 *    "items":[
 *       {
 *          "id":"VIDEO_ID_1",
 *          "status":{
 *             "embeddable":true,
 *             "privacyStatus":"public"
 *          },
 *          "contentDetails":{
 *             "regionRestriction":{
 *                "allowed":[
 *                   "US",
 *                   "CA"
 *                ],
 *                "blocked":[
 *                   "KR",
 *                   "CN"
 *                ]
 *             }
 *          }
 *       },
 *       {
 *          "id":"VIDEO_ID_2",
 *          "status":{
 *             "embeddable":false,
 *             "privacyStatus":"private"
 *          },
 *          "contentDetails":{
 *             "regionRestriction":{
 *                "blocked":[
 *                   "US",
 *                   "CA"
 *                ]
 *             }
 *          }
 *       }
 *    ]
 * }
 *
 */

data class YouTubeVideoRestrictResponse(
    val items: List<RVideoItem>,
)

data class RVideoItem(
    val id: String,
    val status: VideoStatus,
    val contentDetails: VideoContentDetails,
)

data class VideoStatus(
    val embeddable: Boolean,
    val privacyStatus: String,
)

data class VideoContentDetails(
    val regionRestriction: RegionRestriction?,
)

data class RegionRestriction(
    val allowed: List<String>?,
    val blocked: List<String>?,
)
