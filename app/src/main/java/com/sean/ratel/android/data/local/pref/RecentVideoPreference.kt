package com.sean.ratel.android.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.data.common.RemoteConfig.MAX_RECENTLY_SAVE_SIZE
import com.sean.ratel.android.data.dto.MainShortsModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Stack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentVideoPreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private val recentlyVideoKey = stringPreferencesKey("recent_video_list")
        val gson = Gson()

        private suspend fun setRecentVideo(videoList: List<MainShortsModel>) {
            val jsonString = gson.toJson(videoList)
            dataStore.edit { preferences ->
                preferences[recentlyVideoKey] = jsonString
            }
        }

        suspend fun removeRecentVideo() {
            dataStore.edit { it.remove(recentlyVideoKey) }
        }

        suspend fun getRecentVideo(): Stack<MainShortsModel> {
            val jsonString: String =
                dataStore.data
                    .map { preferences -> preferences[recentlyVideoKey] ?: "" }
                    .first()
            val type = object : TypeToken<Stack<MainShortsModel>>() {}.type
            val currentList: Stack<MainShortsModel> =
                if (jsonString.isNotEmpty()) {
                    gson.fromJson(jsonString, type)
                } else {
                    Stack<MainShortsModel>()
                }
            return currentList
        }

        suspend fun updateRecentVideo(recentVideo: MainShortsModel) {
            val currentList = getRecentVideo().toMutableList() // 복사본 생성

            if (currentList.isEmpty()) {
                setRecentVideo(listOf(recentVideo))
                return
            }

            if (currentList.size >= RemoteConfig.getRemoteConfigIntValue(MAX_RECENTLY_SAVE_SIZE)) {
                currentList.removeAt(currentList.size - 1) // 마지막 항목 제거
            }

            // 기존 리스트에서 동일한 videoId가 있는 항목 갱신
            val existingIndex =
                currentList.indexOfFirst {
                    it.shortsVideoModel?.videoId == recentVideo.shortsVideoModel?.videoId
                }

            if (existingIndex != -1) {
                // 기존 영상 업데이트 후 맨앞으로하며 기존 데이터 지움
                currentList.removeAt(existingIndex)
                currentList.add(0, recentVideo)
            } else {
                currentList.add(0, recentVideo) // 리스트 맨 앞에 추가
            }

            setRecentVideo(currentList) // 수정된 리스트 저장
        }
    }
