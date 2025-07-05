package com.sean.ratel.android.ui.home.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.Background
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.ui.toolbox.PhoneAppList
import com.sean.ratel.android.utils.ComposeUtil.ViewBottomMargin
import com.sean.ratel.android.utils.PhoneUtil

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingsAppManager(
    viewModel: SettingViewModel?,
    mainViewModel: MainViewModel?,
    adViewModel: AdViewModel?,
) {
    SettingsAppManagerView(viewModel, mainViewModel, adViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingsAppManagerView(
    viewModel: SettingViewModel?,
    mainViewModel: MainViewModel?,
    adViewModel: AdViewModel?,
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopNavigationBar(
                titleResourceId = R.string.setting_app_manager,
                historyBack = { mainViewModel?.runNavigationBack() },
                isShareButton = true,
                runSetting = { PhoneUtil.shareAppLinkButton(context) },
                filterButton = false,
                onFilterChange = {},
                items = listOf(),
            )
        },
        containerColor = Background,
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            Modifier
                .fillMaxSize()
                .background(APP_BACKGROUND)
                .padding(innerPadding)
                .verticalScroll(scrollState),
        ) {
            SettingsAppDetail(viewModel = viewModel)
            PhoneAppList(viewModel)
            ViewBottomMargin(adViewModel)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SettingViewPreView() {
    RatelappTheme {
        SettingsAppManager(null, null, null)
    }
}
