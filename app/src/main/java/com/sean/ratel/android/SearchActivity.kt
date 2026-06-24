package com.sean.ratel.android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.home.BillingViewModel
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.search.SearchScreen
import com.sean.ratel.android.ui.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import so.smartlab.common.utils.log.RLog

@AndroidEntryPoint
class SearchActivity : FragmentActivity() {
    val searchViewModel by viewModels<SearchViewModel>()
    val adViewModel by viewModels<AdViewModel>()
    val mainViewModel by viewModels<MainViewModel>()
    val billingViewModel by viewModels<BillingViewModel>()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) window.decorView
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        setContent {
            SearchScreen(
                searchViewModel,
                adViewModel,
                mainViewModel,
                billingViewModel,
                finish = { finish() },
            )
        }
        mainViewModel.initAdMobSDK(this)
        searchViewModel.sendGALog(
            screenName = GASplashAnalytics.SCREEN_NAME[SEARCH_SCREEN] ?: "",
            eventName = GASplashAnalytics.Event.SEARCH_VIEW,
            actionName = GASplashAnalytics.Action.VIEW,
            parameter = mapOf(),
        )
        mainViewModel.setInterstitialAdStart(Destination.Search.route, !billingViewModel.isAdRemoved.value)
        deeLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deeLink(intent)
    }

    private fun deeLink(intent: Intent?) {
        val query = intent?.getStringExtra("query")
        val tab = intent?.getStringExtra("tab")
        val date = intent?.getStringExtra("date")
        val category = intent?.getStringExtra("category")
        val topicCategory = intent?.getBundleExtra("topicCategory")
        RLog.d("deeplink", "query : $query , date : $date ,  tab : $tab , category :  $category")

        when (tab) {
            "keyword" -> searchViewModel.setDeepLinkQuery(query)
            "daily" -> searchViewModel.setDeepLinkTab(tab, date, category)
            else -> searchViewModel.setDeepLinkQuery(query)
        }
        searchViewModel.setTopicCategory(topicCategory)
    }
}
