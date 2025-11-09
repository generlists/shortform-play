package com.sean.ratel.android.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sean.player.utils.log.RLog
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenPreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private val accessToken = stringPreferencesKey("access_token")
        private val expiresAt = longPreferencesKey("expires_at")
        private var cachedToken: String? = null

        suspend fun updateTokenCache() {
            val prefs = dataStore.data.first()
            val exp = prefs[expiresAt] ?: 0L

            if (exp > 0) {
                RLog.d(TAG, "exp : $exp")
                val remainingMs = exp - System.currentTimeMillis()
                val remainingHours = remainingMs / 1000 / 3600
                RLog.d(TAG, "남은 시간(시간 단위): $remainingHours")
                RLog.d(TAG, "currentTime : ${System.currentTimeMillis()}")
                cachedToken = if (System.currentTimeMillis() < exp) prefs [accessToken] else null
            }
        }

        suspend fun currentToken(): String? = dataStore.data.first() [accessToken]

        suspend fun saveAccessToken(
            token: String,
            expiresIn: Long,
        ) {
            dataStore.edit { prefs ->
                prefs[accessToken] = token
                prefs[expiresAt] = System.currentTimeMillis() + expiresIn * 1000
                RLog.d(TAG, "save : ${prefs[expiresAt]}")
            }
        }

        fun getAccessToken(): String? = cachedToken

        companion object {
            private const val TAG = "AuthTokenPreference"
        }
    }
