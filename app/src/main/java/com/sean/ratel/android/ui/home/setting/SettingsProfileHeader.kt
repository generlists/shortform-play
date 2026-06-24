package com.sean.ratel.android.ui.home.setting

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.getAvataUrl
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.player.ui.ThemeMode

@Composable
@Suppress("ktlint:standard:function-naming")
fun SettingsProfileHeader(
    mainViewModel: MainViewModel,
    currentMode: ThemeMode,
    isAdRemoved: Boolean = true,
    name: String?,
    username: String,
    onClick: (() -> Unit)? = null,
) {
    val hasAdRemove = isAdRemoved
    val hasAny = hasAdRemove

    // 카드 그라데이션 배경
    val cardGradient =
        when {
            hasAdRemove -> {
                Brush.linearGradient(listOf(Color(0xFF1A2E2A), Color(0xFF1C1C1E)))
            }

            else -> {
                Brush.linearGradient(listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)))
            }
        }

    // 카드 테두리 색상
    val borderColor =
        when {
            hasAdRemove -> {
                APP_TEXT_COLOR.copy(alpha = 0.2f)
            }

            else -> {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
            }
        }

    val infiniteTransition = rememberInfiniteTransition(label = "avatarBorder")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
            ),
        label = "borderAngle",
    )

    // 아바타 테두리 색상
    val avatarBorderBrush =
        when {
            hasAdRemove -> {
                Brush.sweepGradient(
                    colors =
                        listOf(
                            Color(0xFFABFF43),
                            Color(0xFF6EF07F),
                            Color(0xFFABFF43),
//                            Color(0xFF03FFA3),
//                            Color(0xFF28C9B7),
//                            Color(0xFF03FFA3),
                        ),
                    center = Offset(angle, angle),
                )
            }

            else -> {
                null
            }
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 16.dp, bottom = 16.dp),
        shape = RectangleShape,
        border =
            BorderStroke(
                width = if (hasAny) 1.dp else 1.5.dp,
                color = borderColor,
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (currentMode == ThemeMode.DARK) 0.dp else 4.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (cardGradient != null) {
                            Modifier.background(brush = cardGradient)
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        },
                    ).clickable(onClick != null) { onClick?.invoke() }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 아바타 + 구매 상태 뱃지
                Box(
                    modifier = Modifier.wrapContentSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    // 아바타
                    Box(
                        Modifier
                            .then(if (hasAny) Modifier.size(52.dp) else Modifier.wrapContentSize())
                            .then(
                                if (avatarBorderBrush != null) {
                                    Modifier
                                        .clip(CircleShape)
                                        .drawBehind {
                                            rotate(angle) {
                                                drawCircle(
                                                    brush = avatarBorderBrush,
                                                    radius = size.minDimension / 2,
                                                    style = Stroke(width = 2.dp.toPx()),
                                                )
                                            }
                                        }
                                } else {
                                    Modifier
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        NetworkImage(
                            url = getAvataUrl(name),
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .clip(CircleShape)
                                    .width(46.dp)
                                    .height(46.dp),
                            ContentScale.Fit,
                            R.drawable.ic_play_icon,
                            R.drawable.ic_play_icon,
                            R.drawable.ic_play_icon,
                            imageLoader = mainViewModel.imageLoader,
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                // 이름 + 유저네임
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name ?: "unKnown",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                    Text(
                        text = "@$username",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    )
                }

                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // 구매 상태 뱃지 바 — 구매한 것 있을 때만 표시
            if (hasAny) {
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // 광고 제거 뱃지
                    if (hasAdRemove) {
                        PremiumBadgeChip(
                            icon = Icons.Outlined.Block,
                            text = stringResource(R.string.remove_ad),
                            gradient =
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFABFF43),
                                        Color(0xFF6EF07F),
                                    ),
                                ),
                            textColor = Color(0xFF085041),
                            iconTint = Color(0xFF085041),
                        )
                    }
                }
            }
        }
    }
}

// 뱃지 칩

@Composable
@Suppress("ktlint:standard:function-naming")
private fun PremiumBadgeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    gradient: Brush,
    textColor: Color,
    iconTint: Color,
) {
    Row(
        modifier =
            Modifier
                .clip(CircleShape)
                .background(brush = gradient)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(11.dp),
        )
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}
