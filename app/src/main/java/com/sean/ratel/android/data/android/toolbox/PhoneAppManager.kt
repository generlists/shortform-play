package com.sean.ratel.android.data.android.toolbox

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.GET_META_DATA
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.data.domain.model.toolbox.AppManagerInfo
import com.sean.ratel.android.utils.PhoneUtil
import com.sean.ratel.android.utils.TimeUtil.formatMillisToDate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class PhoneAppManager
    @Inject
    constructor() : PhoneAppProvider {
        override suspend fun fetchInstalledApps(
            @ApplicationContext context: Context,
        ): Flow<List<AppManagerInfo>> =
            flow {
                emit(getInstalledApps(context))
            }

        @SuppressLint("QueryPermissionsNeeded")
        private suspend fun getInstalledApps(context: Context): List<AppManagerInfo> =
            withContext(Dispatchers.IO) {
                context.packageManager
                    .getInstalledPackages(GET_META_DATA)
                    .filterNot { it.isSystemApp() || isMyApp(it.packageName) }
                    .map { packageInfo ->
                        AppManagerInfo(
                            packageInfo.applicationInfo.loadLabel(context.packageManager).toString(),
                            packageInfo.applicationInfo.loadIcon(context.packageManager),
                            packageInfo.packageName,
                            PhoneUtil
                                .bytesToMegabytes(File(packageInfo.applicationInfo.sourceDir).length())
                                .toString(),
                            formatMillisToDate(packageInfo.firstInstallTime),
                            formatMillisToDate(packageInfo.lastUpdateTime),
                        )
                    }
            }

        override suspend fun removeInstalledApp(
            deleteAppLauncher: ActivityResultLauncher<Intent>,
            packageName: String,
        ) {
            val intent =
                Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:$packageName")
                }
            deleteAppLauncher.launch(intent)
        }

        override fun goStore(
            context: Context,
            updateUrl: String,
            appName: String,
        ) {
            try {
                val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(updateUrl)
                        setPackage("com.android.vending")
                    }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                // Google Play 앱이 없거나 오류가 발생할 경우, 웹 브라우저로 이동하도록 설정
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(STRINGS.URLUPDATE_GOOGLE_PLAY_WEB(appName)))
                context.startActivity(fallbackIntent)
            }
        }

        override fun goDetailAppInfo(
            context: Context,
            packageName: String,
        ) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }

        private fun PackageInfo.isSystemApp(): Boolean = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        private fun isMyApp(packageName: String): Boolean = packageName == STRINGS.URL_MY_PACKAGE_NAME
    }
