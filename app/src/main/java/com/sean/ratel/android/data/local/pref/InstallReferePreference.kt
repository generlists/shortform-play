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
class InstallReferePreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private val installRefererKey = stringPreferencesKey("install_referer")

        suspend fun setInstallReferer(value: String) {
            dataStore.edit {
                it[installRefererKey] = value
            }
        }

        suspend fun getInstallReferer(): String? = (dataStore.data.map { it[installRefererKey] }.first())
    }
