package com.sean.ratel.android.ui.my

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.filter.ExcludeMatch
import com.sean.ratel.android.data.filter.FilterReason
import com.sean.ratel.android.data.log.GAAccountAnalytics
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import java.util.Locale

/**
 * 지금 필터로 숨겨진 영상과, 사용자가 "빼지 않기"로 둔 영상. 숨겨진 영상은 "빼지 않기"로 살리고,
 * 빼지 않기로 둔 영상은 "다시 빼기"로 되돌린다. 누르면 바로 저장한다(기기 → 서버).
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun HiddenVideosScreen(
    accountViewModel: AccountViewModel,
    mainViewModel: MainViewModel,
) {
    val hidden by mainViewModel.hiddenSamples.collectAsStateWithLifecycle()
    val state by accountViewModel.filterState.collectAsStateWithLifecycle()
    val packs by accountViewModel.topicPacks.collectAsStateWithLifecycle()
    val language = remember { Locale.getDefault().language }
    val exceptionIds = state.filter.exceptions
    val kept = remember(exceptionIds) { mainViewModel.videosById(exceptionIds) }

    AccountMessageEffect(accountViewModel)
    LaunchedEffect(Unit) {
        accountViewModel.track(
            GAAccountAnalytics.Screen.HIDDEN_VIDEOS,
            GAAccountAnalytics.Event.HIDDEN_VIDEOS_VIEW,
            GAAccountAnalytics.Action.VIEW,
        )
    }
    BackHandler(enabled = true) { mainViewModel.runNavigationBack() }

    @Composable
    fun reasonText(match: ExcludeMatch): String =
        when (match.reason) {
            FilterReason.TOPIC -> stringResource(R.string.onb_preview_reason_topic, packs.nameOf(match.detail, language))
            FilterReason.KEYWORD -> stringResource(R.string.onb_preview_reason_keyword, match.detail)
            FilterReason.CHANNEL -> stringResource(R.string.onb_preview_reason_channel)
            FilterReason.DISLIKED -> stringResource(R.string.hidden_reason_disliked)
            FilterReason.WATCHED -> stringResource(R.string.hidden_reason_watched)
        }

    Column(
        Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .padding(WindowInsets.statusBars.asPaddingValues()),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { mainViewModel.runNavigationBack() }, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.filter_edit_back),
                    tint = Color.White,
                )
            }
            Text(stringResource(R.string.hidden_title), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding =
                androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp,
                    top = 8.dp,
                    end = 20.dp,
                    bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.hidden_desc),
                    color = APP_SUBTITLE_TEXT_COLOR,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            if (hidden.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.hidden_empty),
                        color = Color(0xFFC9CBD0),
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            } else {
                item { SectionTitle(stringResource(R.string.hidden_section_hidden, hidden.size)) }
                items(hidden, key = { "h-" + it.videoId }) { sample ->
                    VideoRow(
                        video = sample.item,
                        reason = reasonText(sample.match),
                        kept = false,
                        // 채널 차단·싫어요는 영상 한 개를 살려도 되지만, 주제·키워드와 똑같이 "빼지 않기"로 둔다
                        onClick = { accountViewModel.setException(sample.videoId, keep = true) },
                    )
                }
            }
            if (kept.isNotEmpty()) {
                item { Box(Modifier.height(20.dp)) }
                item { SectionTitle(stringResource(R.string.hidden_section_kept, kept.size)) }
                items(kept, key = { "k-" + it.shortsVideoModel?.videoId }) { video ->
                    VideoRow(
                        video = video,
                        reason = null,
                        kept = true,
                        onClick = { accountViewModel.setException(video.shortsVideoModel?.videoId.orEmpty(), keep = false) },
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun VideoRow(
    video: MainShortsModel,
    reason: String?,
    kept: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NetworkImage(
            url = video.shortsVideoModel?.thumbNail.orEmpty(),
            contentDescription = null,
            modifier = Modifier.width(48.dp).height(72.dp).clip(RoundedCornerShape(8.dp)),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                video.shortsVideoModel?.title.orEmpty(),
                color = if (kept) Color.White else APP_SUBTITLE_TEXT_COLOR,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (kept) null else TextDecoration.LineThrough,
            )
            Text(
                video.shortsChannelModel?.channelTitle.orEmpty(),
                color = Color(0xFF8E9096),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (reason != null) InfoChip(reason)
        }
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, if (kept) APP_TEXT_COLOR else Color(0xFF3A3A3A)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (kept) APP_TEXT_COLOR else Color(0xFFDDDDDD)),
            modifier = Modifier.heightIn(min = 36.dp),
        ) {
            Text(stringResource(if (kept) R.string.onb_preview_hide_again else R.string.onb_preview_keep), fontSize = 12.sp)
        }
    }
}
