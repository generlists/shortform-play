package com.sean.ratel.android.data.local.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.domain.model.push.PushAppUpdateModel
import com.sean.ratel.android.data.domain.model.push.PushModel
import com.sean.ratel.android.data.domain.model.push.PushRecommendModel
import com.sean.ratel.android.data.domain.model.push.PushUploadModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "push-demo")

@Singleton
class PushPreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private val keyToken = stringPreferencesKey("token_save")
        private val appUpdatePush = booleanPreferencesKey("app_update_push")
        private val uploadPush = booleanPreferencesKey("upload_push")
        private val recommendPush = booleanPreferencesKey("recommend_push")
        private val permissionRequestedBefore =
            booleanPreferencesKey("notification_requested_before")
        private val permissionDeniedCount =
            intPreferencesKey("notification_denied_count")
        private val saveOpenSettingPush = booleanPreferencesKey("save_open_setting")
        private val pushNotification = stringPreferencesKey("push_notification_list")
        private val newPush = booleanPreferencesKey("new_setting")
        private val fromPermissionPage = booleanPreferencesKey("from_permission")

        val pushJson =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                classDiscriminator = "pushModelType"
            }

        suspend fun saveToken(token: String?) {
            if (token == null) {
                dataStore.edit { prefs ->
                    prefs.remove(keyToken)
                }
            } else {
                dataStore.edit { prefs ->
                    prefs[keyToken] = token
                }
            }
        }

        fun getSaveToken(): Flow<String?> =

            dataStore.data
                .map { prefs ->
                    prefs[keyToken]
                }

        suspend fun saveAppUpdatePush(update: Boolean) {
            dataStore.edit { prefs ->
                prefs[appUpdatePush] = update
            }
        }

        fun getAppUpdatePush(): Flow<Boolean> =

            dataStore.data
                .map { prefs ->
                    prefs[appUpdatePush] ?: true
                }

        suspend fun saveUploadPush(upload: Boolean) {
            dataStore.edit { prefs ->
                prefs[uploadPush] = upload
            }
        }

        fun getUploadPush(): Flow<Boolean> =

            dataStore.data
                .map { prefs ->
                    prefs[uploadPush] ?: true
                }

        suspend fun saveRecommendPush(upload: Boolean) {
            dataStore.edit { prefs ->
                prefs[recommendPush] = upload
            }
        }

        fun getRecommendPush(): Flow<Boolean> =

            dataStore.data
                .map { prefs ->
                    prefs[recommendPush] ?: true
                }

        fun getNewPush(): Flow<Boolean> =

            dataStore.data
                .map { prefs ->
                    prefs[newPush] ?: false
                }

        suspend fun saveNewPush(new: Boolean) {
            dataStore.edit { prefs ->
                prefs[newPush] = new
            }
        }

        suspend fun saveAllPush(all: Boolean) {
            saveAppUpdatePush(all)
            saveUploadPush(all)
            saveRecommendPush(all)
        }

        fun getAllPush(): Flow<Boolean> =
            combine(
                getUploadPush(),
                getAppUpdatePush(),
                getRecommendPush(),
            ) { upload, appUpdate, recommend ->
                upload || appUpdate || recommend
            }

        val notificationRequestedBefore: Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[permissionRequestedBefore] ?: false
            }

        val saveOpenSetting: Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[saveOpenSettingPush] ?: false
            }

        suspend fun saveOpenSetting(value: Boolean) {
            dataStore.edit { prefs ->
                prefs[saveOpenSettingPush] = value
            }
        }

        suspend fun setNotificationRequestedBefore(value: Boolean) {
            dataStore.edit { prefs ->
                prefs[permissionRequestedBefore] = value
            }
        }

        suspend fun incrementNotificationDeniedCount() {
            dataStore.edit { prefs ->
                val current = prefs[permissionDeniedCount] ?: 0
                prefs[permissionDeniedCount] = current + 1
                RLog.d("SSKKKKKK", "incrementNotificationDeniedCount")
            }
        }

        suspend fun resetNotificationDeniedCount() {
            dataStore.edit { prefs ->
                prefs[permissionDeniedCount] = 0
                RLog.d("SSKKKKKK", "resetNotificationDeniedCount")
            }
        }

        suspend fun clearNotificationPermissionPrefs() {
            dataStore.edit { prefs ->
                prefs.remove(permissionRequestedBefore)
                prefs.remove(permissionDeniedCount)
            }
        }

        private suspend fun setNewPush(pushList: List<PushModel>) {
            val jsonString = pushJson.encodeToString(pushList)
            dataStore.edit { preferences ->
                preferences[pushNotification] = jsonString
            }
        }

        fun getPermissionDeniedCount(): Flow<Int> =
            dataStore.data
                .map { prefs ->
                    prefs[permissionDeniedCount] ?: 0
                }

        suspend fun removeAllPush() {
            dataStore.edit { it.remove(pushNotification) }
        }

        fun getPushList(): Flow<List<PushModel>> =
            dataStore.data.map { preferences ->
                val jsonString = preferences[pushNotification] ?: ""
                if (jsonString.isNotEmpty()) {
                    pushJson.decodeFromString<List<PushModel>>(jsonString)
                } else {
                    emptyList()
                }
            }

        suspend fun deletePushItem(target: PushModel) {
            val currentList = getPushList().first()

            val updatedList =
                currentList.filterNot {
                    it.type == target.type &&
                        it.linkUrl == target.linkUrl &&
                        it.createAt == target.createAt
                }

            updatePush(updatedList)
        }

        suspend fun updateReadFlag(
            id: String,
            isRead: Boolean,
        ) {
            val currentList = getPushList().first()

            val updatedList =
                currentList.map { item ->
                    if (
                        item.id == id
                    ) {
                        when (item) {
                            is PushAppUpdateModel -> item.copy(isRead = isRead)
                            is PushUploadModel -> item.copy(isRead = isRead)
                            is PushRecommendModel -> item.copy(isRead = isRead)
                        }
                    } else {
                        item
                    }
                }
            updatePush(updatedList)
        }

        suspend fun updatePush(pushList: List<PushModel>) {
            setNewPush(pushList)
        }

        fun getFromPermissionPage(): Flow<Boolean> =

            dataStore.data
                .map { prefs ->
                    prefs[fromPermissionPage] ?: true
                }

        suspend fun setFromPermissionPage(upload: Boolean) {
            dataStore.edit { prefs ->
                prefs[fromPermissionPage] = upload
            }
        }
    }
