package com.sean.ratel.android.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
    }
