package com.sean.ratel.android.data.common

import android.content.Context
import android.content.Intent
import android.net.Uri

object YouTubeUtils {
    /**
     * 검색 결과의
     * video id 를 가지고  videos.list API의 categoryId 필드를 사용하여 동영상이 속한 카테고리를 식별
     */
    fun getCategoryName(categoryId: String): String =
        when (categoryId) {
            "1" -> "영화 및 애니메이션"
            "2" -> "자동차 및 차량"
            "10" -> "음악"
            "15" -> "애완동물 및 동물"
            "17" -> "스포츠"
            "20" -> "게임"
            "22" -> "뉴스 및 정치"
            "23" -> "Vlog"
            "24" -> "엔터테인먼트"
            "25" -> "노하우 및 스타일"
            "26" -> "교육"
            "27" -> "과학 및 기술"
            "30" -> "영화"
            "31" -> "애니메이션"
            "43" -> "트레일러"
            else -> "알 수 없음"
        }

    fun goYouTubeAppByVideoId(
        context: Context,
        videoId: String,
    ) {
        try {
            val fallbackIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(STRINGS.YOUTUBE_APP_BY_VIDEO_ID(videoId)),
                )
            context.startActivity(fallbackIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareVideo(
        context: Context,
        videoId: String,
    ) {
        val videoUrl = STRINGS.YOUTUBE_APP_BY_VIDEO_ID(videoId)
        val sendIntent =
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, videoUrl)
                type = "text/plain"
            }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }
}
