package com.sean.ratel.android.ui.toolbox

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.sean.ratel.android.R

@Keep
enum class ToolBox(
    @DrawableRes val icon: Int,
    val mainTitle: Int,
    val description: Int,
) {
    APP_MANAGER(
        R.drawable.ic_app_list,
        R.string.app_manager,
        R.string.app_manager_detail,
    ),
    NETWORK_MANAGER(
        R.drawable.ic_network_manager,
        R.string.network_manager,
        R.string.network_manager_detail,
    ),
    PHONE_CARE(
        R.drawable.ic_phone_care,
        R.string.device_care,
        R.string.device_care_detail,
    ),
}
