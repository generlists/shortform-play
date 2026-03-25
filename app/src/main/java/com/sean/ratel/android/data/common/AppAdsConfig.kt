package com.sean.ratel.android.data.common

import so.smartlab.common.ad.admob.data.repository.AdsConfigProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAdsConfig
    @Inject
    constructor(
        admobId: String,
        testDeviceId: String,
        banner: String,
        adaptiveUnitId: String,
        nativeUnitId: String,
        openId: String,
        interstitialId: String,
    ) : AdsConfigProvider {
        override val admobAppId = admobId
        override val bannerUnitId = banner
        override val testDeviceHashedId = testDeviceId
        override val adaptiveBannerUnitId = adaptiveUnitId
        override val nativeAdUnitId = nativeUnitId
        override val openUnitId = openId

        override val interstitialUnitId = interstitialId
    }
