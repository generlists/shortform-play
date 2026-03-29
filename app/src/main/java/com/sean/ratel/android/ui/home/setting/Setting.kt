package com.sean.ratel.android.ui.home.setting

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.push.PushViewModel
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.Background
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.PhoneUtil
import so.smartlab.common.ad.admob.data.model.AdMobBannerState

@Suppress("ktlint:standard:function-naming")
@Composable
fun Setting(
    viewModel: SettingViewModel?,
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    pushViewModel: PushViewModel,
) {
    SettingView(viewModel, mainViewModel, adViewModel, pushViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingView(
    viewModel: SettingViewModel?,
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    pushViewModel: PushViewModel,
) {
    val context = LocalContext.current
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
    val adFixedBannerState by mainViewModel.fixedBannerState.collectAsState()
    var adSize by remember { mutableStateOf(64) }
    val bottomBarHeight = adViewModel.bottomBarHeight.value
    when {
        adFixedBannerState is AdMobBannerState.AdLoadComplete -> {
            adSize = (adFixedBannerState as AdMobBannerState.AdLoadComplete).adSize.height
        }

        else -> {
            adSize = 0
        }
    }

    BackHandler(enabled = true) {
        mainViewModel.runNavigationBack()
    }
    Scaffold(
        Modifier.padding(insetPaddingValue),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopNavigationBar(
                titleResourceId = R.string.setting,
                historyBack = { mainViewModel.runNavigationBack() },
                isShareButton = false,
                runSetting = { PhoneUtil.shareAppLinkButton(context) },
                filterButton = false,
                onFilterChange = {},
                items = listOf(),
            )
        },
        containerColor = Background,
    ) { innerPadding ->
        RLog.d("Setting", "$innerPadding $adSize")
        // LazyColumn

        Column(
            Modifier
                .fillMaxSize()
                .wrapContentHeight()
                .background(APP_BACKGROUND)
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = adSize.dp + bottomBarHeight.dp),
            ) {
                item { SettingsService(viewModel, pushViewModel) }
                item { SettingsCountry(viewModel) }
                item { SettingsVideo(viewModel) }
                item { SettingsApp(viewModel) }
                item { SettingsDevOtherApp(viewModel) }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SettingViewPreView() {
    RatelappTheme {
        // SettingView(null, hiltViewModel())
    }
}
