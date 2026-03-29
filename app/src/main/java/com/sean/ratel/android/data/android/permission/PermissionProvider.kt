package com.sean.ratel.android.data.android.permission

import androidx.activity.compose.ManagedActivityResultLauncher

interface PermissionProvider {
    fun requestPermissions(
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
        permissions: String,
    )

    fun has(permission: String): Boolean

    fun shouldShowRationale(permission: String): Boolean

    fun requiredPermissions(): String

    fun openAppSettings()

    suspend fun consumeOpenedSettings(): Boolean

    suspend fun markOpenedSettings()
}
