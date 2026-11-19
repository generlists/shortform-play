package com.sean.ratel.android.ui.my

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.log.GAAccountAnalytics
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.BarBorder
import com.sean.ratel.android.utils.findActivity
import java.util.Locale

/** 내 필터 만들기·관리 — 주제 고르기, 키워드 입력, 옵션. 저장하면 기기에 저장하고 서버에 올린다(로그인 필요). */
@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun FilterEditScreen(
    accountViewModel: AccountViewModel,
    mainViewModel: MainViewModel,
) {
    val activity = LocalContext.current.findActivity()
    val draft by accountViewModel.draft.collectAsStateWithLifecycle()
    val packs by accountViewModel.topicPacks.collectAsStateWithLifecycle()
    val user by accountViewModel.user.collectAsStateWithLifecycle()
    val busy by accountViewModel.busy.collectAsStateWithLifecycle()
    var keywordInput by remember { mutableStateOf("") }
    val language = remember { Locale.getDefault().language }
    val removeText = stringResource(R.string.filter_edit_remove)
    // 차단한 채널은 id 대신 이름으로(홈 데이터에 나온 채널만 이름을 알 수 있다)
    val channelNames = remember { mainViewModel.channelNames() }

    LaunchedEffect(Unit) {
        accountViewModel.startEdit()
        accountViewModel.track(
            GAAccountAnalytics.Screen.FILTER_EDIT,
            GAAccountAnalytics.Event.FILTER_EDIT_OPEN,
            GAAccountAnalytics.Action.VIEW,
        )
    }
    AccountMessageEffect(accountViewModel) { message ->
        // 저장했으면(서버에 못 올렸어도 기기에는 저장됨) 마이로 돌아간다
        if (message == AccountMessage.FILTER_SAVED || message == AccountMessage.FILTER_SAVE_PENDING) {
            mainViewModel.runNavigationBack()
        }
    }
    BackHandler(enabled = true) { mainViewModel.runNavigationBack() }

    fun addKeyword() {
        accountViewModel.addKeyword(keywordInput)
        keywordInput = ""
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
            Text(
                stringResource(R.string.filter_edit_title),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            item {
                Section(R.string.filter_edit_topics, R.string.filter_edit_topics_desc) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        packs.topics.forEach { topic ->
                            ToggleChip(
                                text = packs.nameOf(topic.id, language),
                                selected = topic.id in draft.topics,
                                onClick = { accountViewModel.toggleTopic(topic.id) },
                            )
                        }
                    }
                }
            }
            item {
                Section(R.string.filter_edit_keywords, R.string.filter_edit_keywords_desc) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = keywordInput,
                            onValueChange = { keywordInput = it },
                            placeholder = { Text(stringResource(R.string.filter_edit_keyword_hint), color = APP_SUBTITLE_TEXT_COLOR) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { addKeyword() }),
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = APP_TEXT_COLOR,
                                    unfocusedBorderColor = Color(0xFF3A3A3A),
                                    cursorColor = APP_TEXT_COLOR,
                                ),
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = { addKeyword() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = APP_TEXT_COLOR, contentColor = Color.Black),
                            modifier = Modifier.heightIn(min = 56.dp),
                        ) { Text(stringResource(R.string.filter_edit_add), fontWeight = FontWeight.Bold) }
                    }
                    Text(
                        stringResource(R.string.filter_keyword_lang_note),
                        color = APP_SUBTITLE_TEXT_COLOR,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        draft.keywords.forEach { keyword ->
                            RemovableChip(keyword, removeText) { accountViewModel.removeKeyword(keyword) }
                        }
                    }
                }
            }
            item {
                Section(R.string.filter_edit_options, null) {
                    OptionRow(R.string.filter_edit_hide_disliked, draft.hideDisliked, accountViewModel::setHideDisliked)
                    OptionRow(R.string.filter_edit_hide_watched, draft.hideWatched, accountViewModel::setHideWatched)
                }
            }
            if (draft.blockedChannels.isNotEmpty()) {
                item {
                    Section(R.string.filter_edit_channels, R.string.filter_edit_channels_desc) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            draft.blockedChannels.forEach { channelId ->
                                RemovableChip(
                                    text = channelNames[channelId] ?: channelId,
                                    removeDescription = removeText,
                                    onRemove = { accountViewModel.removeChannel(channelId) },
                                )
                            }
                        }
                    }
                }
            }
        }

        PrimaryButton(
            text = stringResource(if (user == null) R.string.filter_edit_save_signin else R.string.filter_edit_save),
            busy = busy,
            onClick = { accountViewModel.saveDraft(activity) },
            modifier =
                Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp)
                    .padding(bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun Section(
    titleRes: Int,
    descRes: Int?,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(titleRes), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (descRes != null) {
                Text(stringResource(descRes), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 13.sp, lineHeight = 19.sp)
            }
        }
        content()
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun OptionRow(
    labelRes: Int,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(labelRes), color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = APP_TEXT_COLOR,
                    uncheckedThumbColor = Color(0xFFDDDDDD),
                    uncheckedTrackColor = BarBorder,
                ),
        )
    }
}
