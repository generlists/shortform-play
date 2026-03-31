package com.sean.ratel.android.ui.push

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.URL_GOOGLE_PLAY_APP
import com.sean.ratel.android.data.common.STRINGS.URL_MY_PACKAGE_NAME
import com.sean.ratel.android.data.domain.model.push.AppPushType
import com.sean.ratel.android.data.domain.model.push.PushAppUpdateModel
import com.sean.ratel.android.data.domain.model.push.PushModel
import com.sean.ratel.android.data.domain.model.push.PushRecommendModel
import com.sean.ratel.android.data.domain.model.push.PushUploadModel
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.end.LoadingArea
import com.sean.ratel.android.ui.push.item.PushUiItem
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.utils.PhoneUtil
import kotlinx.coroutines.delay
import so.smartlab.common.ad.admob.data.model.AdMobBannerState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("ktlint:standard:function-naming")
@Composable
fun NotificationScreen(
    modifier: Modifier,
    mainViewModel: MainViewModel,
    pushViewModel: PushViewModel,
) {
    val notificationData by pushViewModel.notificationPushUiList.collectAsState()
    RLog.d("PUSH_TEST", "notificationData size ${notificationData.size}")
    val context = LocalContext.current
    val hasLoadedOnce by pushViewModel.hasLoadedOnce.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
    var showRealImage by remember(hasLoadedOnce) { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .padding(top = insetPaddingValue.calculateTopPadding()),
    ) {
        when {
            !hasLoadedOnce -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingArea(true)
                }
            }

            notificationData.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.notification_no_data), color = Color.White)
                }
            }

            else -> {
                LaunchedEffect(hasLoadedOnce) {
                    showRealImage = false
                    delay(50)
                    showRealImage = true
                }
                if (showRealImage) {
                    Box(modifier.padding(top = 16.dp)) {
                        Box(
                            Modifier
                                .wrapContentSize()
                                .padding(start = 16.dp, end = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(stringResource(R.string.notification_list_description), color = APP_SUBTITLE_TEXT_COLOR)
                        }
                        NotificationList(modifier, notificationData, pushViewModel, mainViewModel)
                    }
                }
            }
        }
        TopNavigationBar(
            titleResourceId = R.string.app_notification,
            historyBack = { mainViewModel.runNavigationBack() },
            isShareButton = true,
            runSetting = { PhoneUtil.shareAppLinkButton(context) },
            filterButton = false,
        )

        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun NotificationList(
    modifier: Modifier,
    notificationData: List<PushUiItem>?,
    pushViewModel: PushViewModel,
    mainViewModel: MainViewModel,
) {
    val adFixedBannerState by mainViewModel.fixedBannerState.collectAsState()
    var adSize by remember { mutableStateOf(64) }
    if (notificationData.isNullOrEmpty()) return

    when {
        adFixedBannerState is AdMobBannerState.AdLoadComplete -> {
            adSize = (adFixedBannerState as AdMobBannerState.AdLoadComplete).adSize.height
        }

        else -> {
            adSize = 0
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = adSize.dp),
    ) {
        Box(
            modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            val context = LocalContext.current
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp),
            ) {
                for (index in 0 until notificationData.size) {
                    // pagingItems[index]를 호출해야 페이징 로딩이 트리거됨
                    when (val uiItem = notificationData[index]) {
                        is PushUiItem.DateHeader -> {
                            stickyHeader(key = "header_${uiItem.date.toEpochDay()}") {
                                ScrapDateHeader(uiItem.date)
                            }
                        }

                        is PushUiItem.Content -> {
                            val realDataIndex =
                                notificationData
                                    .take(index)
                                    .count { it is PushUiItem.Content }

                            item(key = uiItem.push.createAt) {
                                PushListItem(pushViewModel, uiItem.push, onClick = {
                                })
                                Spacer(Modifier.height(10.dp))
                            }
                        }

                        else -> {
                            Unit
                        }
                    }
                }
            }
        }
    }
    if (notificationData.isEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            LoadingArea(true)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ScrapDateHeader(date: LocalDate) {
    val text =
        when (date) {
            LocalDate.now() -> stringResource(R.string.notification_today)
            LocalDate.now().minusDays(1) -> stringResource(R.string.notification_yesterday)
            else -> date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        }

    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
    ) {
        Text(
            text = text,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(APP_BACKGROUND)
                    .padding(10.dp),
            color = APP_TEXT_COLOR,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun PushListItem(
    pushViewModel: PushViewModel,
    item: PushModel,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable {
                    when (item) {
                        is PushAppUpdateModel -> {
                            PhoneUtil.runAppStore(
                                context,
                                URL_GOOGLE_PLAY_APP(URL_MY_PACKAGE_NAME),
                            )
                        }

                        is PushUploadModel -> {
                            val versionCode = PhoneUtil.getAppVersionCode(context)
                            openDeepLink(
                                context,
                                "https://shortform-play.ai/youtube?vid=${item.videoId}&v=$versionCode",
                            )
                        }

                        is PushRecommendModel -> {
                            val versionCode = PhoneUtil.getAppVersionCode(context)
                            openDeepLink(
                                context,
                                "https://shortform-play.ai/youtube?vid=${item.videoId}&v=$versionCode",
                            )
                        }
                    }

                    pushViewModel.updateReadFlag(item.id, isRead = true)
                },
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, end = 10.dp)
                    .wrapContentHeight(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                IconButton(
                    onClick = {
                        pushViewModel.deleteNotification(item)
                    },
                    Modifier
                        .width(24.dp)
                        .height(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 16.dp, bottom = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .widthIn(20.dp)
                            .padding()
                            .weight(0.05f),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!item.isRead) {
                        Box(
                            modifier =
                                Modifier
                                    .padding(start = 5.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(APP_TEXT_COLOR),
                        )
                    }
                }

                Box(
                    modifier =
                        Modifier
                            .weight(0.35f)
                            .wrapContentSize(),
                ) {
                    Card(
                        modifier =
                            Modifier
                                .wrapContentSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Box(
                            modifier = Modifier,
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            when (item) {
                                is PushAppUpdateModel -> {
                                    Image(
                                        painter = painterResource(id = R.drawable.shortform_play_icon_main),
                                        contentDescription = "Preview Image",
                                        contentScale = ContentScale.Fit,
                                        modifier =
                                            Modifier
                                                .size(64.dp)
                                                .aspectRatio(1 / 1f),
                                    )
                                }

                                is PushUploadModel -> {
                                    Box(Modifier, contentAlignment = Alignment.BottomEnd) {
                                        NetworkImage(
                                            url = (item).thumbUrl,
                                            imageLoader = pushViewModel.imageLoader,
                                            contentDescription = null,
                                            modifier =
                                                Modifier
                                                    .width(120.dp)
                                                    .aspectRatio(16 / 9f),
                                            contentScale = ContentScale.Crop,
                                            placeholderRes = R.drawable.thumb_placeholder,
                                        )
                                        Box(
                                            modifier =
                                                Modifier
                                                    .wrapContentSize()
                                                    .background(Color.Transparent, CircleShape),
                                            contentAlignment = Alignment.BottomEnd,
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.shortform_play_icon_main),
                                                contentDescription = null,
                                                contentScale = ContentScale.Fit,
                                                modifier =
                                                    Modifier
                                                        .size(24.dp)
                                                        .align(Alignment.BottomStart),
                                            )
                                        }
                                    }
                                }

                                is PushRecommendModel -> {
                                    NetworkImage(
                                        url = (item).thumbUrl,
                                        imageLoader = pushViewModel.imageLoader,
                                        contentDescription = null,
                                        modifier =
                                            Modifier
                                                .width(120.dp)
                                                .aspectRatio(16 / 9f),
                                        contentScale = ContentScale.Crop,
                                        placeholderRes = R.drawable.thumb_placeholder,
                                    )
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier =
                        Modifier
                            .weight(0.6f)
                            .fillMaxHeight(),
                ) {
                    Row {
                        Box(
                            modifier = Modifier.wrapContentSize(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            PushTypeBadge(item.type)
                        }
                        Text(
                            text = item.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 14.sp,
                        )
                    }

                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = item.body,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = APP_SUBTITLE_TEXT_COLOR,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun PushTypeBadge(pushType: AppPushType) {
    val badge =
        when (pushType) {
            AppPushType.Update -> {
                Pair(stringResource(R.string.notification_update), Color(0xFF1D4ED8))
            }

            AppPushType.Upload -> {
                Pair(stringResource(R.string.notification_upload), Color(0xFF047857))
            }

            AppPushType.Recommend -> {
                Pair(stringResource(R.string.notification_recommend), Color(0xFF7E22CE))
            }
        }

    Box(Modifier) {
        Box(
            modifier =
                Modifier
                    .padding(end = 10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(badge.second)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = badge.first,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

fun openDeepLink(
    context: Context,
    linkUrl: String,
) {
    runCatching {
        val intent =
            Intent(Intent.ACTION_VIEW, linkUrl.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }.onFailure {
        RLog.e("DEEP_LINK", "Failed to open deep link: $linkUrl", it)
    }
}
