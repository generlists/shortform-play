package com.sean.ratel.android.ui.home.setting

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.common.findActivity
import com.sean.ratel.android.ui.end.LoadingArea
import com.sean.ratel.android.ui.home.BillingViewModel
import com.sean.ratel.android.ui.push.PushViewModel
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.Background
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.ComposeUtil.GetShareLauncher
import com.sean.ratel.android.utils.ComposeUtil.PremiumPopup
import com.sean.ratel.android.utils.PhoneUtil
import com.sean.ratel.player.ui.ThemeMode
import so.smartlab.common.ad.admob.data.model.AdMobBannerState
import so.smartlab.common.utils.log.RLog

@Suppress("ktlint:standard:function-naming")
@Composable
fun Setting(
    viewModel: SettingViewModel,
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    pushViewModel: PushViewModel,
    billingViewModel: BillingViewModel,
) {
    SettingView(viewModel, mainViewModel, adViewModel, pushViewModel, billingViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingView(
    viewModel: SettingViewModel,
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    pushViewModel: PushViewModel,
    billingViewModel: BillingViewModel,
) {
    val activity = LocalContext.current.findActivity()
    val context = LocalContext.current
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
    val adFixedBannerState by mainViewModel.fixedBannerState.collectAsState()
    var adSize by remember { mutableStateOf(64) }
    val bottomBarHeight = adViewModel.bottomBarHeight.value
    val fromPermissionPage by pushViewModel.fromPermissionPage.collectAsState(initial = false)
    val shareLauncher = GetShareLauncher(activity, mainViewModel)
    val userId by viewModel.userId.collectAsState()

    val premiumData by billingViewModel.premiumData.collectAsStateWithLifecycle()
    val isRemoveAd by billingViewModel.isAdRemoved.collectAsStateWithLifecycle()
    var onRemoveAdsClick by remember { mutableStateOf(false) }

    adSize =
        when {
            adFixedBannerState is AdMobBannerState.AdLoadComplete -> {
                (adFixedBannerState as AdMobBannerState.AdLoadComplete).adSize.height
            }

            else -> {
                0
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
                historyBack = {
                    mainViewModel.runNavigationBack(
                        null,
                        false,
                        if (!fromPermissionPage) null else activity,
                    )
                    pushViewModel.setFromPermissionPage(false)
                },
                isShareButton = false,
                runSetting = { PhoneUtil.shareAppLinkButton(context, shareLauncher) },
                filterButton = false,
                onFilterChange = {},
                items = listOf(),
            )
        },
        containerColor = Background,
    ) { innerPadding ->
        RLog.d("Setting", "$innerPadding $adSize")

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
                item {
                    SettingsProfileHeader(
                        mainViewModel = mainViewModel,
                        isAdRemoved = isRemoveAd,
                        currentMode = ThemeMode.DARK,
                        name = userId,
                        username = "guest",
                        onClick = null,
                    )
                }
                item { SettingsService(viewModel, pushViewModel) }
                item {
                    Spacer(Modifier.height(16.dp))
                    Log.d("premiumData", "premiumData : $premiumData")
                    if (!isRemoveAd) {
                        when (val state = premiumData) {
                            is UiState.Loading, UiState.Idle -> {
                                // 로딩 인디케이터
                                LoadingArea(isLoading = true)
                            }

                            is UiState.Success -> {
                                RLog.d("hbungshin", "promoDesc : ${state.data.promoDesc}")
                                SettingAdRemoveRow(
                                    Modifier,
                                    promotionTitle = state.data.promoTitle,
                                    promotionDes = state.data.promoDesc,
                                    isSaleActive = state.data.isPromotionActive,
                                    discountPercent = state.data.discountPercent,
                                    state.data.originalPrice,
                                    onClick = {
                                        onRemoveAdsClick = true
                                    },
                                )
                            }

                            is UiState.Error -> {
                                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                            }
                        }
                        if (onRemoveAdsClick) {
                            PremiumPopup(
                                billingViewModel = billingViewModel,
                                forceDonotMessageRow = true,
                                show = { isShow, promotionButtonType ->
                                    onRemoveAdsClick = isShow
//                                    statisticArgs.sendEvent(
//                                        EventAction.ButtonClick,
//                                        EventName.SettingPromotionButtonClick,
//                                        Screen.Settings,
//                                        mapOf(Parms.AdPromotionButtonType.key to promotionButtonType.name),
//                                    )
                                },
                            )
                        }
                    }
                }
                item { SettingsCountry(viewModel) }
                item { SettingsVideo(viewModel) }
                item { SettingsApp(mainViewModel,billingViewModel, viewModel) }
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
