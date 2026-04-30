package com.sean.ratel.android.ui.toolbox

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.DrawablePainter
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.URL_GOOGLE_PLAY_APP
import com.sean.ratel.android.data.common.STRINGS.URL_MY_PACKAGE_NAME
import com.sean.ratel.android.data.domain.model.toolbox.AppManagerInfo
import com.sean.ratel.android.ui.ad.AdTarget
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.InterstitialAdPage
import com.sean.ratel.android.ui.common.DropDownMenuComposable
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.progress.LoadingPlaceholder
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR

private const val TAG = "AppManagerView"

@Suppress("ktlint:standard:function-naming")
@Composable
fun AppManagerView(
    modifier: Modifier,
    viewModel: AppManagerViewModel,
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
) {
    BackHandler(enabled = true) {
        mainViewModel.runNavigationBack()
    }
    val data = remember { viewModel.contents }
    var filterAction by remember { mutableIntStateOf(-1) }
    val isLoaded by viewModel.appListLoaded.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
    var loading by remember { mutableStateOf(true) }
    val adLoading by mainViewModel.interstitialAdStart.collectAsState(initial = null)

    Box(
        Modifier
            .fillMaxSize()
            .padding(top = insetPaddingValue.calculateTopPadding()),
    ) {
        TopNavigationBar(
            titleResourceId = R.string.app_manager,
            historyBack = { mainViewModel.runNavigationBack() },
            isShareButton = false,
            runSetting = {},
            filterButton = true,
            // 파라메터로 넣고 싶을때는 이렇게 함수로 넘겨서 셋팅
            onFilterChange = { newFilterAction ->
                filterAction = newFilterAction // 상태 업데이트
            },
            items =
                listOf(
                    stringResource(R.string.device_update_sort),
                    stringResource(R.string.device_name_sort),
                    stringResource(R.string.device_size_sort),
                ),
        )
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.Transparent),
        ) {
            Spacer(Modifier.height(32.dp))

            Box(
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(APP_BACKGROUND),
            ) {
                ItemList(data, viewModel, adViewModel)
            }
        }
    }
    FilterAppList(filterAction, viewModel)
    when {
        !isLoaded -> {
            LoadingPlaceholder(loading = true)
        }

        data.isEmpty() -> {
            LoadingPlaceholder(loading = false)
        }

        else -> {}
    }
    if (adLoading?.route == Destination.AppManager.route) {
        InterstitialAdPage(
            adTarget =
                AdTarget(
                    Destination.Home.Main.TopicListDetail.route,
                    adLoading?.adStart ?: true,
                ),
            interstitialAdManager = mainViewModel.interstitialAdManager,
            setAdLoading = {
                it?.let {
                    mainViewModel.setInterstitialAdStart(it.route, it.adStart)
                }
            },
            adInitState = mainViewModel.adMobinitState,
            loading = loading,
            setLoading = {
                loading = it
            },
            itemSize = Integer.MAX_VALUE,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ItemList(
    items: List<AppManagerInfo>?,
    viewModel: AppManagerViewModel,
    adViewModel: AdViewModel,
) {
    items?.let {
        RLog.d(TAG, "size ${items.size}")
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
        ) {
            items(count = items.size) { index ->
                AppListItem(items[index], viewModel)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun AppListItem(
    appManagerInfo: AppManagerInfo?,
    viewModel: AppManagerViewModel,
) {
    val context = LocalContext.current

// 기본 아이콘 (대체 아이콘)
    val placeholderIcon = remember { getDrawable(context, R.drawable.ic_play_icon) }

    val icon =
        remember(appManagerInfo) {
            appManagerInfo?.icon
        }

    val title =
        remember(appManagerInfo) {
            appManagerInfo?.appTitle
        }
    var shouldDeleteApp by remember { mutableStateOf(false) }
    var appDeleteComplete by remember { mutableStateOf(false) }

    // 삭제 후 suspend 함수 실행을 트리거하는 플래그 상태
    var deleteResult by remember { mutableStateOf<String?>(null) }

    val appDeleteLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { _ ->
            appDeleteComplete = true
            shouldDeleteApp = false
        }

    // 패키지 삭제 후 suspend 함수 호출
    if (appDeleteComplete) {
        LaunchedEffect(appDeleteComplete) {
            viewModel.appDeleteUpdataContent(deleteResult)
            deleteResult = null
            appDeleteComplete = false
        }
    }

    if (shouldDeleteApp) {
        LaunchedEffect(shouldDeleteApp) {
            viewModel.appDelete(appDeleteLauncher, appManagerInfo?.packageName ?: "")
        }
    }
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(onClick = {
                        viewModel.goStore(
                            context,
                            URL_GOOGLE_PLAY_APP(
                                appManagerInfo?.packageName ?: URL_MY_PACKAGE_NAME,
                            ),
                            title ?: "",
                        )
                    }),
            // Box 내에서 정렬
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                // Row 내에서 수직 중앙 정렬
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                ) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            icon?.let {
                                Image(
                                    painter = DrawablePainter(it),
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .width(48.dp)
                                            .height(48.dp)
                                            .aspectRatio(1f),
                                )
                            } ?: run {
                                // placeholderIcon?.let{ DrawablePainter(placeholderIcon)}
                                DrawablePainter(placeholderIcon!!)
                            }
                        }
                    }
                }
                Column(Modifier.padding(start = 10.dp)) {
                    Text(
                        title.toString(),
                        // 한 줄로 제한
                        maxLines = 1,
                        // 말줄임표 처리
                        overflow = TextOverflow.Ellipsis,
                        modifier =
                            Modifier
                                .wrapContentSize()
                                .padding(bottom = 5.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Row(Modifier) {
                        Text(
                            text =
                                stringResource(R.string.device_manager_apk_size).format(
                                    appManagerInfo?.apkSize ?: "",
                                ),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = APP_TEXT_COLOR,
                        )

                        Text(
                            text =
                                stringResource(R.string.device_manager_update_time).format(
                                    appManagerInfo?.lastUpdateTime ?: "",
                                ),
                            modifier = Modifier.padding(start = 20.dp),
                            fontSize = 10.sp,
                            color = APP_SUBTITLE_TEXT_COLOR,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f)) // TitleBox와 IconButton 사이에 여백 추가
                DropDownMenuComposable(
                    APP_SUBTITLE_TEXT_COLOR,
                    Icons.Filled.MoreVert,
                    modifer = Modifier,
                ) { menuExpanded, onMenuDismiss ->
                    AppItem(
                        menuExpanded = menuExpanded,
                        appDetail = {
                            viewModel.goDetailAppInfo(
                                context,
                                appManagerInfo?.packageName ?: URL_MY_PACKAGE_NAME,
                            )
                            onMenuDismiss()
                        },
                        onShouldDeleteAppChanged = { newState ->
                            shouldDeleteApp = newState
                            deleteResult = appManagerInfo?.packageName
                            onMenuDismiss()
                        },
                        onMenuDismiss = onMenuDismiss,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun AppItem(
    menuExpanded: Boolean,
    appDetail: () -> Unit,
    onShouldDeleteAppChanged: (Boolean) -> Unit,
    onMenuDismiss: () -> Unit,
) {
    DropdownMenu(
        modifier =
            Modifier
                .wrapContentSize()
                .padding(5.dp),
        expanded = menuExpanded,
        offset = DpOffset(0.dp, 0.dp),
        onDismissRequest = onMenuDismiss,
    ) {
        Column {
            Row(Modifier.clickable(onClick = appDetail)) {
                Text(stringResource(R.string.device_manager_app_detail))
            }
            Row(Modifier.clickable(onClick = { onShouldDeleteAppChanged(true) })) {
                Text(stringResource(R.string.device_manager_app_delete))
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun FilterAppList(
    filterAction: Int,
    viewModel: AppManagerViewModel,
) {
    if (filterAction == -1) return
    // Unit 은 1번만 실행
    LaunchedEffect(filterAction) {
        viewModel.phoneAppListOrder(filterAction)
    }
}
