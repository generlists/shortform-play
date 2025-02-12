package com.sean.ratel.android.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sean.ratel.android.data.dto.MainShortsResponse
import com.sean.ratel.android.utils.UIUtil.jsonToObject
import com.sean.ratel.android.utils.UIUtil.mainShortsListToJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortsJsonPreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        suspend fun setShortsList(
            key: String,
            mainShortsListResponse: MainShortsResponse,
        ) {
            dataStore.edit { it[stringPreferencesKey(key)] = mainShortsListToJson(mainShortsListResponse) }
        }

        // 현재 들어온 json 이외 같은 패턴은 다 지움
        suspend fun clearDataByKeyPattern(keyPattern: String) {
            dataStore.edit { preferences ->
                // 패턴에 맞는 키를 찾아서 삭제
                preferences.asMap().forEach { (key, _) ->
                    if (key.name.contains(keyPattern)) {
                        preferences.remove(key)
                    }
                }
            }
        }

        suspend fun getShortsList(key: String): MainShortsResponse? =
            jsonToObject(dataStore.data.map { it[stringPreferencesKey(key)] }.first())
    }
