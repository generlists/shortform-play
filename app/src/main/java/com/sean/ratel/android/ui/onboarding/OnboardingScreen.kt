package com.sean.ratel.android.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.filter.FilterPreview
import com.sean.ratel.android.data.filter.FilterReason
import com.sean.ratel.android.data.filter.HiddenSample
import com.sean.ratel.android.data.filter.KeywordSuggester
import com.sean.ratel.android.data.filter.allVideos
import com.sean.ratel.android.data.filter.preview
import com.sean.ratel.android.data.log.GAAccountAnalytics
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.my.AccountMessage
import com.sean.ratel.android.ui.my.AccountMessageEffect
import com.sean.ratel.android.ui.my.AccountViewModel
import com.sean.ratel.android.ui.my.PrimaryButton
import com.sean.ratel.android.ui.my.RemovableChip
import com.sean.ratel.android.ui.my.ToggleChip
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.BarBg
import com.sean.ratel.android.ui.theme.BarBorder
import com.sean.ratel.android.utils.findActivity
import java.util.Locale

private const val STEP_INTRO = 0
private const val STEP_TOPICS = 1
private const val STEP_KEYWORDS = 2
private const val STEP_PREVIEW = 3
private const val STEP_COUNT = 4
private const val MAX_PREVIEW_SAMPLES = 6

private val CARD_SHAPE = RoundedCornerShape(20.dp)
private val PILL_SHAPE = RoundedCornerShape(50)
private val HIDDEN_TAG_FG = Color(0xFFFFB86B)
private val HIDDEN_TAG_BG = Color(0xFF2A1F10)
private val KEPT_TAG_BG = Color(0xFF16200A)

/**
 * 처음 실행 때 한 번 보여주는 필터 온보딩: 소개 → 주제 → 키워드 → 미리보기(→ 로그인 시트).
 * 어디서 나가든 "봤다"고 기록하고 홈으로 간다. 필터는 저장할 때만 만들어진다(마이의 필터 편집과 같은 초안을 쓴다).
 */
@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun OnboardingScreen(
    accountViewModel: AccountViewModel,
    mainViewModel: MainViewModel,
) {
    val activity = LocalContext.current.findActivity()
    var step by rememberSaveable { mutableIntStateOf(STEP_INTRO) }
    var showLogin by rememberSaveable { mutableStateOf(false) }
    val draft by accountViewModel.draft.collectAsStateWithLifecycle()
    val packs by accountViewModel.topicPacks.collectAsStateWithLifecycle()
    val user by accountViewModel.user.collectAsStateWithLifecycle()
    val busy by accountViewModel.busy.collectAsStateWithLifecycle()
    val country by accountViewModel.country.collectAsStateWithLifecycle()
    val language = remember { Locale.getDefault().language }
    val raw = remember { mainViewModel.rawMain() }

    fun finish() {
        accountViewModel.markOnboardingSeen()
        mainViewModel.goHomeFromOnboarding()
    }

    val screen = GAAccountAnalytics.Screen.ONBOARDING

    fun track(
        event: String,
        params: Map<String, Any> = emptyMap(),
        action: String = GAAccountAnalytics.Action.CLICK,
    ) = accountViewModel.track(screen, event, action, params)

    LaunchedEffect(Unit) {
        accountViewModel.startEdit()
        track(GAAccountAnalytics.Event.ONBOARDING_VIEW, action = GAAccountAnalytics.Action.VIEW)
    }
    // 단계가 바뀔 때마다 한 번(처음 소개 화면 포함) — 어느 단계에서 나가는지 본다
    LaunchedEffect(step) {
        track(GAAccountAnalytics.Event.ONBOARDING_STEP, mapOf(GAAccountAnalytics.Param.STEP to step), GAAccountAnalytics.Action.VIEW)
    }
    AccountMessageEffect(accountViewModel) { message ->
        // 저장됐으면(서버에 못 올렸어도 기기에는 저장됨) 홈으로
        if (message == AccountMessage.FILTER_SAVED || message == AccountMessage.FILTER_SAVE_PENDING) {
            track(
                GAAccountAnalytics.Event.ONBOARDING_COMPLETE,
                mapOf(
                    GAAccountAnalytics.Param.TOPIC_COUNT to draft.topics.size,
                    GAAccountAnalytics.Param.KEYWORD_COUNT to draft.keywords.size,
                ),
            )
            finish()
        }
    }
    BackHandler(enabled = true) { if (step > STEP_INTRO) step-- else finish() }

    Column(
        Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 24.dp),
    ) {
        StepHeader(
            step = step,
            onBack = if (step > STEP_INTRO) ({ step-- }) else null,
            onSkip =
                when (step) {
                    STEP_INTRO ->
                        (
                            {
                                track(GAAccountAnalytics.Event.ONBOARDING_SKIP, mapOf(GAAccountAnalytics.Param.STEP to step))
                                finish()
                            }
                        )
                    STEP_TOPICS, STEP_KEYWORDS ->
                        (
                            {
                                track(GAAccountAnalytics.Event.ONBOARDING_SKIP, mapOf(GAAccountAnalytics.Param.STEP to step))
                                step++
                            }
                        )
                    else -> null
                },
        )

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            when (step) {
                STEP_INTRO -> IntroStep()
                STEP_TOPICS ->
                    TopicsStep(
                        topics = packs.topics.map { it.id to packs.nameOf(it.id, language) },
                        selected = draft.topics,
                        countryName = countryDisplayName(country),
                        onToggle = accountViewModel::toggleTopic,
                    )
                STEP_KEYWORDS -> {
                    val suggestions =
                        remember(draft.topics, draft.keywords, packs, country) {
                            val titles = raw.allVideos().map { it.shortsVideoModel?.title }
                            KeywordSuggester.frequent(titles, exclude = draft.keywords)
                        }
                    KeywordsStep(
                        keywords = draft.keywords,
                        suggestions = suggestions,
                        onAdd = accountViewModel::addKeyword,
                        onRemove = accountViewModel::removeKeyword,
                    )
                }
                else -> {
                    // 예외를 뺀 초안으로 계산해야 "빼지 않기"를 누른 영상도 목록에 남아 되돌릴 수 있다
                    val preview =
                        remember(draft.topics, draft.keywords, packs, country) {
                            accountViewModel.compileDraft(draft.copy(exceptions = emptyList())).preview(raw)
                        }
                    PreviewStep(
                        preview = preview,
                        exceptions = draft.exceptions,
                        reasonText = { match ->
                            when (match.reason) {
                                FilterReason.TOPIC ->
                                    stringResource(R.string.onb_preview_reason_topic, packs.nameOf(match.detail, language))
                                FilterReason.KEYWORD -> stringResource(R.string.onb_preview_reason_keyword, match.detail)
                                else -> stringResource(R.string.onb_preview_reason_channel)
                            }
                        },
                        onToggleException = accountViewModel::toggleException,
                    )
                }
            }
        }

        StepFooter(
            step = step,
            selectedTopics = draft.topics.size,
            hasRules = draft.hasRules,
            busy = busy,
            onNext = { step++ },
            onLater = {
                track(
                    GAAccountAnalytics.Event.ONBOARDING_SKIP,
                    mapOf(GAAccountAnalytics.Param.STEP to step, GAAccountAnalytics.Param.REASON to "later"),
                )
                finish()
            },
            onSave = {
                when {
                    !draft.hasRules -> {
                        track(
                            GAAccountAnalytics.Event.ONBOARDING_SKIP,
                            mapOf(GAAccountAnalytics.Param.STEP to step, GAAccountAnalytics.Param.REASON to "no_rules"),
                        )
                        finish()
                    }
                    user == null -> showLogin = true
                    else -> accountViewModel.saveDraft(activity, "onboarding")
                }
            },
        )
    }

    if (showLogin) {
        LoginSheet(
            busy = busy,
            onGoogle = { accountViewModel.saveDraft(activity, "onboarding") },
            onWithout = {
                showLogin = false
                track(
                    GAAccountAnalytics.Event.ONBOARDING_SKIP,
                    mapOf(GAAccountAnalytics.Param.STEP to step, GAAccountAnalytics.Param.REASON to "without_login"),
                )
                finish()
            },
            onDismiss = { if (!busy) showLogin = false },
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun StepHeader(
    step: Int,
    onBack: (() -> Unit)?,
    onSkip: (() -> Unit)?,
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp).padding(end = 4.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.onb_back), tint = Color.White)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(STEP_COUNT) { index ->
                Box(
                    Modifier
                        .width(24.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index <= step) APP_TEXT_COLOR else Color(0xFF3A3A3A)),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (onSkip != null) {
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.onb_skip), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 14.sp)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun StepFooter(
    step: Int,
    selectedTopics: Int,
    hasRules: Boolean,
    busy: Boolean,
    onNext: () -> Unit,
    onLater: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .padding(bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when (step) {
            STEP_INTRO -> {
                PrimaryButton(text = stringResource(R.string.onb_intro_start), busy = false, onClick = onNext)
                TextButton(onClick = onLater, modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)) {
                    Text(stringResource(R.string.onb_intro_later), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 14.sp)
                }
            }
            STEP_TOPICS ->
                PrimaryButton(
                    text =
                        if (selectedTopics > 0) {
                            stringResource(R.string.onb_topics_next_count, selectedTopics)
                        } else {
                            stringResource(R.string.onb_topics_next)
                        },
                    busy = false,
                    onClick = onNext,
                )
            STEP_KEYWORDS -> PrimaryButton(text = stringResource(R.string.onb_keywords_next), busy = false, onClick = onNext)
            else ->
                PrimaryButton(
                    text = stringResource(if (hasRules) R.string.onb_preview_save else R.string.onb_preview_start_without),
                    busy = busy,
                    onClick = onSave,
                )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun Title(
    titleRes: Int,
    descRes: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(titleRes), color = Color.White, fontSize = 26.sp, lineHeight = 35.sp, fontWeight = FontWeight.Black)
        Text(stringResource(descRes), color = Color(0xFFC9CBD0), fontSize = 14.sp, lineHeight = 22.sp)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun IntroStep() {
    Title(R.string.onb_intro_title, R.string.onb_intro_desc)

    // 어떤 느낌인지 보여주는 예시(실제 영상이 아니다)
    val hidden = stringResource(R.string.onb_intro_example_hidden)
    val kept = stringResource(R.string.onb_intro_example_kept)
    val rows =
        listOf(
            Triple(R.string.onb_intro_example_1, Color(0xFF3B2F2A), true),
            Triple(R.string.onb_intro_example_2, Color(0xFF26343B), false),
            Triple(R.string.onb_intro_example_3, Color(0xFF33283A), true),
        )
    Column(
        Modifier
            .fillMaxWidth()
            .clip(CARD_SHAPE)
            .background(BarBg)
            .border(BorderStroke(1.dp, BarBorder), CARD_SHAPE)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rows.forEach { (textRes, color, isHidden) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.width(44.dp).height(64.dp).clip(RoundedCornerShape(8.dp)).background(color))
                Text(
                    stringResource(textRes),
                    color = if (isHidden) APP_SUBTITLE_TEXT_COLOR else Color.White,
                    fontSize = 14.sp,
                    textDecoration = if (isHidden) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Tag(text = if (isHidden) hidden else kept, hidden = isHidden)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        listOf(R.string.onb_intro_point_1, R.string.onb_intro_point_2, R.string.onb_intro_point_3).forEachIndexed { index, res ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(PILL_SHAPE)
                        .background(Color(0xFF1C1C1E))
                        .border(1.dp, BarBorder, PILL_SHAPE),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${index + 1}", color = APP_TEXT_COLOR, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(stringResource(res), color = Color(0xFFEAEAEA), fontSize = 14.sp, lineHeight = 21.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
private fun TopicsStep(
    topics: List<Pair<String, String>>,
    selected: List<String>,
    countryName: String,
    onToggle: (String) -> Unit,
) {
    Title(R.string.onb_topics_title, R.string.onb_topics_desc)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        topics.forEach { (id, name) ->
            ToggleChip(text = name, selected = id in selected, onClick = { onToggle(id) })
        }
    }
    Text(stringResource(R.string.onb_topics_country_note, countryName), color = Color(0xFF8E9096), fontSize = 12.sp, lineHeight = 18.sp)
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
private fun KeywordsStep(
    keywords: List<String>,
    suggestions: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val removeText = stringResource(R.string.filter_edit_remove)

    fun add() {
        onAdd(input)
        input = ""
    }

    Title(R.string.onb_keywords_title, R.string.onb_keywords_desc)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            placeholder = { Text(stringResource(R.string.onb_keywords_hint), color = APP_SUBTITLE_TEXT_COLOR) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { add() }),
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
            onClick = { add() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = APP_TEXT_COLOR, contentColor = Color.Black),
            modifier = Modifier.heightIn(min = 56.dp),
        ) { Text(stringResource(R.string.filter_edit_add), fontWeight = FontWeight.Bold) }
    }
    if (keywords.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            keywords.forEach { keyword -> RemovableChip(keyword, removeText) { onRemove(keyword) } }
        }
    }
    if (suggestions.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(R.string.onb_keywords_suggest),
                color = APP_SUBTITLE_TEXT_COLOR,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { word -> SuggestChip(word) { onAdd(word) } }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun PreviewStep(
    preview: FilterPreview,
    exceptions: List<String>,
    reasonText: @Composable (com.sean.ratel.android.data.filter.ExcludeMatch) -> String,
    onToggleException: (String) -> Unit,
) {
    val hiddenCount = preview.hidden.count { it.videoId !in exceptions }

    Title(R.string.onb_preview_title, R.string.onb_preview_desc)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BarBg)
            .border(BorderStroke(1.dp, BarBorder), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            stringResource(R.string.onb_preview_count, hiddenCount),
            color = APP_TEXT_COLOR,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(R.string.onb_preview_total, preview.total),
            color = Color(0xFFC9CBD0),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }

    if (preview.hidden.isEmpty()) {
        Text(stringResource(R.string.onb_preview_none), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 13.sp, lineHeight = 19.sp)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.onb_preview_samples),
                color = APP_SUBTITLE_TEXT_COLOR,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            preview.hidden.take(MAX_PREVIEW_SAMPLES).forEach { sample ->
                SampleRow(sample, kept = sample.videoId in exceptions, reason = reasonText(sample.match)) {
                    onToggleException(sample.videoId)
                }
            }
        }
        Text(stringResource(R.string.onb_preview_note), color = Color(0xFF8E9096), fontSize = 12.sp, lineHeight = 18.sp)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SampleRow(
    sample: HiddenSample,
    kept: Boolean,
    reason: String,
    onToggle: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NetworkImage(
            url = sample.item.shortsVideoModel?.thumbNail.orEmpty(),
            contentDescription = null,
            modifier = Modifier.width(48.dp).height(72.dp).clip(RoundedCornerShape(8.dp)),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                sample.item.shortsVideoModel?.title.orEmpty(),
                color = if (kept) Color.White else APP_SUBTITLE_TEXT_COLOR,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (kept) null else TextDecoration.LineThrough,
            )
            Row { Tag(text = reason, hidden = !kept) }
        }
        OutlinedButton(
            onClick = onToggle,
            shape = PILL_SHAPE,
            border = BorderStroke(1.dp, if (kept) APP_TEXT_COLOR else Color(0xFF3A3A3A)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (kept) APP_TEXT_COLOR else Color(0xFFDDDDDD)),
            modifier = Modifier.heightIn(min = 36.dp),
        ) {
            Text(stringResource(if (kept) R.string.onb_preview_hide_again else R.string.onb_preview_keep), fontSize = 12.sp)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun Tag(
    text: String,
    hidden: Boolean,
) {
    Box(
        Modifier
            .clip(PILL_SHAPE)
            .background(if (hidden) HIDDEN_TAG_BG else KEPT_TAG_BG)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text, color = if (hidden) HIDDEN_TAG_FG else APP_TEXT_COLOR, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

/** 점선 테두리 느낌의 추천 칩("+ 단어"). 누르면 키워드로 들어간다. */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun SuggestChip(
    word: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .heightIn(min = 36.dp)
            .clip(PILL_SHAPE)
            .border(1.dp, Color(0xFF3A3A3A), PILL_SHAPE)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("+ $word", color = Color(0xFFDDDDDD), fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
private fun LoginSheet(
    busy: Boolean,
    onGoogle: () -> Unit,
    onWithout: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BarBg,
        contentColor = Color.White,
        onDismissRequest = onDismiss,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.onb_login_title),
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(stringResource(R.string.onb_login_desc), color = Color(0xFFC9CBD0), fontSize = 14.sp, lineHeight = 22.sp)
            }
            Button(
                onClick = onGoogle,
                enabled = !busy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF111111)),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) {
                Text(
                    stringResource(if (busy) R.string.my_signin_busy else R.string.my_signin_google),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            TextButton(onClick = onWithout, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.onb_login_without), color = Color(0xFFDDDDDD), fontSize = 14.sp)
                    Text(stringResource(R.string.onb_login_without_note), color = Color(0xFF8E9096), fontSize = 12.sp)
                }
            }
        }
    }
}

private fun countryDisplayName(countryCode: String?): String {
    if (countryCode.isNullOrBlank()) return ""
    return Locale("", countryCode).getDisplayCountry(Locale.getDefault()).ifBlank { countryCode }
}
