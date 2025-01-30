package com.sean.ratel.android.data.domain.model.toolbox

import android.graphics.drawable.Drawable

data class AppManagerInfo(
    val appTitle: String,
    val icon: Drawable?,
    val packageName: String,
    val apkSize: String,
    val firstInstallTime: String,
    val lastUpdateTime: String,
)
