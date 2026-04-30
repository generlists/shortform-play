package com.sean.ratel.android.ui.home.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.ad.AdTarget
import com.sean.ratel.android.ui.ad.InterstitialAdPage
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.Background
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.ui.toolbox.PhoneAppList
import com.sean.ratel.android.utils.PhoneUtil

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingsAppManager(
    viewModel: SettingViewModel?,
    mainViewModel: MainViewModel,
) {
    SettingsAppManagerView(viewModel, mainViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingsAppManagerView(
    viewModel: SettingViewModel?,
    mainViewModel: MainViewModel,
) {
    val context = LocalContext.current
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
    var loading by remember { mutableStateOf(true) }
    val adLoading by mainViewModel.interstitialAdStart.collectAsState(initial = null)

    Scaffold(
        Modifier.padding(insetPaddingValue),
        topBar = {
            TopNavigationBar(
                titleResourceId = R.string.setting_app_manager,
                historyBack = { mainViewModel.runNavigationBack() },
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
            PhoneAppList(mainViewModel, viewModel)
        }
        if (adLoading?.route == Destination.SettingAppManagerDetail.route) {
            InterstitialAdPage(
                adTarget =
                    AdTarget(
                        Destination.Home.Main.TopicListDetail.route,
                        adLoading?.adStart ?: true,
                    ),
                interstitialAdManager = mainViewModel.interstitialAdManager,
                setAdLoading = {
                    it?.let {
                        mainViewModel.setInterstitialAdStart(it.route, it.adStart)
                    }
                },
                adInitState = mainViewModel.adMobinitState,
                loading = loading,
                setLoading = {
                    loading = it
                },
                itemSize = Integer.MAX_VALUE,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SettingViewPreView() {
    RatelappTheme {
        SettingsAppManager(null, hiltViewModel())
    }
}
