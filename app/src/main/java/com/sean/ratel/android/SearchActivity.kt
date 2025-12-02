package com.sean.ratel.android

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.search.SearchScreen
import com.sean.ratel.android.ui.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : FragmentActivity() {
    val searchViewModel by viewModels<SearchViewModel>()
    val adViewModel by viewModels<AdViewModel>()
    val mainViewModel by viewModels<MainViewModel>()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) window.decorView
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            enableEdgeToEdge()
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        setContent {
            SearchScreen(
                searchViewModel,
                adViewModel,
                mainViewModel,
                finish = { finish() },
            )
        }
        searchViewModel.sendGALog(
            screenName = GASplashAnalytics.SCREEN_NAME[SEARCH_SCREEN] ?: "",
            eventName = GASplashAnalytics.Event.SEARCH_VIEW,
            actionName = GASplashAnalytics.Action.VIEW,
            parameter = mapOf(),
        )

        deeLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deeLink(intent)
    }

    private fun deeLink(intent: Intent?) {
        val query = intent?.getStringExtra("query")

        searchViewModel.setDeepLinkQuery(query)
    }
}
