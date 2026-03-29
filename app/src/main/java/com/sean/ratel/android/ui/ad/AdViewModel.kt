package com.sean.ratel.android.ui.ad

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.LoadAdError
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 광고과련 Event ViewModel
 */
@Suppress("ktlint:standard:property-naming")
@HiltViewModel
class AdViewModel
    @Inject
    constructor(
        val navigator: Navigator,
    ) : ViewModel() {
        private val _bottomBarHeight = mutableStateOf(56)
        val bottomBarHeight = _bottomBarHeight

        private val _adNativeFail = MutableStateFlow<LoadAdError?>(null)
        val adNativeFail: StateFlow<LoadAdError?> = _adNativeFail

        private val _forceClearCache = MutableStateFlow(false)
        val forceClearCache: StateFlow<Boolean> = _forceClearCache

        fun setForceClearCache(forceClearCache: Boolean) {
            _forceClearCache.value = forceClearCache
        }

        fun setBottomBarHeight(height: Int) {
            _bottomBarHeight.value = height
        }

        fun goMainHome() {
            viewModelScope.launch {
                navigator.navigateTo(Destination.Home.route, true)
            }
        }

        companion object {
            const val TAG: String = "ADVIEW"
        }
    }
