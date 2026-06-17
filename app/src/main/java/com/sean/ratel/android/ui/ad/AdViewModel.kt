package com.sean.ratel.android.ui.ad

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
@Deprecated("라이브러리 교체로 다른 view Model 로 이관")
@HiltViewModel
class AdViewModel
    @Inject
    constructor(
        val navigator: Navigator,
    ) : ViewModel() {
        private val _bottomBarHeight = mutableStateOf(56)
        val bottomBarHeight = _bottomBarHeight

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
