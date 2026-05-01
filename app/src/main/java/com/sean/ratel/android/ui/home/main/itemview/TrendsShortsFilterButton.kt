package com.sean.ratel.android.ui.home.main.itemview

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR

@Suppress("ktlint:standard:function-naming")
@Composable
fun TrendsShortsFilterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sweepTransition = rememberInfiniteTransition(label = "shine")
    val sweepOffset by sweepTransition.animateFloat(
        initialValue = -200f,
        targetValue = 400f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 1800,
                        easing = LinearEasing,
                    ),
                repeatMode = RepeatMode.Restart,
            ),
        label = "shineOffset",
    )

    Button(
        onClick = onClick,
        modifier =
            modifier
                .height(56.dp)
                .width(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = APP_TEXT_COLOR,
                contentColor = Color.Black,
            ),
        elevation = ButtonDefaults.buttonElevation(4.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            // 상단 하이라이트 (입체감)
            Box(
                modifier =
                    Modifier
                        .wrapContentWidth()
                        .height(18.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Gray.copy(alpha = 0.35f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
            )

            // 좌 → 우 빛나는 스윕
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(40.dp)
                        .offset { IntOffset(sweepOffset.toInt(), 0) }
                        .background(
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.35f),
                                        Color.Transparent,
                                    ),
                            ),
                        ),
            )

            // 텍스트
            Text(
                text = "\uD83D\uDD25 오늘의 트랜드는?",
                modifier = Modifier.align(Alignment.Center),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black,
                style =
                    TextStyle(
                        shadow =
                            Shadow(
                                color = Color.Black,
                                // 그림자의 위치 (x, y)
                                offset = Offset(2f, 2f),
                                // 그림자의 흐림 정도
                                blurRadius = 4f,
                            ),
                    ),
            )
        }
    }
}

// Box(
// modifier =
// Modifier
// .clip(RoundedCornerShape(20.dp))
// .background(
// if (selectedFilter == index) APP_TEXT_COLOR else APP_FILTER_BACKGROUND,
// ).clickable {
//    selectedFilter = index
//
//    val filterType =
//        when (selectedFilter) {
//            0 -> TopicFilterType.Popular
//            1 -> TopicFilterType.Views
//            2 -> TopicFilterType.Subscriber
//            else -> TopicFilterType.Popular
//        }
//
//    mainViewModel.sendGALog(
//        screenName =
//            GASplashAnalytics.SCREEN_NAME.get(
//                TOPIC_DETAIL,
//            ) ?: "",
//        eventName = GASplashAnalytics.Event.SELECT_TOPIC_DETAIL_FILTER_ITEM_CLICK,
//        actionName = GASplashAnalytics.Action.CLICK,
//        mapOf(
//            "topicId" to topicKey,
//            "filterType" to filterType.name,
//        ),
//    )
// }.padding(horizontal = 16.dp, vertical = 6.dp),
// ) {
//    Text(
//        text = filter,
//        fontSize = 12.sp,
//        fontWeight = if (selectedFilter == index) FontWeight.Bold else FontWeight.Normal,
//        color = if (selectedFilter == index) APP_BACKGROUND else APP_FILTER_DIABLE_COLOR,
//    )
// }
