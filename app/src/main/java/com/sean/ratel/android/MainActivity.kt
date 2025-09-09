package com.sean.ratel.android

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.GoogleMobileAdsConsentManager
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.pip.PipAction
import com.sean.ratel.android.utils.UIUtil
import com.sean.ratel.android.utils.UIUtil.hasPipPermission
import com.sean.ratel.player.core.util.launch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
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
    val mainViewModel by viewModels<MainViewModel>()
    val adViewModel by viewModels<AdViewModel>()

    @Inject
    lateinit var log: GALog
    private val currentPipClick = MutableStateFlow(false)
    private val currentItem = MutableStateFlow(0)

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
    }

    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)

        RLog.d(TAG, "isInPictureInPictureMode : $isInPictureInPictureMode")

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
                currentPipClick.value = isInPictureInPictureMode
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
                "SKT",
                "onReceive() , intent=$intent, action=${intent.action} extra : ${
                    intent.getIntExtra(
                        "splay.intent.extra.pip",
                        0,
                    )
                }",
            )
            val fragment = UIUtil.findFragment(this@MainActivity, currentItem.value)

            when {
                PipAction.isPauseAction(intent) -> {
                    fragment?.pause()
                    fragment?.pipButtonState()
                }

                PipAction.isPlayAction(intent) -> {
                    fragment?.play()
                    fragment?.pipButtonState()
                }

                PipAction.isPreviousAction(intent) -> {
                    launch {
                        val totalSize = fragment?.pager?.adapter?.itemCount ?: 0
                        val index = fragment?.pager?.currentItem ?: 0
                        val isLast = (totalSize - 1) == index
                        RLog.d(
                            TAG,
                            "isPreviousAction totalSize  $totalSize , index : $index  , isLast : $isLast",
                        )
                        // 여러번 클릭하거나 다른 액션 갓다오면 한 3번 클릭해도 반응이 없다
                        if (index == 0) {
                            Toast
                                .makeText(
                                    baseContext,
                                    this@MainActivity.getString(R.string.pip_frist_video_message),
                                    Toast.LENGTH_LONG,
                                ).show()
                        } else {
                            fragment?.pager?.setCurrentItem(currentItem.value - 1, false)
                            currentItem.value -= 1
                            fragment?.pipButtonState()
                        }
                    }
                }

                PipAction.isNextAction(intent) -> {
                    launch {
                        val totalSize = fragment?.pager?.adapter?.itemCount ?: 0
                        val index = fragment?.pager?.currentItem ?: 0
                        val isLast = (totalSize - 1) == index

                        RLog.d(
                            TAG,
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
                            fragment?.pager?.setCurrentItem(currentItem.value + 1, false)
                            currentItem.value += 1
                            fragment?.pipButtonState()
                        }
                    }
                }
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
            mainViewModel.pipClick.collect {
                if (it.first && !currentPipClick.value) {
                    RLog.d(TAG, "PIP 클릭이벤트 : currentItem : ${it.second?.currentItem ?: 0}")
                    val currentIndex = it.second?.currentItem ?: 0
                    val fragment =
                        UIUtil.findFragment(this@MainActivity, currentIndex)
                    if (hasPipPermission()) {
                        fragment?.onClickPipButton()
                        currentItem.value = it.second?.currentItem ?: 0
                        currentPipClick.value = true
                    }
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (isInPictureInPictureMode) {
            Log.d("PIP", "Close button clicked in PIP mode")
            finish()
        }
    }

    override fun onStop() {
        super.onStop()

        if (isInPictureInPictureMode) {
            RLog.d("SKT", "PIP mode stopped, possibly due to Close button")
            finish()
        }
    }

    companion object {
        private val TAG = "MainActivity"
    }
}
