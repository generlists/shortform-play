package com.sean.ratel.android.ui.end

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.YouTubeUtils
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.ShortsVideoModel
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.progress.YouTubeLoader
import com.sean.ratel.android.ui.theme.Background_op_10
import com.sean.ratel.android.ui.theme.Background_op_20
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.ui.theme.Red
import com.sean.ratel.android.utils.UIUtil.formatNumberByLocale
import kotlinx.coroutines.launch
import java.util.Locale

@Suppress("ktlint:standard:function-naming")
@Composable
fun EndBottomContents(mainShortsModel: MainShortsModel?) {
    val channelModel = remember { mainShortsModel?.shortsChannelModel }
    val videoModel = remember { mainShortsModel?.shortsVideoModel }

    val channelThumbnail =
        remember(channelModel) {
            channelModel?.channelThumbNail
        }
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {},
    ) {
        Column(
            Modifier
                .background(Background_op_20)
                .align(Alignment.BottomStart)
                .padding(top = 15.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 15.dp),
                // 세로로 중앙 정렬
                verticalAlignment = Alignment.CenterVertically,
                // 가로로 중앙정렬
                horizontalArrangement = Arrangement.Start,
            ) {
                Box(
                    Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                ) {
                    if (LocalInspectionMode.current) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_play_icon),
                            contentDescription = "Preview Image",
                            contentScale = ContentScale.Fit,
                            modifier =
                                Modifier
                                    .wrapContentSize()
                                    .width(24.dp)
                                    .height(24.dp),
                        )
                    } else {
                        channelThumbnail?.let {
                            NetworkImage(
                                url = it,
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .clip(CircleShape)
                                        .width(24.dp)
                                        .height(24.dp),
                            )
                        }
                    }
                }
                Row(Modifier.wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channelModel?.channelTitle ?: "no title",
                        Modifier
                            .wrapContentHeight()
                            .width(220.dp)
                            .alpha(0.9f)
                            .padding(start = 7.dp),
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            disPlayViewCount(viewCount = videoModel?.viewCount ?: "1111110"),
                            Modifier
                                .wrapContentSize()
                                .alpha(0.9f)
                                .padding(end = 25.dp),
                            fontFamily = FontFamily.SansSerif,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
            Box(
                Modifier
                    .width(480.dp)
                    .wrapContentHeight(),
            ) {
                Text(
                    videoModel?.title ?: "",
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 15.dp, top = 10.dp)
                        .background(Color.Transparent),
                    fontFamily = FontFamily.SansSerif,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
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
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun LoadingArea(isLoading: Boolean) {
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            YouTubeLoader(loading = isLoading)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun PlayButton(
    isPlaying: Boolean,
    onPlayChange: (Boolean) -> Unit,
) {
    // asCollectState 는 마지막것만 가져오고 recomposition 되지않으면 호출이 안됨
    if (isPlaying) return

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        IconButton(
            onClick = { onPlayChange(true) },
            modifier =
                Modifier
                    .size(85.dp),
        ) {
            Image(
                // 이미지 리소스
                painter = painterResource(id = R.drawable.play),
                contentDescription = "Play Icon",
                modifier =
                    Modifier
                        .height(85.dp)
                        .width(85.dp),
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun RightContentArea(
    youTubeContentEndViewModel: YouTubeContentEndViewModel?,
    mainShortsModel: MainShortsModel?,
    onSoundChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    val rightMenu = remember { YouTubeEndContentRightMenu.entries.toTypedArray().asList() }
    val videoModel = remember { mainShortsModel?.shortsVideoModel }

    var like by remember { mutableStateOf(false) }
    var disLike by remember { mutableStateOf(false) }
    var sound by remember { mutableStateOf(false) }

    val coroutine = rememberCoroutineScope()

    LaunchedEffect(like || disLike) {
        like = youTubeContentEndViewModel?.getLikeDisLikeVideo("like${mainShortsModel?.shortsVideoModel?.videoId}") ?: false
        disLike = youTubeContentEndViewModel?.getLikeDisLikeVideo("disLike${mainShortsModel?.shortsVideoModel?.videoId}") ?: false
        sound = youTubeContentEndViewModel?.getSoundOff() ?: false
    }

    Box(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(
            Modifier
                .wrapContentSize()
                .padding(end = 10.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            rightMenu.forEach { item ->

                Box(
                    Modifier
                        .width(55.dp)
                        .height(55.dp)
                        .clickable { }
                        .background(Background_op_20, shape = RoundedCornerShape(60.dp)),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    item.title?.let {
                        // title이 있을 경우
                        Column(
                            modifier =
                                Modifier
                                    .clickable {
                                        coroutine.launch {
                                            toggleRightEvent(
                                                context,
                                                youTubeContentEndViewModel,
                                                mainShortsModel,
                                                item,
                                                like,
                                                disLike,
                                                sound,
                                                onChanged = { pLike, pDisLike, pSound ->
                                                    Log.d(
                                                        "KKKKKKKK",
                                                        "pLike : $pLike , pDisLike : $pDisLike pSound : $pSound , sound : $sound",
                                                    )
                                                    like = pLike
                                                    disLike = pDisLike
                                                    sound = pSound
                                                    onSoundChange(sound)
                                                },
                                            )
                                        }
                                    }.fillMaxWidth()
                                    .align(Alignment.Center)
                                    .padding(top = 5.dp, bottom = 3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                painter = setImageView(item, like, disLike, sound),
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .width(24.dp)
                                        .height(24.dp),
                            )

                            Text(
                                text = disPlayRightMenu(videoModel, item, sound) ?: "",
                                modifier = Modifier,
                                fontSize = 10.sp,
                                color = if (LocalInspectionMode.current) Color.Black else Color.White,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.SansSerif,
                            )
                        }
                    }
                }
                Spacer(
                    Modifier
                        .height(10.dp)
                        .wrapContentWidth(),
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun BottomSeekBar(
    modifier: Modifier = Modifier,
    // 0f to 1f
    progress: Float,
    onSeekChanged: (Float) -> Unit,
) {
    val barHeight = 3.dp // SeekBar의 높이
    val backgroundColor = Background_op_10
    val progressColor = Red

    Box(
        modifier =
            modifier
                .fillMaxWidth() // 전체 너비를 차지하게 설정
                .height(1.dp) // 높이는 Thumb에 맞춰 설정
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newProgress = offset.x / size.width
                        onSeekChanged(newProgress.coerceIn(0f, 1f))
                    }
                },
    ) {
        // SeekBar 배경
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val barWidth = size.width
            val barTop = size.height / 2 - barHeight.toPx() / 2

            // 배경 바
            drawRoundRect(
                color = backgroundColor,
                size = Size(barWidth, barHeight.toPx()),
                topLeft = Offset(0f, barTop),
            )

            // 프로그레스 바
            drawRoundRect(
                color = progressColor,
                size = Size(barWidth * progress, barHeight.toPx()),
                topLeft = Offset(0f, barTop),
            )
        }

        // SeekBar의 Thumb (드래그 가능한 동그라미)
//        Canvas(
//            modifier = Modifier
//                .align(Alignment.CenterStart)
//                .offset(x = with(LocalDensity.current) { (progress * (size.width - thumbRadius.toPx() * 2)).toDp() })
//                .size(thumbRadius * 2)
//        ) {
//            drawCircle(color = thumbColor)
//        }
    }
}

@Composable
private fun disPlayViewCount(viewCount: String): String =
    String.format(
        Locale.getDefault(),
        stringResource(R.string.end_video_view_count),
        formatNumberByLocale(viewCount.toLong(), Locale.getDefault()),
    )

@Composable
private fun disPlayRightMenu(
    videoModel: ShortsVideoModel?,
    item: YouTubeEndContentRightMenu,
    sound: Boolean,
): String? {
    val locale = Locale.getDefault()

    return when (item) {
        YouTubeEndContentRightMenu.Like -> {
            val likeCount = videoModel?.likeCount ?: stringResource(R.string.end_lkie)
            formatNumberByLocale(likeCount.toLongOrNull() ?: 0, locale)
        }

        YouTubeEndContentRightMenu.Comment -> {
            val commentCount = videoModel?.commentCount ?: stringResource(R.string.end_comment)
            formatNumberByLocale(commentCount.toLongOrNull() ?: 0, locale)
        }

        YouTubeEndContentRightMenu.DisLike -> stringResource(R.string.end_dislike)
        YouTubeEndContentRightMenu.Share -> stringResource(R.string.end_share)
        YouTubeEndContentRightMenu.Sound ->
            if (sound) {
                stringResource(R.string.end_sound_off)
            } else {
                stringResource(
                    R.string.end_sound_on,
                )
            }
    }
}

private suspend fun toggleRightEvent(
    context: Context,
    youTubeContentEndViewModel: YouTubeContentEndViewModel?,
    mainShortsModel: MainShortsModel?,
    item: YouTubeEndContentRightMenu,
    like: Boolean,
    disLike: Boolean,
    sound: Boolean,
    onChanged: (Boolean, Boolean, Boolean) -> Unit,
) {
    when (item) {
        YouTubeEndContentRightMenu.Like -> {
            val likeKey = "like${mainShortsModel?.shortsVideoModel?.videoId}"
            val disLikeKey = "disLike${mainShortsModel?.shortsVideoModel?.videoId}"
            if (!like) {
                youTubeContentEndViewModel?.likeDisLike(likeKey, mainShortsModel)
                youTubeContentEndViewModel?.canCelLikeDisLike(disLikeKey)
                onChanged(true, !disLike, sound)
            } else {
                youTubeContentEndViewModel?.canCelLikeDisLike(likeKey)
                onChanged(false, disLike, sound)
            }
        }

        YouTubeEndContentRightMenu.DisLike -> {
            val disLikeKey = "disLike${mainShortsModel?.shortsVideoModel?.videoId}"
            val likeKey = "like${mainShortsModel?.shortsVideoModel?.videoId}"

            if (!disLike) {
                youTubeContentEndViewModel?.likeDisLike(disLikeKey, mainShortsModel)
                youTubeContentEndViewModel?.canCelLikeDisLike(likeKey)
                onChanged(!like, true, sound)
            } else {
                youTubeContentEndViewModel?.canCelLikeDisLike(disLikeKey)
                onChanged(like, false, sound)
            }
        }

        YouTubeEndContentRightMenu.Sound -> {
            youTubeContentEndViewModel?.setSoundOff(!sound)
            onChanged(like, disLike, !sound)
        }
        YouTubeEndContentRightMenu.Comment -> {
            mainShortsModel?.shortsVideoModel?.videoId?.let {
                YouTubeUtils.goYouTubeAppByVideoId(
                    context,
                    it,
                )
            }
        }
        YouTubeEndContentRightMenu.Share -> {
            mainShortsModel?.shortsVideoModel?.videoId?.let {
                YouTubeUtils.shareVideo(context, it)
            }
        }
    }
}

@Composable
private fun setImageView(
    item: YouTubeEndContentRightMenu,
    like: Boolean,
    disLike: Boolean,
    sound: Boolean,
): Painter {
    when (item) {
        YouTubeEndContentRightMenu.Like -> {
            return if (like) painterResource(item.selectedResourceId) else painterResource(item.unSelectedResourceId)
        }

        YouTubeEndContentRightMenu.DisLike -> {
            return if (disLike) painterResource(item.selectedResourceId) else painterResource(item.unSelectedResourceId)
        }

        YouTubeEndContentRightMenu.Sound -> {
            return if (sound) painterResource(item.selectedResourceId) else painterResource(item.unSelectedResourceId)
        }

        else -> return painterResource(item.unSelectedResourceId)
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun PlayControllerPreView() {
    RatelappTheme {
        EndBottomContents(null)
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun PlayControllerSeekBarPreView() {
    RatelappTheme {
        BottomSeekBar(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            30f,
            {},
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun PlayControllerRightMenuPreView() {
    RatelappTheme {
        RightContentArea(null, null, {})
    }
}
