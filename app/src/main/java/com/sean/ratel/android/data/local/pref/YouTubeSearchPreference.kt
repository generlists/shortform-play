package com.sean.ratel.android.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sean.ratel.android.data.dto.SearchResultModel
import com.sean.ratel.android.data.dto.YouTubeCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeSearchPreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val keySearchResultList = stringPreferencesKey("search_keyword_list")
        val keyCategoryList = stringPreferencesKey("category_list")

        suspend fun saveSearchResultModel(newItem: SearchResultModel) {
            val gson = Gson()
            val key = keySearchResultList

            val currentList: List<SearchResultModel> =
                dataStore.data
                    .first()[key]
                    ?.let { json ->
                        gson.fromJson(json, object : TypeToken<List<SearchResultModel>>() {}.type)
                    } ?: emptyList()

            val updatedList =
                currentList
                    .filterNot {
                        it.searchKeyword.equals(
                            newItem.searchKeyword,
                            ignoreCase = true,
                        )
                    } // keyword(title) 기준 중복 제거
                    .toMutableList()
                    .apply {
                        add(0, newItem) // 최신 항목을 맨 위로
                    }.take(20) // 최대 20개 유지

            val updatedJson = gson.toJson(updatedList)

            dataStore.edit { prefs ->
                prefs[key] = updatedJson
            }
        }

        fun getSaveSuggestResultList(): Flow<List<SearchResultModel>> =

            dataStore.data.map { prefs ->
                val json = prefs[keySearchResultList]
                if (json.isNullOrEmpty()) {
                    emptyList()
                } else {
                    Gson().fromJson(json, object : TypeToken<List<SearchResultModel>>() {}.type)
                }
            }

        suspend fun removeSuggestKeyWord(searchKeyWordModel: SearchResultModel) {
            val gson = Gson()
            val key = keySearchResultList

            val currentList: List<SearchResultModel> =
                dataStore.data
                    .first()[key]
                    ?.let { json ->
                        gson.fromJson(json, object : TypeToken<List<SearchResultModel>>() {}.type)
                    } ?: emptyList()

            val updatedList =
                currentList.toMutableList().apply {
                    removeIf { it.searchKeyword == searchKeyWordModel.searchKeyword }
                }

            val updatedJson = gson.toJson(updatedList)

            dataStore.edit { prefs ->
                prefs[key] = updatedJson
            }
        }

        suspend fun getYouTubeCategoryList(): List<YouTubeCategory> =
            dataStore.data
                .map { prefs ->
                    if (prefs[keyCategoryList].isNullOrEmpty()) {
                        emptyList<YouTubeCategory>()
                    } else {
                        Gson().fromJson(prefs[keyCategoryList], object : TypeToken<List<YouTubeCategory>>() {}.type)
                    }
                }.first()

        suspend fun removeCategory() {
            dataStore.edit { preferences ->
                preferences.remove(keyCategoryList)
            }
        }

        suspend fun saveCategoryList(categoryList: List<YouTubeCategory>) {
            val gson = Gson()
            val key = keyCategoryList
            val updatedJson = gson.toJson(categoryList)
            dataStore.edit { prefs ->
                prefs[key] = updatedJson
            }
        }
    }
