package com.sean.ratel.android.data.android.permission

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import dagger.hilt.android.qualifiers.ActivityContext

interface PermissionProvider {
    fun requestPermissions(
        permissionLauncher: ActivityResultLauncher<Array<String>>,
        permissions: Array<String>,
    )

    fun handlePermissionResult(
        @ActivityContext activity: Context,
        permissions: Map<String, Boolean>,
    ): PermissionManager.PermissionState
}
