package com.sean.ratel.android.ui.end

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.filter.FilterLanguages
import com.sean.ratel.android.data.filter.KeywordSuggester
import com.sean.ratel.android.data.filter.TopicSuggester
import com.sean.ratel.android.ui.my.AccountMessageEffect
import com.sean.ratel.android.ui.my.AccountViewModel
import com.sean.ratel.android.ui.my.PrimaryButton
import com.sean.ratel.android.ui.my.ToggleChip
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.utils.findActivity
import java.util.Locale

/**
 * 재생 화면의 "관심없음" 시트 — 이 영상의 채널, 걸리는 주제, 제목의 단어 중에서 골라 내 필터에 더한다.
 * 로그인하지 않았으면 "로그인하고 추가"로 먼저 로그인한다.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun NotInterestedSheet(
    item: MainShortsModel?,
    onApplied: () -> Unit,
    onDismiss: () -> Unit,
    // 재생 화면의 "싫어요"는 레일에서 빠지고 이 시트 맨 아래로 들어왔다(null 이면 숨긴다)
    disliked: Boolean = false,
    onToggleDislike: (() -> Unit)? = null,
) {
    val activity = LocalContext.current.findActivity() as? ComponentActivity ?: return
    // 마이 탭·필터 편집과 같은 상태를 쓴다(Activity 범위)
    val accountViewModel = remember(activity) { ViewModelProvider(activity)[AccountViewModel::class.java] }
    val user by accountViewModel.user.collectAsStateWithLifecycle()
    val packs by accountViewModel.topicPacks.collectAsStateWithLifecycle()
    val country by accountViewModel.country.collectAsStateWithLifecycle()
    val busy by accountViewModel.busy.collectAsStateWithLifecycle()
    val language = remember { Locale.getDefault().language }

    val channelId = item?.shortsChannelModel?.channelId?.takeIf { it.isNotBlank() }
    val channelTitle = item?.shortsChannelModel?.channelTitle.orEmpty()
    val topicIds =
        remember(item, packs, country) { item?.let { TopicSuggester.suggest(it, packs, FilterLanguages.forCountry(country)) }.orEmpty() }
    val words = remember(item) { KeywordSuggester.fromTitle(item?.shortsVideoModel?.title) }

    var blockChannel by remember { mutableStateOf(false) }
    var pickedTopics by remember { mutableStateOf(setOf<String>()) }
    var pickedWords by remember { mutableStateOf(setOf<String>()) }
    val anyPicked = blockChannel || pickedTopics.isNotEmpty() || pickedWords.isNotEmpty()

    AccountMessageEffect(accountViewModel)

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = APP_BACKGROUND,
        contentColor = Color.White,
        onDismissRequest = onDismiss,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.not_interested_title), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.not_interested_hint), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 13.sp)
            }

            if (channelId != null) {
                Section(R.string.not_interested_channel) {
                    ToggleChip(
                        text = channelTitle.ifBlank { channelId },
                        selected = blockChannel,
                        onClick = { blockChannel = !blockChannel },
                    )
                }
            }
            if (topicIds.isNotEmpty()) {
                Section(R.string.not_interested_topics) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        topicIds.forEach { id ->
                            ToggleChip(
                                text = packs.nameOf(id, language),
                                selected = id in pickedTopics,
                                onClick = { pickedTopics = pickedTopics.toggled(id) },
                            )
                        }
                    }
                }
            }
            if (words.isNotEmpty()) {
                Section(R.string.not_interested_keywords) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        words.forEach { word ->
                            ToggleChip(text = word, selected = word in pickedWords, onClick = { pickedWords = pickedWords.toggled(word) })
                        }
                    }
                }
            }

            PrimaryButton(
                text = stringResource(if (user == null) R.string.not_interested_apply_signin else R.string.not_interested_apply),
                busy = busy,
                enabled = anyPicked,
                onClick = {
                    accountViewModel.addToFilter(
                        activity = activity,
                        channelId = channelId.takeIf { blockChannel },
                        topics = pickedTopics.toList(),
                        keywords = pickedWords.toList(),
                    ) {
                        onApplied()
                        onDismiss()
                    }
                },
            )
            if (onToggleDislike != null) {
                Text(
                    text = stringResource(if (disliked) R.string.not_interested_undislike else R.string.not_interested_dislike),
                    color = APP_SUBTITLE_TEXT_COLOR,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onToggleDislike()
                                onDismiss()
                            }.padding(vertical = 8.dp),
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun Section(
    titleRes: Int,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(titleRes), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        content()
    }
}

private fun <T> Set<T>.toggled(value: T): Set<T> = if (value in this) this - value else this + value
