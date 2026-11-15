package com.sean.ratel.android.data.repository

import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.library.LIKES_FOLDER_ID
import com.sean.ratel.android.data.library.LibraryRepository
import com.sean.ratel.android.data.library.toSnapshot
import com.sean.ratel.android.data.local.pref.YouTubeEndPreference
import com.sean.ratel.android.utils.UIUtil.deserializeFromJson
import com.sean.ratel.android.utils.UIUtil.serializeToJson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeEndUserRepository
    @Inject
    constructor(
        private val youTubeEndPreference: YouTubeEndPreference,
        private val library: LibraryRepository,
    ) {
        suspend fun likeDisLike(
            key: String,
            likeShortsModel: MainShortsModel?,
        ) {
            likeShortsModel?.let {
                youTubeEndPreference.setLikeDisLike(key, serializeToJson(likeShortsModel))
                // 좋아요는 보관함의 "좋아요" 폴더에도 담는다(싫어요 키는 "disLike…" 라서 해당 없음)
                if (key.startsWith(LIKE_PREFIX)) likeShortsModel.toSnapshot()?.let { library.add(LIKES_FOLDER_ID, it) }
            }
        }

        suspend fun cancelLikeDisLike(key: String) {
            youTubeEndPreference.setCancelLikeDisLike(key)
            if (key.startsWith(LIKE_PREFIX)) library.remove(LIKES_FOLDER_ID, key.removePrefix(LIKE_PREFIX))
        }

        /** 이 기기에서 지금까지 누른 좋아요(저장된 영상 정보). 읽을 수 없는 항목은 건너뛴다. */
        suspend fun likedVideos(): List<MainShortsModel> =
            youTubeEndPreference.likedEntries().mapNotNull { (videoId, json) ->
                runCatching { deserializeFromJson(json) }.getOrNull()?.takeIf { it.shortsVideoModel?.videoId == videoId }
            }

        suspend fun getLikeDisLikeVideo(key: String): MainShortsModel? {
            val data = youTubeEndPreference.getLikeDisLikeVideo(key)
            data?.let {
                return deserializeFromJson(it)
            }

            return null
        }

        private companion object {
            const val LIKE_PREFIX = "like"
        }
    }
