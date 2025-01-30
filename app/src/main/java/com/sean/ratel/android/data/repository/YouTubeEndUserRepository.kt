package com.sean.ratel.android.data.repository

import com.sean.ratel.android.data.dto.MainShortsModel
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
    ) {
        suspend fun likeDisLike(
            key: String,
            likeShortsModel: MainShortsModel?,
        ) {
            likeShortsModel?.let {
                youTubeEndPreference.setLikeDisLike(key, serializeToJson(likeShortsModel))
            }
        }

        suspend fun cancelLikeDisLike(key: String) {
            youTubeEndPreference.setCancelLikeDisLike(key)
        }

        suspend fun getLikeDisLikeVideo(key: String): MainShortsModel? {
            val data = youTubeEndPreference.getLikeDisLikeVideo(key)
            data?.let {
                return deserializeFromJson(it)
            }

            return null
        }
    }
