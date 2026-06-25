package com.sean.ratel.android.ui.home

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.sean.ratel.android.R
import com.sean.ratel.android.data.api.ApiResult
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.data.common.STRINGS.APP_NAME
import com.sean.ratel.android.data.common.STRINGS.INTER_AD_MAX
import com.sean.ratel.android.data.dto.PromotionResponse
import com.sean.ratel.android.data.dto.VerifyIAPRequest
import com.sean.ratel.android.data.local.pref.PromotionPreference
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.repository.BillingRepository
import com.sean.ratel.android.utils.UIUtil.calculateDiscountedPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import so.smartlab.common.ad.admob.AdsSdk
import so.smartlab.common.ad.admob.data.repository.AdsConfigProvider
import so.smartlab.common.iap.BillingConfig
import so.smartlab.common.iap.BillingManager
import so.smartlab.common.iap.model.BillingError
import so.smartlab.common.iap.model.BillingEvent
import so.smartlab.common.iap.ui.model.PremiumSheetData
import so.smartlab.common.utils.log.RLog
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BillingViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        private val adsConfig: AdsConfigProvider,
        private val config: BillingConfig,
        private val billingManager: BillingManager,
        private val billingRepository: BillingRepository,
        private val promotionPreference: PromotionPreference,
        private val gaLog: GALog,
        adsSdk: AdsSdk,
    ) : ViewModel() {
        // 광고 제거 여부
        private val _isAdRemoved = MutableStateFlow(billingManager.isAdRemoved())
        val isAdRemoved: StateFlow<Boolean> = _isAdRemoved.asStateFlow()

        // 상품 정보
        private val _productDetails = MutableStateFlow<ProductDetails?>(null)
        val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

        // 세일 여부
        val isSaleActive: StateFlow<Boolean> =
            _productDetails
                .map { details ->
                    details?.let { billingManager.isSaleActive(it) } ?: false
                }.stateIn(viewModelScope, SharingStarted.Lazily, false)

        // 현재 가격
        val currentPrice: StateFlow<String> =
            _productDetails
                .map { details ->
                    details?.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                }.stateIn(viewModelScope, SharingStarted.Lazily, "")

        // 구매 시트 표시 여부
        private val _showPurchaseSheet = MutableStateFlow(false)
        val showPurchaseSheet: StateFlow<Boolean> = _showPurchaseSheet.asStateFlow()

        // 토스트 메시지
        private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
        val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

        private val premiumPromotion = MutableStateFlow<PromotionResponse?>(null)
        private val premiumPromotionError = MutableStateFlow<Throwable?>(null)

        private val _premiumData =
            MutableSharedFlow<UiState<PremiumSheetData>>(
                replay = 1,
                extraBufferCapacity = 1,
            )
        val premiumData: SharedFlow<UiState<PremiumSheetData>> = _premiumData.asSharedFlow()

        private val _adSaleActive = MutableStateFlow<Boolean>(true)
        val adSaleActive: StateFlow<Boolean> = _adSaleActive

        private val _interstitialAdDisMissCount = MutableStateFlow<Int>(0)
        val interstitialAdDisMissCount: StateFlow<Int> = _interstitialAdDisMissCount.asStateFlow()

        init {
            RLog.d("In App Purchase", "[App] BillingViewModel init!!!!!!!")

            // 상품 정보 조회
            viewModelScope.launch {

                requestPremiumPromotion(APP_NAME, Locale.getDefault().toLanguageTag())

                val products = billingManager.queryProducts()
                RLog.d("In App Purchase", "[App] 상품 정보 조회 start _isAdRemoved : ${_isAdRemoved.value} , details : $products")
                _productDetails.value = products.second.firstOrNull()
                // 오퍼 아이디가 없으면 기간이 지났거나 1번 구매한 사용자로 치고 정가로 가격 수정
                if (products.first != BillingClient.BillingResponseCode.OK) {
                    _premiumData.tryEmit(UiState.Error(context.getString(R.string.iap_purchase_not_enviroment)))
                    return@launch
                }
                if (premiumPromotionError.value != null) {
                    _toastMessage.tryEmit(context.getString(R.string.api_server_error))
                    return@launch
                }

                combine(_productDetails, premiumPromotion) { product, promotion ->
                    RLog.d("In App Purchase", "[App] product is null: ${product == null} , promotion is null: ${promotion == null}")

                    if (product == null || promotion == null) {
                        Log.d("In App Purchas", "[App] Loading 반환")
                        return@combine UiState.Loading
                    }

                    val originalPriceMicros =
                        product.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 4900_00L
                    val discountPercent = promotion.discountPercent ?: 39
                    val discountPrice = calculateDiscountedPrice(originalPriceMicros, discountPercent)
                    val offer = product.oneTimePurchaseOfferDetailsList?.firstOrNull { it.offerId == "launch-sale" }
                    RLog.d("In App Purchase", "[APP] offer : $offer")
                    _adSaleActive.value = premiumPromotion.value?.isPromotionActive ?: false
                    RLog.d("In App Purchase", "[App] isSaleActiveisSaleActive : ${_adSaleActive.value}")

                    UiState.Success(
                        offer?.let {
                            PremiumSheetData(
                                productId = product.productId,
                                promoTitle =
                                    premiumPromotion.value?.promoTitle
                                        ?: context.getString(R.string.promotion_title),
                                promoDesc =
                                    premiumPromotion.value?.promoDesc
                                        ?: context.getString(R.string.promotion_des),
                                promoNoti = premiumPromotion.value?.promoNoti,
                                badgeText = context.getString(R.string.promotion_badge),
                                originalPrice =
                                    String.format(
                                        context.getString(R.string.promotion_original_price),
                                        _productDetails.value?.oneTimePurchaseOfferDetails?.formattedPrice,
                                    ),
                                discountedPrice = discountPrice,
                                discountPercent =
                                    String.format(
                                        context.getString(R.string.promotion_discount_percent),
                                        discountPercent.toString(),
                                    ),
                                dDayText = promotion.dDayText,
                                features = promotion.features,
                                buttonText = context.getString(R.string.promotion_discount_purchase),
                                isPromotionActive = _adSaleActive.value,
                            )
                        } ?: run {
                            PremiumSheetData(
                                promoTitle = context.getString(R.string.remove_ad),
                                promoDesc = context.getString(R.string.promotion_des),
                                originalPrice =
                                    String.format(
                                        context.getString(R.string.original_price),
                                        config.removeAdsOriginalPriceMicros,
                                    ),
                                features =
                                    listOf(
                                        context.getString(R.string.banner_ad_remove),
                                        context.getString(R.string.inter_ad_remove),
                                    ),
                                buttonText = context.getString(R.string.purchase),
                                isPromotionActive = false,
                                productId = product.productId,
                                // promoNoti = null,
                                // purchaseToken = "pkcdiedbigecfjomjloeddln.AO-J1Oz1sAfZHwW_pwLeLqJ4Ru0xGD1iWuwZanCOCcVjENq2TEF7Ca0lX_XZLNf2ZhC3SjC1dEKlqMYqjSyU1C-vVGvAQNabcPr2dRp_MbuuJ_IQ4lRd548"
                            )
                        },
                    )
                }.collect { state ->
                    _premiumData.tryEmit(state)

                    RLog.d("In App Purchase", "[App] state :  $state")
                }
            }

            // billingEvent 수신
            viewModelScope.launch {
                billingManager.billingEvent.collect { event ->
                    RLog.d("In App Purchase", "[App]billingEvent 수신  PurchaseSuccess  billingEvent : $event")
                    when (event) {
                        is BillingEvent.PurchaseSuccess -> {

                            RLog.d("In App Purchase", "[App] billingEvent 수신 PurchaseSuccess start details : ${_isAdRemoved.value}")

                            val productId = event.result.productId

                            if (productId.isEmpty()) {
                                Log.e("IAP", "billingEvent 수신 productId is empty → skip verify")
                                return@collect
                            }

                            verifyIAP(
                                VerifyIAPRequest(
                                    packageName = event.result.packageName,
                                    productId = productId,
                                    purchaseToken = event.result.purchaseToken,
                                ),
                            )
                            _isAdRemoved.value = billingManager.isAdRemoved()
                            _showPurchaseSheet.value = false
                            Log.e("IAP", "구매가 완료되었습니다 시작!!!!!")
                            _toastMessage.tryEmit(context.getString(R.string.iap_purchase_complete))
                        }

                        is BillingEvent.PurchaseRestored -> {

                            val purchases = event.results
                            RLog.d("In App Purchase", "[App] billingEvent 수신 PurchaseRestored $purchases")

                            if (purchases.isEmpty()) {
                                _toastMessage.tryEmit(context.getString(R.string.iap_restore_not_founded))
                                return@collect
                            }

                            for (purchase in purchases) {

                                val productId = purchase.productId
                                val token = purchase.purchaseToken

                                if (productId.isEmpty()) {
                                    RLog.e("In App Purchase", "billingEvent 수신 [App] productId missing → skip verify")
                                    continue
                                }
                                RLog.d("In App Purchase", "billingEvent 수신 packageName : ${context.packageName}")
                                RLog.d("In App Purchase", "billingEvent 수신 productId : $productId")
                                RLog.d("In App Purchase", "billingEvent 수신 token : $token")

                                verifyIAP(
                                    VerifyIAPRequest(
                                        packageName = context.packageName,
                                        productId = productId,
                                        purchaseToken = token,
                                    ),
                                )
                            }
                            _isAdRemoved.value = billingManager.isAdRemoved()
                            _showPurchaseSheet.value = false
                            // _toastMessage.tryEmit(context.getString(R.string.iap_restore_purchase))
                        }

                        is BillingEvent.PurchasePending -> {
                            RLog.d("In App Purchase", "[App] billingEvent 수신 PurchasePending: 결제 대기 중입니다. 완료 후 자동 반영됩니다")
                            _isAdRemoved.value = false

                            _toastMessage.tryEmit(context.getString(R.string.iap_purchase_ready))
                        }

                        is BillingEvent.PurchaseCanceled -> {
                            // 조용히 처리
                            _isAdRemoved.value = billingManager.isAdRemoved()
                            RLog.d(
                                "In App Purchase",
                                "[App] billingEvent 수신 PurchaseCanceled isAdRemoved : ${billingManager.isAdRemoved()}",
                            )
                        }

                        is BillingEvent.PurchaseFailed -> {
                            RLog.d("In App Purchase", "[App] billingEvent 수신 PurchaseFailed ${event.error}")
                            when (event.error) {
                                is BillingError.RootedDevice -> {
                                    _toastMessage.tryEmit(context.getString(R.string.iap_root_device))
                                }

                                is BillingError.ItemAlreadyOwned -> {
                                    _toastMessage.tryEmit(context.getString(R.string.iap_allready_purchase))
                                }

                                else -> {
                                    _toastMessage.tryEmit(context.getString(R.string.iap_purchase_failed))
                                }
                            }
                        }

                        is BillingEvent.BillingNotAvailable -> {
                            RLog.d("In App Purchase", "[App] BillingNotAvailable Google Play 서비스를 확인해주세요")
                            _toastMessage.tryEmit(context.getString(R.string.iap_google_play_service))
                        }
                    }
                }
            }
        }

        // 구매 실행
        fun purchase(activity: Activity) {
            val details =
                _productDetails.value ?: run {
                    _toastMessage.tryEmit(context.getString(R.string.iap_product_request_service))
                    return
                }
            RLog.d("In App Purchase", "[App] purchase start details : $details")
            billingManager.purchase(activity, details)
        }

        // 구매 복원
        fun restore() {
            viewModelScope.launch {
                RLog.d("In App Purchase", "[App] purchase restore")

                billingManager.restorePurchases()
            }
        }

        fun requestPremiumPromotion(
            appId: String,
            locale: String,
        ) {
            viewModelScope.launch {
                billingRepository
                    .requestPremiumPromotion(
                        appId,
                        locale,
                    ).collect { response ->
                        when (response) {
                            is ApiResult.Loading -> {
                                RLog.d("In App Purchase", "[App] Api Loading")
                            }

                            is ApiResult.Success -> {
                                RLog.d("In App Purchase", "[App] Api Success ${response.data}")
                                if (response.data.success) {
                                    RLog.d("SearchViewModel", "[App] Api data : ${response.data}")

                                    premiumPromotion.value = response.data
                                } else {
                                    premiumPromotion.value = null
                                }
                            }

                            is ApiResult.Exception -> {
                                RLog.d("In App Purchase", "[App] Api Exception")

                                premiumPromotion.value = null
                                premiumPromotionError.value = response.e
                                _premiumData.tryEmit(UiState.Error(context.getString(R.string.api_server_error)))
                            }

                            else -> {
                            }
                        }
                    }
            }
        }

        fun verifyIAP(verifyIAPRequest: VerifyIAPRequest) {
            viewModelScope.launch {
                billingRepository
                    .verifyIap(verifyIAPRequest)
                    .collect { response ->
                        when (response) {
                            is ApiResult.Loading -> {
                                RLog.d("In App Purchase", "[App] Api Loading")
                            }

                            is ApiResult.Success -> {
                                RLog.d("In App Purchase", "[App] Api Success ${response.data}")
                                if (response.data.success) {
                                    if (response.data.data?.active == true) {
                                        RLog.d("In App Purchase", "[App] Api data :광고 제거 적용")
                                        _isAdRemoved.value = true
                                    } else {
                                        // 결제는 조회됐지만 active 아님
                                        RLog.d("In App Purchase", "[App] Api data :결제는 조회됐지만 active 아님")
                                        _isAdRemoved.value = false
                                        // premiumManager.setPremium(false)
                                    }
                                    // premiumPromotion.value = response.data
                                } else {
                                    _isAdRemoved.value = false
                                    RLog.e("In App Purchase", "verify fail: ${response.data.error?.code} / ${response.data.error?.message}")
                                    premiumPromotionError.value = Throwable("error")
                                    // premiumManager.setPremium(false)
                                }
                            }

                            is ApiResult.Exception -> {
                                RLog.d("In App Purchase", "111[App] Api Exception")
                                premiumPromotion.value = null
                                premiumPromotionError.value = response.e
                                _premiumData.tryEmit(UiState.Error(context.getString(R.string.api_server_error)))
                            }

                            else -> {
                            }
                        }
                    }
            }
        }

        val doNotShowAgain: StateFlow<Boolean> =
            promotionPreference
                .isShowDoNotShowAgain()
                .onEach {
                    RLog.d("In App Purchase", "[App] isShowDoNotShowAgain : $it")
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = false,
                )

        suspend fun markDoNotShowAgain(isHide: Boolean) {
            RLog.d("In App Purchase", "[App] save check : $isHide")
            promotionPreference.markDoNotShowAgain(isHide)
        }

        fun setInterstitialAdDisMissCount(count: Int) {
            RLog.d("Route!!!!!", "[App]  count : $count , _interstitialAdDisMissCount : ${_interstitialAdDisMissCount.value}")

            if (count >= INTER_AD_MAX) {
                _interstitialAdDisMissCount.value = 1
                return
            }

            _interstitialAdDisMissCount.value = count + 1
        }

        fun sendGALog(
            screenName: String,
            eventName: String,
            actionName: String,
            parameter: Map<String, String>,
        ) {
            gaLog.sendEvent(
                screenName,
                eventName,
                actionName,
                parameter,
            )
        }
    }
