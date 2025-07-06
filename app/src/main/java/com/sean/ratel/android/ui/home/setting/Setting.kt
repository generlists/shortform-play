package com.sean.ratel.android.ui.home.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.Background
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.ComposeUtil.ViewBottomMargin
import com.sean.ratel.android.utils.PhoneUtil

@Suppress("ktlint:standard:function-naming")
@Composable
fun Setting(
    viewModel: SettingViewModel?,
    mainViewModel: MainViewModel?,
    adViewModel: AdViewModel,
) {
    SettingView(viewModel, mainViewModel, adViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingView(
    viewModel: SettingViewModel?,
    mainViewModel: MainViewModel?,
    adViewModel: AdViewModel?,
) {
    val context = LocalContext.current
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
    Scaffold(
        Modifier.padding(insetPaddingValue),
        topBar = {
            TopNavigationBar(
                titleResourceId = R.string.setting,
                historyBack = { mainViewModel?.runNavigationBack() },
                isShareButton = false,
                runSetting = { PhoneUtil.shareAppLinkButton(context) },
                filterButton = false,
                onFilterChange = {},
                items = listOf(),
            )
        },
        containerColor = Background,
    ) { innerPadding ->
        RLog.d("Setting", "$innerPadding")
        // LazyColumn
        val scrollState = rememberScrollState()
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Column(
            Modifier
                .fillMaxSize()
                .background(APP_BACKGROUND)
                .verticalScroll(scrollState),
        ) {
            SettingsService(viewModel = viewModel)
            SettingsVideo(viewModel = viewModel)
            SettingsApp(viewModel = viewModel)
            ViewBottomMargin(adViewModel)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SettingViewPreView() {
    RatelappTheme {
        SettingView(null, null, null)
    }
}
