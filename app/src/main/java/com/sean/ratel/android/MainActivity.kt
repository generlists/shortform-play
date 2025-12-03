package com.sean.ratel.android

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.android.UnifiedLinkHandler
import com.sean.ratel.android.data.android.UnifiedLinkHandler.Companion.APP_MANAGER
import com.sean.ratel.android.data.android.UnifiedLinkHandler.Companion.HOME
import com.sean.ratel.android.data.android.UnifiedLinkHandler.Companion.SEARCH
import com.sean.ratel.android.data.android.UnifiedLinkHandler.Companion.SETTING
import com.sean.ratel.android.data.android.UnifiedLinkHandler.Companion.SHARE
import com.sean.ratel.android.data.android.UnifiedLinkHandler.Companion.SHORTFORM
import com.sean.ratel.android.data.android.UnifiedLinkHandler.Companion.YOUTUBE
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.GoogleMobileAdsConsentManager
import com.sean.ratel.android.ui.end.YouTubeEndFragment
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.Destination.Screen.Companion.BASE_DEEPLINK_URL
import com.sean.ratel.android.ui.pip.PipAction
import com.sean.ratel.android.utils.PhoneUtil
import com.sean.ratel.android.utils.UIUtil.getEndFragment
import com.sean.ratel.android.utils.UIUtil.hasPipPermission
import com.sean.ratel.player.core.util.launch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * 1.UI 는 컴포즈로 선언적 프로그래밍
 * 2.clean architecture 방식 도메인,엔티티 차용
 * 3. flow 및 corutine 활용
 * 4. hilt 를 통한 injection
 */
@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var unifiedLinkHandler: UnifiedLinkHandler

    val mainViewModel by viewModels<MainViewModel>()
    val adViewModel by viewModels<AdViewModel>()

    @Inject
    lateinit var log: GALog
    private val pipButtonState = MutableSharedFlow<Int>(extraBufferCapacity = 1)

    private val currentItem = MutableStateFlow(0)

    private var currentFragment: YouTubeEndFragment? = null

    private var pipBroadcastReceiver: BroadcastReceiver? = null

    @Inject
    lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    @Inject
    lateinit var remoteConfig: FirebaseRemoteConfig

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) window.decorView
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            enableEdgeToEdge()
        } else {
            // Android 15 이상에서는 enableEdgeToEdge() 미사용
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        // Remote Config
        launch {
            mainViewModel.firebaseRemoteConfig(remoteConfig)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            installSplashScreen()
        }

        adViewModel.setForceClearCache(intent.getBooleanExtra("clear_cache", false))

        googleMobileAdsConsentManager.gatherConsent(this) { error ->
            if (error != null) RLog.d(TAG, "${error.errorCode}: ${error.message}")

            if (googleMobileAdsConsentManager.canRequestAds) adViewModel.initAdMob(this)

            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                mainViewModel.setPrivacyOptionMenu(
                    true,
                )
            }
        }
        if (googleMobileAdsConsentManager.canRequestAds) adViewModel.initAdMob(this)

        setContent {
            ShortFormPlayApp(
                mainViewModel = mainViewModel,
                adViewModel = adViewModel,
                finish = { finish() },
            )
        }

        mainViewModel.sendGALog(Event.APP_OPEN, route = Destination.Home.route)

        launch {
            combine(
                mainViewModel.pipClick,
                mainViewModel.viewPager2,
            ) { pipClick, viewPager2 ->
                Pair(pipClick, viewPager2)
            }.collect { (pipClick, viewPager2) ->
                if (pipClick.first) {
                    currentFragment = getEndFragment(this@MainActivity, viewPager2)
                }
            }
        }

        launch {
            pipButtonState.collect {
                when (it) {
                    PipAction.PAUSE.intentExtraValue -> {
                        currentFragment?.pause()
                        currentFragment?.pipButtonState()
                        pipButtonState.tryEmit(0)
                    }
                    PipAction.PLAY.intentExtraValue -> {
                        currentFragment?.play()
                        currentFragment?.pipButtonState()
                        pipButtonState.tryEmit(0)
                    }
                    PipAction.SKIP_PREVIOUS.intentExtraValue -> {
                        val currentIndex = currentFragment?.pager?.currentItem ?: 0
                        if (currentIndex == 0) {
                            Toast
                                .makeText(
                                    baseContext,
                                    this@MainActivity.getString(R.string.pip_frist_video_message),
                                    Toast.LENGTH_LONG,
                                ).show()
                        } else {
                            currentFragment?.pager?.setCurrentItem(currentItem.value - 1, false)
                            currentItem.value -= 1
                            currentFragment?.pipButtonState()
                        }
                        pipButtonState.tryEmit(0)
                    }
                    PipAction.SKIP_NEXT.intentExtraValue -> {
                        val totalSize = currentFragment?.pager?.adapter?.itemCount ?: 0
                        val index = currentFragment?.pager?.currentItem ?: 0
                        val isLast = (totalSize - 1) == index

                        RLog.d(
                            "MainActivity",
                            "isNextAction totalSize  $totalSize , index : $index  , isLast : $isLast",
                        )
                        if (isLast) {
                            Toast
                                .makeText(
                                    baseContext,
                                    this@MainActivity.getString(R.string.pip_last_video_message),
                                    Toast.LENGTH_LONG,
                                ).show()
                        } else {
                            currentFragment?.pager?.setCurrentItem(currentItem.value + 1, false)
                            currentItem.value += 1
                            currentFragment?.pipButtonState()
                        }
                        pipButtonState.tryEmit(0)
                    }
                }
            }
        }
        // deep link
        deepLink()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val target = intent.getStringExtra("target")
        val videoId = intent.getStringExtra("videoId")
        val viewType = intent.getStringExtra("viewType")
        val selectedIndex = intent.getIntExtra("selectedIndex", 0)
        val categoryKey = intent.getStringExtra("categoryKey")

        RLog.d(
            "deepLink",
            "target : $target , videoId : $videoId , viewType : $viewType , categoryKey$categoryKey selectedIndex : $selectedIndex",
        )
        when (viewType) {
            ViewType.SearchShortsVideo.name -> {
                if (target == "youtube_end" && videoId != null) {
                    mainViewModel.setSearchCategoryShortFormVieo()
                    mainViewModel.goEndContent(
                        Destination.Search.route,
                        ViewType.SearchShortsVideo,
                        0,
                        null,
                        videoId,
                    )
                }
            }
            ViewType.SearchShortsDaily.name -> {
                if (target == "youtube_end" && categoryKey != null) {
                    mainViewModel.goEndContent(
                        Destination.Search.route,
                        ViewType.SearchShortsDaily,
                        selectedIndex,
                        null,
                        videoId,
                    )
                }
            }
        }

        deepLink()
    }

    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)

        RLog.d("MainActivity", "isInPictureInPictureMode : $isInPictureInPictureMode")

        if (isInPictureInPictureMode) {
            val pipBroadcastReceiver = pipBroadcastReceiver ?: PipBroadcastReceiver()
            registerReceiver(
                pipBroadcastReceiver,
                PipAction.getIntentFilter(),
                RECEIVER_NOT_EXPORTED,
            )
            this@MainActivity.pipBroadcastReceiver = pipBroadcastReceiver
        } else {
            pipBroadcastReceiver?.let {
                unregisterReceiver(it)
            }
        }
        launch {
            mainViewModel.viewPager2.collect {
                mainViewModel.setPIPClick(Pair(isInPictureInPictureMode, it))
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    /**
     * [BroadcastReceiver] to handle PIP button action and check VOIP status
     */
    inner class PipBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?,
        ) {
            intent ?: return
            RLog.d(
                TAG,
                "onReceive() , intent=$intent, action=${intent.action} extra : ${
                    intent.getIntExtra(
                        "splay.intent.extra.pip",
                        0,
                    )
                }",
            )

            when {
                PipAction.isPauseAction(intent) -> pipButtonState.tryEmit(PipAction.PAUSE.intentExtraValue)
                PipAction.isPlayAction(intent) -> pipButtonState.tryEmit(PipAction.PLAY.intentExtraValue)
                PipAction.isPreviousAction(intent) -> pipButtonState.tryEmit(PipAction.SKIP_PREVIOUS.intentExtraValue)
                PipAction.isNextAction(intent) -> pipButtonState.tryEmit(PipAction.SKIP_NEXT.intentExtraValue)
            }
        }
    }

    @Override
    override fun onResume() {
        super.onResume()
        pipClickProcess()
    }

    private fun pipClickProcess() {
        launch {
            combine(
                mainViewModel.pipClick,
                mainViewModel.viewPager2,
            ) { pipClick, viewPager2 ->

                Pair(pipClick, viewPager2)
            }.collect { combinedResult ->

                val (pipClick, viewPager2) = combinedResult

                if (pipClick.first) {
                    val currentIndex = pipClick.second?.currentItem ?: 0
                    val fragmentManager = (this@MainActivity as FragmentActivity).supportFragmentManager
                    val itemId = viewPager2?.adapter?.getItemId(currentIndex)
                    val fragment = fragmentManager.findFragmentByTag("f$itemId") as? YouTubeEndFragment
                    if (hasPipPermission()) {
                        fragment?.onClickPipButton()
                        currentItem.value = pipClick.second?.currentItem ?: 0
                    }
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (isInPictureInPictureMode) {
            RLog.d(TAG, "Close button clicked in PIP mode")
            finish()
        }
    }

    override fun onStop() {
        super.onStop()

        if (isInPictureInPictureMode) {
            RLog.d(TAG, "PIP mode stopped, possibly due to Close button")
            finish()
        }
    }

    private fun setUnifiedLinkHandler(intent: Intent?) {
        RLog.d("deepLink", "setUnifiedLinkHandler $intent")
        if (intent?.data == null) return

        launch {
            // 이벤트 등록
            unifiedLinkHandler.setOnDeepLinkHandler { appLinkInfo ->
                val deepLinkType = appLinkInfo.deepLinkType
                val viewType = appLinkInfo.type
                val route = appLinkInfo.route
                val param1 = appLinkInfo.extraParam1
                val param2 = appLinkInfo.extraParam2
                val param3 = appLinkInfo.extraParam3
                val param4 = appLinkInfo.extraParam4
                when (deepLinkType) {
                    HOME, SETTING, SHORTFORM, APP_MANAGER -> {
                        RLog.d("deepLink", "HOME route : $route, videoId : $param1 , viewType : $viewType")

                        viewType?.let {
                            mainViewModel.setViewType(viewType)
                            mainViewModel.navigator.navigateTo(route)
                        }
                    }
                    YOUTUBE -> {
                        RLog.d("deepLink", "YOUTUBE route : $route, videoId : $param1 , viewType : $viewType")
                        param1?.let {
                            mainViewModel.goEndContent(route, viewType ?: ViewType.DeepLinkVideo, 0, null, param1)
                        }
                    }
                    SEARCH -> {
                        RLog.d("deeplink", "tab : $param2 query : $param1 , date : $param3 , category : $param4")
                        PhoneUtil.searchButton(this@MainActivity, param1, param2, param3, param4)
                    }
                    SHARE -> {
                        RLog.d("YouTubeContentEnd", "SHARE route : $route, videoId : $param1 , viewType : $viewType")
                        mainViewModel.goEndContent(route, appLinkInfo.type ?: ViewType.DeepLinkVideo, 0, null, param1)
                    }
                }
            }

            mainViewModel.mainShorts.collect { mainShorts ->
                mainShorts.second
                    .takeIf { it > 0 }
                    ?.run {
                        RLog.d("deepLink", "$this")
                        mainViewModel.setSearchCategoryShortFormVieo()
                        unifiedLinkHandler.goDeepLinKPage(this@MainActivity, intent)
                    }
            }
        }
    }

    private fun deepLink() {
        launch {
            val deepLineUrl = intent.data
            val value = mainViewModel.getInstallRerere()
            RLog.d("deepLink", "deepLineUrl : $deepLineUrl")
            if (deepLineUrl == null) {
                RLog.d("deepLink", "value : $value")
                value?.let {
                    RLog.d("deepLink", "value : $it")
                    setUnifiedLinkHandler(intent)
                } ?: run {
                    unifiedLinkHandler.setReferer(this@MainActivity, {
                        mainViewModel.setInstallReferer(it)
                        val referer = it.substringAfter("path=")
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = "$BASE_DEEPLINK_URL$referer".toUri()
                        setUnifiedLinkHandler(intent)
                    })
                }
            } else {
                setUnifiedLinkHandler(intent)
            }
        }
    }

    companion object {
        private val TAG = "MainActivity"
    }
}
