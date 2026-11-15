package com.sean.ratel.android.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeEndPreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        suspend fun setLikeDisLike(
            key: String,
            likeVideo: String,
        ) {
            dataStore.edit { it[stringPreferencesKey(key)] = likeVideo }
        }

        suspend fun setCancelLikeDisLike(key: String) {
            dataStore.edit { it.remove(stringPreferencesKey(key)) }
        }

        suspend fun getLikeDisLikeVideo(key: String): String? = dataStore.data.map { it[stringPreferencesKey(key)] }.first()

        /** 좋아요를 누른 영상 (영상 id, 저장된 JSON). 키는 `like{videoId}`(YouTubeEndCompose). 보관함의 "좋아요" 폴더로 옮길 때 한 번 읽는다. */
        suspend fun likedEntries(): List<Pair<String, String>> =
            dataStore.data
                .first()
                .asMap()
                .mapNotNull { (key, value) ->
                    if (key.name.startsWith(LIKE_KEY_PREFIX) && value is String) key.name.removePrefix(LIKE_KEY_PREFIX) to value else null
                }

        /** 싫어요를 누른 영상 id 모음. 키는 `disLike{videoId}`(YouTubeEndCompose)다. 바뀔 때마다 새로 내보낸다. */
        val dislikedVideoIds: Flow<Set<String>> =
            dataStore.data
                .map { prefs ->
                    prefs
                        .asMap()
                        .keys
                        .map { it.name }
                        .filter { it.startsWith(DISLIKE_KEY_PREFIX) }
                        .map { it.removePrefix(DISLIKE_KEY_PREFIX) }
                        .toSet()
                }.distinctUntilChanged()

        private companion object {
            const val LIKE_KEY_PREFIX = "like"
            const val DISLIKE_KEY_PREFIX = "disLike"
        }
    }
