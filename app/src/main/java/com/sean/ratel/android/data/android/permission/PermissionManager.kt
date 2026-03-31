package com.sean.ratel.android.data.android.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/*
 isGranted       isRationale         result
*  ------------------------------------------------------------
*  true            false               granted
*  true            true                granted
*  false           false               before first permission deny or "never ask again checked"
*  false           true                after first deny
 카메라, 위치, 마이크는 좀 더 민감 3가지로 분류
*/

class PermissionManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : PermissionProvider {
        override fun requestPermissions(
            permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
            permissions: String,
        ) {
            permissionLauncher.launch(permissions)
        }

        override fun has(permission: String): Boolean =
            ContextCompat.checkSelfPermission(
                context,
                permission,
            ) == PackageManager.PERMISSION_GRANTED

        override fun shouldShowRationale(permission: String): Boolean {
            val activity = context as? Activity ?: return false
            return ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permission,
            )
        }

        override fun openAppSettings() {
            val intent =
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        override suspend fun consumeOpenedSettings(): Boolean = false

        override suspend fun markOpenedSettings() {}

        companion object {
            private const val TAG = "PermissionManager"

            private const val EXPLAINED = "EXPLAINED"
            private const val DENIED = "DENIED"
        }

        override fun requiredPermissions(): String =
//            if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.POST_NOTIFICATIONS
//            } else {
//                ""
//            }
    }
