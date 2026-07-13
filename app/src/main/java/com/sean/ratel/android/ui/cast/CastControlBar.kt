package com.sean.ratel.android.ui.cast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.R
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.BarBg
import com.sean.ratel.android.ui.theme.BarBorder
import com.sean.ratel.android.ui.theme.IconOff
import com.sean.ratel.android.ui.theme.ProgressBg
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate

@Composable
@Suppress("ktlint:standard:function-naming")
fun CastControlBar(
    currentRoute: String,
    adViewModel: AdViewModel,
    playerManager: YouTubePlayersManager,
    onPlayPause: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSubtitle: () -> Unit,
    onSpeedUp: () -> Unit,
    onSpeedDown: () -> Unit,
) {
    val bottomBarHeight = adViewModel.bottomBarHeight.value

    val castSessionState by playerManager.castEventManager.castSession.collectAsStateWithLifecycle(
        initialValue = CastSessionState.SessionUnKnown,
    )
    val currentVideo by playerManager.currentVideo.collectAsStateWithLifecycle()

    val currentTime: Float? by playerManager.castCurrentTime.collectAsStateWithLifecycle()
    val duration: Float? by playerManager.castDuration.collectAsStateWithLifecycle()
    val isCaptionOnOff by playerManager.captionOnOff.collectAsStateWithLifecycle()
    val mute by playerManager.mute.collectAsStateWithLifecycle(initialValue = false)

    val playbackSpeed by playerManager.currentPlaySpeed.collectAsStateWithLifecycle()
    var deviceName by remember { mutableStateOf<String?>(null) }
    val playButtonEvent by playerManager.castEventManager.playEvents.collectAsStateWithLifecycle(initialValue = CastEvent.Idle)

    val floatDuration = duration ?: 0f
    val floatCurrentTime = currentTime ?: 0f
    var progress by remember { mutableFloatStateOf(0.0f) }
    var showControls by rememberSaveable { mutableStateOf(true) }
    val bottomPadding =
        when (currentRoute) {
            Destination.Home.Main.route, Destination.Home.Main.ShortForm.route, Destination.Setting.route -> {
                bottomBarHeight.dp
            }

            else -> {
                0.dp
            }
        }

    val progressRate =
        if (floatDuration == 0f) {
            0f
        } else {
            (floatCurrentTime / floatDuration).coerceIn(0f, 1f)
        }

    LaunchedEffect(progressRate) {
        progress = progressRate
    }

    deviceName =
        when {
            castSessionState is CastSessionState.SessionStart -> (castSessionState as CastSessionState.SessionStart).castDevice
            else -> ""
        }
    // RLog.e("LLLLLLL", "playButtonEvent : $playButtonEvent")

    AnimatedVisibility(
        visible = currentVideo != null && castSessionState is CastSessionState.SessionStart,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 10.dp, bottom = bottomPadding + 10.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(BarBg.copy(alpha = 0.8f)),
            ) {
                // 썸네일 + 제목 + Cast 상태
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 썸네일
                    NetworkImage(
                        url = currentVideo?.shortsVideoModel?.thumbNail ?: "",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2A2A2A)),
                    )

                    // 제목 + Cast 기기명
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = currentVideo?.shortsVideoModel?.title ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            // Cast 아이콘
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cast_connected),
                                contentDescription = null,
                                tint = APP_TEXT_COLOR,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = String.format(stringResource(R.string.cast_running_cast_device), deviceName),
                                color = APP_TEXT_COLOR,
                                fontSize = 10.sp,
                            )
                        }
                    }
                    val rotation by animateFloatAsState(
                        targetValue = if (showControls) 180f else 0f,
                        label = "",
                    )

                    IconButton(
                        onClick = {
                            showControls = !showControls
                            playerManager.sendGALog(
                                screenName = GASplashAnalytics.SCREEN_NAME[currentRoute] ?: "",
                                eventName = GASplashAnalytics.Event.SELECT_ARROW_CAST_CLICK,
                                actionName = GASplashAnalytics.Action.CLICK,
                                mapOf(),
                            )
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = APP_TEXT_COLOR,
                            modifier = Modifier.rotate(rotation),
                        )
                    }
                }
                AnimatedVisibility(
                    visible = showControls,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth(),
                    ) {
                        // 컨트롤 버튼 행
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .padding(top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            // 이전
                            CastIconButton(
                                enabled = !playerManager.isFirst(),
                                iconRes = R.drawable.ic_skip_previous,
                                contentDescription = null,
                                tint = if (playerManager.isFirst()) IconOff else APP_TEXT_COLOR,
                                onClick = onPrevious,
                            )
                            // 재생/정지
                            CastIconButton(
                                enabled = playButtonEvent is CastEvent.Play || playButtonEvent is CastEvent.Pause,
                                iconRes =
                                    when (playButtonEvent) {
                                        is CastEvent.Idle -> {
                                            R.drawable.ic_pause
                                        }

                                        is CastEvent.Pause -> {
                                            R.drawable.ic_play
                                        }

                                        else -> {
                                            R.drawable.ic_pause
                                        }
                                    },
                                contentDescription = if (playButtonEvent is CastEvent.Pause) "일시정지" else "재생",
                                tint = if (playButtonEvent is CastEvent.Idle) IconOff else APP_TEXT_COLOR,
                                size = 28.dp,
                                onClick = {
                                    onPlayPause(playButtonEvent is CastEvent.Play)
                                },
                            )
                            // 다음
                            CastIconButton(
                                enabled = !playerManager.isLast(),
                                iconRes = R.drawable.ic_skip_next,
                                contentDescription = null,
                                tint = if (playerManager.isLast()) IconOff else APP_TEXT_COLOR,
                                onClick = onNext,
                            )

                            // 구분선
                            Spacer(
                                modifier =
                                    Modifier
                                        .width(1.dp)
                                        .height(20.dp)
                                        .background(BarBorder),
                            )

                            // 자막
                            CastIconButton(
                                enabled = true,
                                iconRes = if (isCaptionOnOff) R.drawable.ic_caption_enabled else R.drawable.ic_caption_disabled,
                                contentDescription = null,
                                tint = APP_TEXT_COLOR,
                                onClick = onToggleSubtitle,
                            )
                            // 소리
                            CastIconButton(
                                enabled = true,
                                iconRes = if (mute == true) R.drawable.ic_volume_off else R.drawable.ic_volume_on,
                                contentDescription = null,
                                tint = APP_TEXT_COLOR,
                                onClick = onToggleMute,
                            )

                            // 구분선
                            Spacer(
                                modifier =
                                    Modifier
                                        .width(1.dp)
                                        .height(20.dp)
                                        .background(BarBorder),
                            )

                            // 배속 (▲ 현재값 ▼)
                            SpeedControl(
                                currentSpeed = playbackSpeed,
                                onSpeedUp = onSpeedUp,
                                onSpeedDown = onSpeedDown,
                            )
                        }
                        // 진행바
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                            color = APP_TEXT_COLOR,
                            trackColor = ProgressBg,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
private fun CastIconButton(
    enabled: Boolean,
    iconRes: Int,
    contentDescription: String?,
    tint: Color,
    size: Dp = 22.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size),
        )
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
private fun SpeedControl(
    currentSpeed: YouTubeStreamPlaybackRate?,
    onSpeedUp: () -> Unit,
    onSpeedDown: () -> Unit,
) {
    val seed = currentSpeed ?: YouTubeStreamPlaybackRate.RATE_1
    val label =
        remember(seed) {
            seed.rate.toString().removeSuffix(".0") + "x"
        }

    val currentIndex =
        remember(seed) {
            YouTubeStreamPlaybackRate.entries.indexOf(seed)
        }

    val canSpeedUp = currentIndex < YouTubeStreamPlaybackRate.entries.lastIndex
    val canSpeedDown = currentIndex > 0

    Column(
        modifier = Modifier.width(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clickable(
                        enabled = canSpeedUp,
                        onClick = onSpeedUp,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onSpeedUp) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Filled.ArrowDropUp,
                    contentDescription = null,
                    tint = if (canSpeedDown) APP_TEXT_COLOR else IconOff.copy(alpha = 0.3f),
                )
            }
        }

        Text(
            text = label,
            color = APP_TEXT_COLOR,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clickable(
                        enabled = canSpeedDown,
                        onClick = onSpeedDown,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onSpeedDown) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = if (canSpeedDown) APP_TEXT_COLOR else IconOff.copy(alpha = 0.3f),
                )
            }
        }
    }
}
