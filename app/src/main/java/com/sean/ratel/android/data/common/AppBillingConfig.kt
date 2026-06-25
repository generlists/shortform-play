package com.sean.ratel.android.data.common

import so.smartlab.common.iap.BillingConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBillingConfig
    @Inject
    constructor(
        adDelProductId: String,
        offer: String,
        subProductIds: List<String>,
        consumeProductIds: List<String>,
        originalPrice: Float,
    ) : BillingConfig {
        override val removeAdsProductId = adDelProductId
        override val removeAdsOriginalPriceMicros = originalPrice
        override val offerId = offer
        override val subscriptionProductIds = subProductIds
        override val consumableProductIds = consumeProductIds
    }
