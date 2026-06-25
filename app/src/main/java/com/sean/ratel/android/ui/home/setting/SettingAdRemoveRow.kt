package com.sean.ratel.android.ui.home.setting

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme

@Composable
@Suppress("ktlint:standard:function-naming")
fun SettingAdRemoveRow(
    modifier: Modifier = Modifier,
    promotionTitle: String,
    promotionDes: String,
    isSaleActive: Boolean,
    discountPercent: String?,
    currentPrice: String = "",
    onClick: () -> Unit,
) {
    val backgroundColor = APP_TEXT_COLOR.copy(alpha = 0.08f)
    val iconBackgroundColor = APP_TEXT_COLOR.copy(alpha = 0.08f)
    val rightChipBackgroundColor = APP_TEXT_COLOR.copy(alpha = 0.08f)
    val borderColor = APP_TEXT_COLOR.copy(alpha = 0.4f)
    val contentColor = APP_TEXT_COLOR

    // 펄스 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "adChipPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "adChipScale",
    )

    Box(
        modifier =
            modifier
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp),
                ).background(backgroundColor)
                .clickable { onClick() },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 아이콘 박스
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBackgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Block,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 텍스트
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = promotionTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                )
                if (isSaleActive && currentPrice.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text =
                            if (!discountPercent.isNullOrBlank()) {
                                stringResource(R.string.setting_banner_discount, discountPercent, currentPrice)
                            } else {
                                currentPrice
                            },
                        fontSize = 12.sp,
                        color = contentColor,
                    )
                }

                if (discountPercent == null) {
                    Text(
                        text = promotionDes,
                        fontSize = 14.sp,
                        color = contentColor,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 우측 칩 + 화살표
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isSaleActive && !discountPercent.isNullOrEmpty()) {
                    val infiniteTransition = rememberInfiniteTransition(label = "shine")
                    val shimmerOffset by infiniteTransition.animateFloat(
                        initialValue = -300f,
                        targetValue = 300f,
                        animationSpec =
                            infiniteRepeatable(
                                animation = tween(durationMillis = 1800, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart,
                            ),
                        label = "shimmerOffset",
                    )
                    val shimmerBrush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    rightChipBackgroundColor,
                                    Color.White.copy(alpha = 0.5f),
                                    rightChipBackgroundColor,
                                ),
                            start = Offset(shimmerOffset, 0f),
                            end = Offset(shimmerOffset + 200f, 0f),
                            tileMode = TileMode.Clamp,
                        )
                    Box(
                        modifier =
                            Modifier
                                .scale(scale)
                                .clip(CircleShape)
                                .background(shimmerBrush)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$discountPercent↓",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun HomeTopBarPreview() {
    RatelappTheme {
        SettingAdRemoveRow(Modifier, "광고제거", "광고를 영구 제거하고 오직 나만의 스크랩 영상을 쾌적하게 완성하세요.", true, null, "₩4900", onClick = {
        })
    }
}
