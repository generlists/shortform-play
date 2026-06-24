package com.sean.ratel.android.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import so.smartlab.common.utils.log.RLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromotionPreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private val keyShowDoNotAgain = booleanPreferencesKey(PROMOTION_IS_SHWO_DO_NOT_AGAIN_CHECK)

        fun isShowDoNotShowAgain(): Flow<Boolean> =
            dataStore.data
                .map { prefs ->
                    prefs[keyShowDoNotAgain] ?: false
                }

        suspend fun markDoNotShowAgain(isHide: Boolean) {
            dataStore.edit { prefs ->
                RLog.d("In App Purchase", "[APP] markDoNotShowAgain check :  $isHide")
                prefs[keyShowDoNotAgain] = isHide
            }
        }

        companion object {
            private const val PROMOTION_IS_SHWO_DO_NOT_AGAIN_CHECK = "promotion_do_not_again_preference"
        }
    }
