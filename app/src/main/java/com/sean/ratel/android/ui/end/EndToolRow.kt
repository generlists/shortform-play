package com.sean.ratel.android.ui.end

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.log.GAAccountAnalytics
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.utils.findActivity
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private val CHIP_BG = Color(0xB3000000)
private val CHIP_BORDER = Color(0x33FFFFFF)

/**
 * 재생 화면 왼쪽 아래 한 줄: [관심없음(눈에 띄게)] ……… [소리] [속도] [자막].
 * 오른쪽 레일에서 빠진 관심없음·소리·속도·자막을 여기서 다룬다(시안 B, ai_doc/client/ui/player).
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun EndToolRow(
    youTubeContentEndViewModel: YouTubeContentEndViewModel,
    mainShortsModel: MainShortsModel?,
    availableCaption: Boolean,
    onSoundChange: (Boolean) -> Unit,
    speedChange: (YouTubeStreamPlaybackRate) -> Unit,
    enableCaption: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 페이지 넘김(ViewPager2)은 Activity 범위의 MainViewModel 에 등록되어 있다(fragment 범위 hiltViewModel() 은 다른 인스턴스)
    val activity = LocalContext.current.findActivity() as? ComponentActivity
    val mainViewModel = remember(activity) { activity?.let { ViewModelProvider(it)[MainViewModel::class.java] } }
    val videoId = mainShortsModel?.shortsVideoModel?.videoId
    val coroutine = rememberCoroutineScope()

    // 플레이어 도구 사용을 GA 로 보낸다(값은 켬/끔·속도만, 영상 내용은 보내지 않는다)
    fun track(
        event: String,
        value: String? = null,
    ) = mainViewModel?.sendGALog(
        GAAccountAnalytics.Screen.PLAYER,
        event,
        GAAccountAnalytics.Action.CLICK,
        value?.let { mapOf(GAAccountAnalytics.Param.VALUE to it) } ?: emptyMap(),
    )
    val pager by (mainViewModel?.viewPager2 ?: flowOf(null)).collectAsStateWithLifecycle(null)
    val currentRate by youTubeContentEndViewModel.currentPlaySpeed.collectAsStateWithLifecycle()

    // sound: true = 소리 켜짐 (아이콘 ic_volume_on). 저장된 값은 영상이 바뀌어도 유지된다
    var sound by remember { mutableStateOf(false) }
    var caption by remember(availableCaption) { mutableStateOf(availableCaption) }
    var disliked by remember(videoId) { mutableStateOf(false) }
    var showSpeed by remember { mutableStateOf(false) }
    var showNotInterested by remember { mutableStateOf(false) }

    LaunchedEffect(videoId) {
        sound = youTubeContentEndViewModel.getSoundOff() ?: false
        disliked = youTubeContentEndViewModel.getLikeDisLikeVideo("disLike$videoId") ?: false
    }

    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 관심없음: 이 앱의 핵심 동작이라 라임 테두리로 눈에 띄게
        Row(
            Modifier
                .heightIn(min = 36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CHIP_BG)
                .border(BorderStroke(1.5.dp, APP_TEXT_COLOR), RoundedCornerShape(18.dp))
                .clickable {
                    track(GAAccountAnalytics.Event.NOT_INTERESTED_OPEN)
                    showNotInterested = true
                }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_not_interested),
                contentDescription = null,
                tint = APP_TEXT_COLOR,
                modifier = Modifier.size(18.dp),
            )
            Text(stringResource(R.string.end_not_interested), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        ToolChip(
            icon = painterResource(if (sound) R.drawable.ic_volume_on else R.drawable.ic_volume_off),
            label = stringResource(if (sound) R.string.end_chip_sound_on else R.string.end_chip_sound_off),
            onClick = {
                coroutine.launch {
                    youTubeContentEndViewModel.setSoundOff(!sound)
                    sound = !sound
                    track(GAAccountAnalytics.Event.PLAYER_SOUND_TOGGLE, if (sound) "on" else "off")
                    onSoundChange(sound)
                }
            },
        )
        ToolChip(
            icon = painterResource(R.drawable.ic_speed),
            label = "${currentRate.rate}x",
            onClick = { showSpeed = true },
        )
        ToolChip(
            icon = painterResource(if (caption && availableCaption) R.drawable.ic_caption_enabled else R.drawable.ic_caption_disabled),
            label = stringResource(R.string.end_chip_caption),
            enabled = availableCaption,
            onClick = {
                caption = !caption
                track(GAAccountAnalytics.Event.PLAYER_CAPTION_TOGGLE, if (caption) "on" else "off")
                enableCaption(caption)
            },
        )
    }

    if (showSpeed) {
        PlaybackRateBottomSheet(
            currentRate,
            onRateSelected = {
                track(GAAccountAnalytics.Event.PLAYER_SPEED_CHANGE, it.rate.toString())
                speedChange(it)
            },
            onDismiss = { showSpeed = false },
        )
    }
    if (showNotInterested) {
        NotInterestedSheet(
            item = mainShortsModel,
            onApplied = { pager.goNext() },
            onDismiss = { showNotInterested = false },
            disliked = disliked,
            onToggleDislike = {
                val dislikeKey = "disLike$videoId"
                val likeKey = "like$videoId"
                coroutine.launch {
                    if (disliked) {
                        youTubeContentEndViewModel.canCelLikeDisLike(dislikeKey)
                        track(GAAccountAnalytics.Event.VIDEO_DISLIKE_CANCEL)
                        disliked = false
                    } else {
                        youTubeContentEndViewModel.likeDisLike(dislikeKey, mainShortsModel)
                        youTubeContentEndViewModel.canCelLikeDisLike(likeKey)
                        track(GAAccountAnalytics.Event.VIDEO_DISLIKE)
                        disliked = true
                        // 싫어요를 눌렀다 = 이 영상은 그만 보고 다음으로
                        pager.goNext()
                    }
                }
            },
        )
    }
}

private fun androidx.viewpager2.widget.ViewPager2?.goNext() {
    val p = this ?: return
    val next = p.currentItem + 1
    if (next < (p.adapter?.itemCount ?: 0)) p.setCurrentItem(next, true)
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ToolChip(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        Modifier
            .alpha(if (enabled) 1f else 0.4f)
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CHIP_BG)
            .border(BorderStroke(1.dp, CHIP_BORDER), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
