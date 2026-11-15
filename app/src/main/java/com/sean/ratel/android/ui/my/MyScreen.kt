package com.sean.ratel.android.ui.my

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.filter.FilterState
import com.sean.ratel.android.data.filter.TopicPackSet
import com.sean.ratel.android.ui.home.BillingViewModel
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.home.setting.SettingsProfileHeader
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.utils.findActivity
import com.sean.ratel.player.ui.ThemeMode
import java.util.Locale

/** 하단 "마이" 탭 — 로그인, 내 필터 요약, 로그아웃·계정 삭제. */
@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun MyScreen(
    accountViewModel: AccountViewModel,
    mainViewModel: MainViewModel,
    billingViewModel: BillingViewModel,
) {
    val activity = LocalContext.current.findActivity()
    val user by accountViewModel.user.collectAsStateWithLifecycle()
    val state by accountViewModel.filterState.collectAsStateWithLifecycle()
    val packs by accountViewModel.topicPacks.collectAsStateWithLifecycle()
    val busy by accountViewModel.busy.collectAsStateWithLifecycle()
    val hiddenCount by mainViewModel.hiddenCount.collectAsStateWithLifecycle()
    val guestId by accountViewModel.guestId.collectAsStateWithLifecycle()
    val reportViewModel: WatchReportViewModel = hiltViewModel()
    val report by reportViewModel.report.collectAsStateWithLifecycle()
    val limitViewModel: WatchLimitViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val folders by libraryViewModel.folders.collectAsStateWithLifecycle()
    var showNewFolder by remember { mutableStateOf(false) }
    val limitSettings by limitViewModel.settings.collectAsStateWithLifecycle()
    val limitToday by limitViewModel.today.collectAsStateWithLifecycle()
    val isRemoveAd by billingViewModel.isAdRemoved.collectAsStateWithLifecycle()
    val productDetails by billingViewModel.productDetails.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val language = remember { Locale.getDefault().language }

    AccountMessageEffect(accountViewModel)
    BackHandler(enabled = true) {
        mainViewModel.runNavigationBack(route = Destination.Home.Main.route, recreate = false, context = null)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .padding(WindowInsets.statusBars.asPaddingValues()),
    ) {
        Text(
            text = stringResource(R.string.my_title),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 8.dp),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = mainViewModel.bottomBarHeight.value.dp + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 프로필: 로그인 전에는 게스트(설정에 있던 것), 로그인하면 Google 계정. 광고 제거 구매 뱃지는 둘 다 보인다
            item {
                val account = user
                if (account == null) {
                    SettingsProfileHeader(
                        mainViewModel = mainViewModel,
                        currentMode = ThemeMode.DARK,
                        isAdRemoved = isRemoveAd,
                        name = guestId,
                        username = if (!isRemoveAd) "guest" else productDetails?.oneTimePurchaseOfferDetails?.offerId ?: "",
                    )
                } else {
                    SettingsProfileHeader(
                        mainViewModel = mainViewModel,
                        currentMode = ThemeMode.DARK,
                        isAdRemoved = isRemoveAd,
                        // 이름 아래에 항상 이메일. 이름이 없으면 이메일 앞부분을 이름으로 쓴다
                        name = account.displayName ?: account.email?.substringBefore("@") ?: "Google",
                        username = account.email.orEmpty(),
                        avatarUrl = account.photoUrl,
                    )
                }
            }
            if (user == null) {
                item {
                    SignInCard(
                        busy = busy,
                        onSignIn = { accountViewModel.signIn(activity) },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
            item {
                FilterSummaryCard(
                    signedIn = user != null,
                    state = state,
                    packs = packs,
                    language = language,
                    hiddenCount = hiddenCount,
                    weeklyHidden = report.hiddenCount,
                    busy = busy,
                    onSignIn = { accountViewModel.signIn(activity) },
                    onEdit = { mainViewModel.navigator.navigateTo(Destination.FilterEdit.route) },
                    onViewHidden = { mainViewModel.navigator.navigateTo(Destination.HiddenVideos.route) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                WatchReportCard(report, Modifier.padding(horizontal = 20.dp))
            }
            item {
                LibraryFoldersCard(
                    folders = folders,
                    onOpen = { folder ->
                        mainViewModel.navigator.navigateTo(Destination.LibraryFolder.createRoute(listOf(folder.id)))
                    },
                    onNewFolder = { showNewFolder = true },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                RecentWatchRow(
                    onClick = {
                        // 홈의 "최근본 영상" 화살표와 같은 동작
                        mainViewModel.setInterstitialAdStart(Destination.Home.Main.RecentlyWatchMore.route, !isRemoveAd)
                        mainViewModel.goMoreContent(Destination.Home.Main.RecentlyWatchMore.route, ViewType.RecentlyWatch)
                        mainViewModel.setIsHomeVisible(true)
                    },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                WatchLimitCard(
                    settings = limitSettings,
                    today = limitToday,
                    onMode = limitViewModel::setMode,
                    onMinutes = limitViewModel::setMinutes,
                    onVideos = limitViewModel::setVideos,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            if (user != null) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlineActionButton(
                            text = stringResource(R.string.my_logout),
                            contentColor = Color.White,
                            borderColor = Color(0xFF555555),
                            onClick = { accountViewModel.signOut() },
                        )
                        OutlineActionButton(
                            text = stringResource(R.string.my_delete_account),
                            contentColor = Color(0xFFFF6B6B),
                            borderColor = Color(0xFF5A2E2E),
                            onClick = { showDeleteDialog = true },
                        )
                    }
                }
            }
        }
    }

    if (showNewFolder) {
        FolderNameDialog(
            title = stringResource(R.string.library_name_new_title),
            initial = "",
            onConfirm = {
                showNewFolder = false
                libraryViewModel.createFolder(it)
            },
            onDismiss = { showNewFolder = false },
        )
    }
    LibraryMessageEffect(libraryViewModel)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1C1C1E),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFDDDDDD),
            title = { Text(stringResource(R.string.my_delete_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.my_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        accountViewModel.deleteAccount()
                    },
                ) { Text(stringResource(R.string.my_delete_confirm), color = Color(0xFFFF6B6B)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.my_cancel), color = Color.White)
                }
            },
        )
    }
}

/** 게스트일 때만 보이는 로그인 안내. 로그인하면 이 카드는 사라지고 위 프로필이 Google 계정으로 바뀐다. */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun SignInCard(
    busy: Boolean,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.my_signin_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.my_signin_desc), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 13.sp)
        }
        GoogleSignInButton(busy = busy, onClick = onSignIn)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
private fun FilterSummaryCard(
    signedIn: Boolean,
    state: FilterState,
    packs: TopicPackSet,
    language: String,
    hiddenCount: Int,
    weeklyHidden: Int,
    busy: Boolean,
    onSignIn: () -> Unit,
    onEdit: () -> Unit,
    onViewHidden: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = state.filter.hasRules
    SectionCard(modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.my_filter_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            StatusPill(
                text = stringResource(if (active) R.string.my_filter_status_on else R.string.my_filter_status_off),
                active = active,
            )
        }
        if (active) {
            if (state.filter.topics.isNotEmpty()) {
                SummaryRow(R.string.my_filter_topics_label, state.filter.topics.map { packs.nameOf(it, language) })
            }
            if (state.filter.keywords.isNotEmpty()) {
                SummaryRow(R.string.my_filter_keywords_label, state.filter.keywords)
            }
            if (hiddenCount > 0 || state.filter.exceptions.isNotEmpty()) {
                // 시안 B2: "이번 주에 N개를 빼고 보여드렸어요". 기록이 아직 없으면 지금 목록 기준 숫자를 보여 준다
                if (weeklyHidden > 0) {
                    Text(stringResource(R.string.my_filter_hidden_week, weeklyHidden), color = APP_TEXT_COLOR, fontSize = 13.sp)
                } else if (hiddenCount > 0) {
                    Text(stringResource(R.string.my_filter_hidden, hiddenCount), color = APP_TEXT_COLOR, fontSize = 13.sp)
                }
                TextButton(onClick = onViewHidden, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        stringResource(R.string.my_filter_hidden_view) + "  ›",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }
            PrimaryButton(text = stringResource(R.string.my_filter_manage), busy = false, onClick = onEdit)
        } else {
            Text(stringResource(R.string.my_filter_desc_off), color = Color(0xFFC9CBD0), fontSize = 13.sp, lineHeight = 20.sp)
            PrimaryButton(
                text = stringResource(if (signedIn) R.string.my_filter_make else R.string.my_filter_make_signin),
                busy = busy,
                // 로그인하지 않았으면 로그인부터
                onClick = if (signedIn) onEdit else onSignIn,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
private fun SummaryRow(
    @StringRes labelRes: Int,
    values: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(labelRes), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 12.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { InfoChip(it) }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun GoogleSignInButton(
    busy: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !busy,
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF111111),
                disabledContainerColor = Color(0xFFBBBBBB),
            ),
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        if (busy) {
            BusyIndicator()
            Spacer(Modifier.size(10.dp))
            Text(stringResource(R.string.my_signin_busy), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(stringResource(R.string.my_signin_google), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
internal fun PrimaryButton(
    text: String,
    busy: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !busy,
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = APP_TEXT_COLOR,
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF5B7A2A),
            ),
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        if (busy) {
            BusyIndicator()
            Spacer(Modifier.size(10.dp))
        }
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

/** 테두리가 있는 눈에 띄는 버튼(로그아웃, 계정 삭제). 가운데 정렬 글자, 화면 폭만큼 넓다. */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun OutlineActionButton(
    text: String,
    contentColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF121212), contentColor = contentColor),
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
