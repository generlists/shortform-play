package com.sean.ratel.android.data.android.toolbox

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.sean.ratel.android.data.domain.model.toolbox.AppManagerInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow

interface PhoneAppProvider {
    suspend fun fetchInstalledApps(
        @ApplicationContext context: Context,
    ): Flow<List<AppManagerInfo>>

    suspend fun removeInstalledApp(
        deleteAppLauncher: ActivityResultLauncher<Intent>,
        packageName: String,
    )

    fun goStore(
        context: Context,
        updateUrl: String,
        appName: String,
    )

    fun goDetailAppInfo(
        context: Context,
        packageName: String,
    )
}
