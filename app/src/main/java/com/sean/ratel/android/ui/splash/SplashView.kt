package com.sean.ratel.android.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.utils.PhoneUtil.StatusBarHeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BACK_CARD = Color(0xFF76767C)
private val TRACK = Color(0xFF2A2A2A)
private val EASE_OUT = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/** 단계별 진행률. 마지막(INIT: 홈 데이터·광고 준비)이 가장 오래 걸려서 그 안에서는 천천히 차오르게 한다. */
private fun SplashStep.progress(): Float =
    when (this) {
        SplashStep.NETWORK -> 0.1f
        SplashStep.SERVER_MAINTAIN -> 0.25f
        SplashStep.NOTIFICATION -> 0.4f
        SplashStep.AUTH -> 0.55f
        SplashStep.INIT -> 0.7f
        SplashStep.DONE -> 1f
    }

/**
 * 스플래시 화면(시안 1 "위로 넘기기", ai_doc/client/ui/splash/splash_options.html).
 * 뒤 화면이 먼저 보이고 → 라임 화면이 아래에서 올라와 자리 잡고 → 재생 버튼이 톡 → 앱 이름·문구·진행 막대.
 * 문구는 5개 중 하나를 실행마다 무작위로 보여 준다.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
internal fun SplashView(step: SplashStep) {
    val statusBarPadding = StatusBarHeight()
    val taglines = stringArrayResource(R.array.splash_taglines)
    val taglineIndex by rememberSaveable { mutableFloatStateOf(taglines.indices.random().toFloat()) }

    val back = remember { Animatable(0f) }
    val front = remember { Animatable(0f) }
    val play = remember { Animatable(0f) }
    val text = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { back.animateTo(1f, tween(550, easing = EASE_OUT)) }
        launch {
            delay(240)
            front.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow))
        }
        launch {
            delay(900)
            play.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
        }
        launch {
            delay(1100)
            text.animateTo(1f, tween(500, easing = EASE_OUT))
        }
    }

    // 진행 막대: 단계가 바뀔 때 부드럽게 따라가고, INIT 에서는 0.7 → 0.95 까지 천천히 차오른다
    var creep by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(step) {
        creep = 0f
        if (step == SplashStep.INIT) {
            val c = Animatable(0f)
            c.animateTo(1f, tween(6000, easing = LinearEasing)) { creep = value }
        }
    }
    val target = if (step == SplashStep.INIT) 0.7f + 0.25f * creep else step.progress()
    val progress by animateFloatAsState(target, tween(400, easing = EASE_OUT), label = "splashProgress")

    Box(
        Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .offset(y = -statusBarPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SplashLogo(back.value, front.value, play.value)
            Spacer(Modifier.height(20.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier.graphicsLayer {
                        alpha = text.value
                        translationY = (1f - text.value) * 6.dp.toPx()
                    },
            ) {
                // 앱 이름: 흰 "숏폼" + 라임 "플레이" (헤더와 같은 배색). 끝 공백은 리소스에서 잘리므로 간격은 Spacer 로 준다
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.splash_app_name_short),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        stringResource(R.string.splash_app_name_play),
                        color = APP_TEXT_COLOR,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = taglines[taglineIndex.toInt().coerceIn(taglines.indices)],
                    color = APP_SUBTITLE_TEXT_COLOR,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }
            Spacer(Modifier.height(28.dp))
            // 진행 막대
            Box(
                Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TRACK),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .background(APP_TEXT_COLOR),
                )
            }
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Image(
                painterResource(R.drawable.ic_smartlab_logo),
                contentDescription = null,
                modifier = Modifier.size(22.dp).clip(RoundedCornerShape(5.dp)),
            )
            Text("SmartLab", color = Color(0xFFD0D0D4), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** 새 앱 아이콘(2a)을 그대로 그린 로고. 각 값은 0→1 진행도. */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun SplashLogo(
    back: Float,
    front: Float,
    play: Float,
) {
    Box(Modifier.size(width = 84.dp, height = 96.dp)) {
        // 뒤 화면: 아래에서 살짝 올라오며 나타남
        Box(
            Modifier
                .offset(x = 32.dp)
                .size(width = 46.dp, height = 78.dp)
                .graphicsLayer {
                    alpha = back
                    translationY = (1f - back) * 60.dp.toPx()
                }.clip(RoundedCornerShape(10.dp))
                .background(BACK_CARD),
        )
        // 앞 화면: 아래에서 올라와 튕기며 자리 잡음(검은 테두리 = 뒤 화면과의 틈)
        Box(
            Modifier
                .offset(x = 6.dp, y = 8.dp)
                .size(width = 56.dp, height = 88.dp)
                .graphicsLayer {
                    alpha = front.coerceIn(0f, 1f)
                    translationY = (1f - front) * 140.dp.toPx()
                }.clip(RoundedCornerShape(16.dp))
                .background(APP_BACKGROUND)
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(APP_TEXT_COLOR),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = play
                        scaleY = play
                    },
            ) {
                val w = size.width
                val h = size.height
                val path =
                    Path().apply {
                        moveTo(w * 0.25f, 0f)
                        lineTo(w, h / 2f)
                        lineTo(w * 0.25f, h)
                        close()
                    }
                drawPath(path, APP_BACKGROUND)
            }
        }
    }
}
