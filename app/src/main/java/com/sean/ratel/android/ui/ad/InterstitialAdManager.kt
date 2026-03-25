package com.sean.ratel.android.ui.ad

import android.util.Log
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.BuildConfig
import com.sean.ratel.android.data.common.RemoteConfig.END_AD_POSITION
import com.sean.ratel.android.data.common.RemoteConfig.getRemoteConfigIntValue
import com.sean.ratel.player.core.domain.YouTubeStreamPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import so.smartlab.common.ad.admob.AdsSdk
import so.smartlab.common.ad.admob.data.model.AdMobInitState
import so.smartlab.common.ad.admob.data.model.AdMobInterstitialAdState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialAdManager
    @Inject
    constructor(
        val adsSdk: AdsSdk,
    ) {
        private var interstitialCollectJob: Job? = null
        private var showAd = false

        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun launchAdRequest(
            createPosition: Int,
            adTriggerState: AdTriggerState,
            youTubeStreamPlayer: YouTubeStreamPlayer,
            fromSearchComplete: (Boolean) -> Unit,
            showLoading: (Boolean) -> Unit,
        ) {
            if (createPosition == adTriggerState.selection) {
                RLog.d("InterstitialAdManager", "showAd $showAd")
                if (!showAd && (
                        adTriggerState.fromSearch ||
                            shouldTriggerAd(
                                adTriggerState.selection,
                                adTriggerState.totalSize,
                            )
                    )
                ) {
                    showLoading(true)
                    if (youTubeStreamPlayer.isPlaying()) {
                        youTubeStreamPlayer.pause()
                    }

                    handleAdFlow(adTriggerState, fromSearchComplete, showLoading)
                }
            }
        }

        fun shouldTriggerAd(
            selection: Int,
            totalSize: Int,
        ): Boolean {
            val adInterval = getRemoteConfigIntValue(END_AD_POSITION)

            val isIntervalHit =
                selection > 0 &&
                    totalSize > adInterval &&
                    selection % adInterval == 0

            // Log.d("InterstitialAdManager","totalSize : $totalSize isIntervalHit : $isIntervalHit")
            return isIntervalHit
        }

        private fun handleAdFlow(
            adTriggerState: AdTriggerState,
            fromSearchComplete: (Boolean) -> Unit,
            showLoading: (Boolean) -> Unit,
        ) {
            // Log.d("InterstitialAdManager","initAdMobState : $adTriggerState.initAdMobState")
            if (adTriggerState.initAdMobInitState is AdMobInitState.InitComplete) {
                showAd = true
                requestInitInterstitialAd({ adState ->
                    Log.d("InterstitialAdManager", "$adState")
                    if (adState == null ||
                        adState is AdMobInterstitialAdState.FullScreenContentDismiss ||
                        adState is AdMobInterstitialAdState.AdError
                    ) {
                        fromSearchComplete(false)
                        showLoading(false)
                    }
                })
            }
        }

        fun requestInitInterstitialAd(initInterstitialAdState: (AdMobInterstitialAdState?) -> Unit) {
            scope.launch {
                adsSdk.initInterstitialAd(BuildConfig.INTERSTITIALAd_UNIT_ID)
            }

            interstitialCollectJob?.cancel()
            interstitialCollectJob =
                scope.launch {
                    adsSdk.interstitialState.collect { event ->
                        Log.d("InterstitialAdManager", "initAdMobState : $event")
                        initInterstitialAdState(event)

                        if (event is AdMobInterstitialAdState.AdLoadComplete) {
                            adsSdk.showInterstitialAds()
                        }
                    }
                }
        }

        fun clearShowAd() {
            showAd = false
        }

        data class AdTriggerState(
            val selection: Int,
            val fromSearch: Boolean,
            // val playbackState: YouTubeStreamPlaybackState,
            val totalSize: Int,
            val initAdMobInitState: AdMobInitState,
            // val shouldTriggerAd: Boolean,
        )
    }
