package com.sean.ratel.android.data.repository

import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.local.pref.RecentVideoPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentVideoRepository
    @Inject
    constructor(
        private val recentVideoPreference: RecentVideoPreference,
    ) {
        suspend fun updateRecentVideo(mainShortsModel: MainShortsModel?) {
            mainShortsModel?.let {
                recentVideoPreference.updateRecentVideo(mainShortsModel)
            }
        }

        suspend fun removeRecentVideoe() {
            recentVideoPreference.removeRecentVideo()
        }

        suspend fun getRecentVideo(): List<MainShortsModel> = recentVideoPreference.getRecentVideo()
    }
