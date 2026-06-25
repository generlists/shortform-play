package com.sean.ratel.android.ui.ad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.ui.home.BillingViewModel
import com.sean.ratel.android.ui.progress.LoadingMainPlaceholder
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import kotlinx.coroutines.flow.StateFlow
import so.smartlab.common.ad.admob.data.model.AdMobInitState
import so.smartlab.common.ad.admob.data.model.AdMobInterstitialAdState
import so.smartlab.common.utils.log.RLog

@Suppress("ktlint:standard:function-naming")
@Composable
fun InterstitialAdPage(
    adTarget: AdTarget?,
    interstitialAdManager: InterstitialAdManager,
    billingViewModel: BillingViewModel,
    adInitState: StateFlow<AdMobInitState>,
    setAdLoading: (AdTarget?) -> Unit,
    loading: Boolean,
    setLoading: (Boolean) -> Unit,
    itemSize: Int,
    isEnd: Boolean = false,
) {
    val initAdMobState by adInitState.collectAsState()
    var requesting by remember { mutableStateOf<Boolean>(false) }
    val interstitialDisMissCount by billingViewModel.interstitialAdDisMissCount.collectAsStateWithLifecycle(
        initialValue = 0,
    )
    var onRemoveAdsClick by remember { mutableStateOf(false) }
    var showPromotionPopup by remember { mutableStateOf(true) }
    val premiumSheetData by billingViewModel.premiumData.collectAsStateWithLifecycle(initialValue = UiState.Idle)
    RLog.d(
        "InterstitialAdPage",
        " adTarget : $adTarget progressLoading $loading , $initAdMobState",
    )
    if (adTarget?.adStart == true) {
        if (initAdMobState is AdMobInitState.InitComplete) {
            if (requesting) return

            interstitialAdManager.requestInitInterstitialAdPage({ adState ->
                RLog.d("InterstitialAdPage", "$adState")

                if (adState == null ||
                    adState is AdMobInterstitialAdState.FullScreenContentDismiss ||
                    adState is AdMobInterstitialAdState.AdError
                ) {
                    requesting = false

                    initLoading(adLoading = {
                        RLog.d("InterstitialAdPage", "adLoading :  $it")
                        setAdLoading(AdTarget(adTarget.route, it))
                    }, loading = {
                        RLog.d("Route!!!!!", "2 $it")
                        setLoading(it)
                    }, itemSize)
                    // 팝업 뜬 카운트

                    RLog.d("Route!!!!!", "21111:  $interstitialDisMissCount")
                    if (interstitialDisMissCount == STRINGS.INTER_AD_MAX) {
                        onRemoveAdsClick = true
                    }
                    billingViewModel.setInterstitialAdDisMissCount(interstitialDisMissCount)
                }
                if (adState is AdMobInterstitialAdState.FullScreenContent && itemSize > 0) {
                    requesting = true
                    setLoading(false)
                }
            })
        } else {
            if (initAdMobState !is AdMobInitState.InitStart) {
                initLoading(adLoading = {
                    setAdLoading(AdTarget(adTarget.route, it))
                }, loading = {
                    setLoading(it)
                }, itemSize)
            }
        }
    } else {
        LaunchedEffect(itemSize) {
            if (itemSize > 0) {
                setLoading(false)
            }
        }
    }

    if (loading) {
        val modifier =
            Modifier
                .fillMaxSize()
                .background(APP_BACKGROUND)

        Box(
            modifier,
            contentAlignment = Alignment.Center,
        ) {
            LoadingMainPlaceholder(modifier, loading = loading)
        }
    }
}

private fun initLoading(
    adLoading: (Boolean) -> Unit,
    loading: (Boolean) -> Unit,
    size: Int,
) {
    adLoading(false)
    if (size > 0) {
        loading(false)
    }
}
