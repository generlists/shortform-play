package com.sean.ratel.android.ui.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.log.GAKeys.MAIN_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.cast.CastSessionState
import com.sean.ratel.android.ui.cast.YouTubePlayersManager
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.push.PushViewModel
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.ComposeUtil.GetShareLauncher
import com.sean.ratel.android.utils.MediaRouteButtonUtils
import com.sean.ratel.android.utils.PhoneUtil.searchButton
import com.sean.ratel.android.utils.PhoneUtil.shareAppLinkButton
import com.sean.ratel.android.utils.UIUtil.hasPipPermission
import com.sean.ratel.android.utils.findActivity
import so.smartlab.common.utils.log.RLog

@Suppress("ktlint:standard:function-naming")
@Composable
fun HomeTopBar(
    modifier: Modifier,
    mainViewModel: MainViewModel,
    pushViewModel: PushViewModel,
    billingViewModel: BillingViewModel,
    playersManager: YouTubePlayersManager,
    castConnectLoading: Boolean,
    isHomeNaviBar: String,
    historyBack: () -> Unit,
    privacyOptionClick: () -> Unit,
    notificationPage: () -> Unit = {},
    endMoreClick: (MainShortsModel?) -> Unit,
    castClickRoute: (String) -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color(0xCC000000),
                                        Color.Transparent,
                                    ),
                            ),
                    ),
        )
        if (isHomeNaviBar != Destination.YouTube.route) {
            Divider(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFF1C1C1E)),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(53.dp)
                    .clickable(enabled = false, onClick = {})
                    .background(
                        if (isHomeNaviBar == Destination.YouTube.route) {
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        // 80%
                                        Color(0xCC000000),
                                        Color.Transparent,
                                    ),
                            )
                        } else {
                            Brush.linearGradient(
                                // 동일한 빨간색으로 단일 색상 처리
                                colors = listOf(APP_BACKGROUND, APP_BACKGROUND),
                            )
                        },
                    ),
            contentAlignment = Alignment.CenterStart,
        ) {
            val density = LocalDensity.current
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp)
                        .onGloballyPositioned { coordinates ->
                            val topBarHeight =
                                with(density) {
                                    coordinates.size.height
                                        .toDp()
                                        .value
                                }
                            mainViewModel.setTopBarHeight(topBarHeight.toInt())
                        },
                // Row 내에서 수직 중앙 정렬
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val isPrivacy = mainViewModel.isPrivacyOptionMenu.collectAsState(false)
                if (isHomeNaviBar == Destination.Home.Main.route || isHomeNaviBar == Destination.Home.Main.ShortForm.route) {
                    Image(
                        painterResource(R.drawable.shortform_play_icon_main),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .width(42.dp)
                                .height(42.dp),
                        contentScale = ContentScale.Fit,
                    )
                    TitleBox()
                    Spacer(modifier = Modifier.weight(1f))
                    PrivacyOptionMenu(isPrivacy.value, privacyOptionClick)
                    CastButton(isHomeNaviBar, castConnectLoading, castClickRoute)
                    NotificationIconButton(
                        notificationPage,
                        mainViewModel,
                        pushViewModel,
                        billingViewModel,
                    )
                    SearchIconButton(mainViewModel, playersManager)
                } else if (isHomeNaviBar == Destination.YouTube.route) {
                    BackButton(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        historyBack,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    CastButton(isHomeNaviBar, castConnectLoading, castClickRoute)
                    SearchIconButton(mainViewModel, playersManager)
                    PIPButton(mainViewModel, playersManager)
                    MoreInfoButton(mainViewModel = mainViewModel, moreClick = endMoreClick)
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun BackButton(
    modifier: Modifier,
    historyBack: () -> Unit,
) {
    Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowBack,
        contentDescription = null,
        tint = Color.White,
        modifier =
            modifier
                .size(32.dp)
                .clickable {
                    historyBack()
                },
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun TitleBox() {
    Image(
        // 이미지 리소스
        painter = painterResource(id = R.drawable.main_text),
        contentDescription = stringResource(R.string.app_name),
        modifier = Modifier.padding(start = 10.dp),
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SharerIconButton(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val shareLauncer = GetShareLauncher(activity, mainViewModel)
    Icon(
        imageVector = Icons.Outlined.IosShare,
        contentDescription = null,
        tint = Color.White,
        modifier =
            Modifier
                .size(32.dp)
                .clickable {
                    shareAppLinkButton(context, shareLauncer)
                },
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SearchIconButton(
    mainViewModel: MainViewModel?,
    playersManager: YouTubePlayersManager,
) {
    val context = LocalContext.current
    val mainShorts = mainViewModel?.mainShorts?.collectAsState()
    val category =
        mainShorts
            ?.value
            ?.first
            ?.topicList
            ?.topicCategory
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ManageSearch,
        contentDescription = null,
        tint = Color.White,
        modifier =
            Modifier
                .size(32.dp)
                .clickable {
                    searchButton(context, category)
                    playersManager.sendGALog(
                        screenName = GASplashAnalytics.SCREEN_NAME.get(MAIN_SCREEN) ?: "",
                        eventName = GASplashAnalytics.Event.SELECT_SEARCH_BTN_CLICK,
                        actionName = GASplashAnalytics.Action.CLICK,
                        mapOf(),
                    )
                },
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun NotificationIconButton(
    notificationPage: () -> Unit,
    mainViewModel: MainViewModel,
    pushViewModel: PushViewModel,
    billingViewModel: BillingViewModel,
) {
    val permission by pushViewModel.hasPermission.collectAsState()
    val hasNewPush by pushViewModel.hasNewPush.collectAsState()
    val isAdRemoved by billingViewModel.isAdRemoved.collectAsStateWithLifecycle()
    IconButton(onClick = {
        mainViewModel.setInterstitialAdStart(Destination.Notifcation.route, !isAdRemoved)
        notificationPage()
    }) {
        val icon =
            if (permission) {
                Icons.Outlined.Notifications
            } else {
                Icons.Outlined.NotificationsOff
            }
        Box(modifier = Modifier.wrapContentSize()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )

            if (hasNewPush) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 0.dp, y = 2.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun PIPButton(
    mainViewModel: MainViewModel?,
    youTubePlayersManager: YouTubePlayersManager,
) {
    val context = LocalContext.current
    val pipAction = mainViewModel?.pipClick?.collectAsStateWithLifecycle(initialValue = null)
    val pipButtonEnabled = remember { mutableStateOf(false) }
    val castSessionState by youTubePlayersManager.castEventManager.castSession.collectAsStateWithLifecycle()
    val fragment = mainViewModel?.topPipClick?.collectAsStateWithLifecycle()
    val str = stringResource(R.string.setting_pip_go)

    LaunchedEffect(Unit) {
        mainViewModel?.buttonClickState?.collect {
            pipButtonEnabled.value = it
        }
    }
    Box(
        Modifier
            .height(64.dp)
            .width(64.dp)
            .clickable(
                enabled = pipButtonEnabled.value && castSessionState !is CastSessionState.SessionStart,
                onClick = {
                    if (!context.hasPipPermission()) {
                        Toast
                            .makeText(
                                context,
                                str,
                                Toast.LENGTH_SHORT,
                            ).show()

                        mainViewModel?.goSettingView()
                    } else {
                        RLog.d("PIP_CLICK", "currentFragment : $fragment")
                        val action = !(pipAction?.value?.first ?: false)
                        val viewPager = pipAction?.value?.second
                        mainViewModel?.setPIPClick(Pair(action, viewPager))
                        fragment?.value?.onClickPipButton()
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            // 이미지 리소스
            painter = painterResource(id = R.drawable.pip_button),
            contentDescription = null,
            modifier =
                Modifier
                    .width(32.dp)
                    .height(32.dp),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun PrivacyOptionMenu(
    isPrivacy: Boolean,
    privacyOptionClick: () -> Unit,
) {
    if (isPrivacy) {
        Box(
            modifier = Modifier.wrapContentSize(),
        ) {
            IconButton(onClick = privacyOptionClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun MoreInfoButton(
    mainViewModel: MainViewModel,
    moreClick: (MainShortsModel?) -> Unit,
) {
    Box(
        modifier = Modifier.wrapContentSize(),
    ) {
        val currentShorts by mainViewModel.currentMainShorts.collectAsStateWithLifecycle()
        IconButton(onClick = {
            moreClick(currentShorts)
        }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun CastButton(
    isHomeNaviBar: String,
    castConnectLoading: Boolean,
    castClickRoute: (String) -> Unit,
) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    val medialButton = MediaRouteButtonUtils.initMediaRouteButton(activity) ?: return

    MediaRouteButtonUtils.addMediaRouteButtonToPlayerUi(
        medialButton,
        android.R.color.white,
        null,
        null,
    )

    Box(
        modifier = Modifier.size(48.dp),
    ) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { _ ->
                medialButton
            },
        )

        if (castConnectLoading) {
            castClickRoute(isHomeNaviBar)
            CircularProgressIndicator(
                modifier =
                    Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = -5.dp, y = -8.dp)
                        .padding(2.dp),
                strokeWidth = 2.dp,
                color = APP_TEXT_COLOR,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun HomeTopBarPreview() {
    val mainViewModel: MainViewModel = hiltViewModel<MainViewModel>()
    val pushViewModel: PushViewModel = hiltViewModel<PushViewModel>()
    val billingViewModel: BillingViewModel = hiltViewModel<BillingViewModel>()

    RatelappTheme {
        // HomeTopBar(Modifier,mainViewModel ,pushViewModel,billingViewModel, Destination.Home.Main.route, {}, {},{},{})
    }
}
