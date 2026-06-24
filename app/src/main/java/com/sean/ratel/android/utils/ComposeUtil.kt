package com.sean.ratel.android.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.premiumDefault
import com.sean.ratel.android.ui.end.LoadingArea
import com.sean.ratel.android.ui.home.BillingViewModel
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.player.ui.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import so.smartlab.common.iap.ui.PremiumBottomSheet
import so.smartlab.common.utils.log.RLog

object ComposeUtil {
    private val URL_REGEX = Patterns.WEB_URL.toRegex()
    private val EMAIL_REGEX = Patterns.EMAIL_ADDRESS.toRegex()
    private val HASHTAG_REGEX = Regex("""#\w+""")
    private val MENTION_REGEX = Regex("""@[\w.]+""")

    @Composable
    fun pxToDp(px: Int): Int {
        // LocalDensity를 사용하여 px를 dp로 변환
        with(LocalDensity.current) {
            return px.toDp().value.toInt()
        }
    }

    @Composable
    fun LazyListState.isAtBottom(): Boolean {
        // 스크롤 방향 감지를 위한 previousOffset
        var previousOffset by remember { mutableStateOf(firstVisibleItemScrollOffset) }

        // 아래 방향 스크롤인지
        val isScrollingDown by remember {
            derivedStateOf {
                val current = firstVisibleItemScrollOffset
                val down = current > previousOffset
                previousOffset = current
                down
            }
        }

        return remember(this) {
            derivedStateOf {
                val layoutInfo = layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo

                if (layoutInfo.totalItemsCount == 0 || visibleItems.isEmpty()) {
                    return@derivedStateOf false
                }

                val lastVisibleItem = visibleItems.last()

                // viewport end (실제 화면 맨 아래)
                val viewportEnd = layoutInfo.viewportEndOffset

                // 마지막 아이템이 리스트의 마지막인지
                val isLastItem = lastVisibleItem.index == layoutInfo.totalItemsCount - 1

                // 마지막 아이템이 화면에 완전히 보이는지
                val isFullyVisible =
                    lastVisibleItem.offset + lastVisibleItem.size <= viewportEnd

                RLog.d(
                    "AT_BOTTOM",
                    "isScrollingDown=$isScrollingDown, lastIndex=${lastVisibleItem.index}, " +
                        "isLastItem=$isLastItem, isFullyVisible=$isFullyVisible, viewportEnd=$viewportEnd",
                )

                // 아래로 스크롤 중이고 + 마지막 아이템 완전 노출일 때만 true
                isLastItem && isFullyVisible && isScrollingDown
            }
        }.value
    }

    @Suppress("ktlint:standard:function-naming")
    @Composable
    fun GetShareLauncher(
        activity: Activity?,
        mainViewModel: MainViewModel,
    ): ManagedActivityResultLauncher<Intent, ActivityResult>? {
        val lifecycleOwner = LocalLifecycleOwner.current

        activity?.let {
            // Compose에서 ActivityResultLauncher 등록
            return rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
            ) { result ->
                Log.d("shareAppLinkButton", "result : $result")
                if (result.resultCode == Activity.RESULT_OK) {
                    lifecycleOwner.lifecycleScope.launch {
                        delay(500)

                        mainViewModel.onShareClicked(activity)
                    }
                }
            }
        }
        return null
    }

    @Suppress("ktlint:standard:function-naming")
    @Composable
    fun GetCommentLauncher(
        activity: Activity?,
        mainViewModel: MainViewModel,
    ): ManagedActivityResultLauncher<Intent, ActivityResult>? {
        val lifecycleOwner = LocalLifecycleOwner.current

        activity?.let {
            // Compose에서 ActivityResultLauncher 등록
            return rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
            ) { result ->
                Log.d("shareAppLinkButton", "result : $result")
                if (result.resultCode == Activity.RESULT_OK) {
                    lifecycleOwner.lifecycleScope.launch {
                        delay(500)

                        mainViewModel.reviewManager.trackComment()
                    }
                }
            }
        }
        return null
    }

    @Composable
    @Suppress("ktlint:standard:function-naming")
    fun LinkedText(
        text: String,
        modifier: Modifier = Modifier,
        fontSize: TextUnit = 13.sp,
        color: Color = Color(0xFFCCCCCC),
        linkColor: Color = APP_TEXT_COLOR,
        onEmailClick: (String) -> Unit = {},
        onUrlClick: (String) -> Unit = {},
        onHashtagClick: (String) -> Unit = {},
        onMentionClick: (String) -> Unit = {},
    ) {
        val annotatedString =
            remember(text) {
                buildAnnotatedString {
                    val matches =
                        (
                            EMAIL_REGEX.findAll(text).map { Triple(it, "EMAIL", it.value) } +
                                URL_REGEX.findAll(text).map {
                                    Triple(
                                        it,
                                        "URL",
                                        if (it.value.startsWith("http://") || it.value.startsWith("https://")) {
                                            it.value
                                        } else {
                                            "https://${it.value}"
                                        },
                                    )
                                } +
                                HASHTAG_REGEX.findAll(text).map { Triple(it, "HASHTAG", it.value.removePrefix("#")) } +
                                MENTION_REGEX.findAll(text).map { Triple(it, "MENTION", it.value.removePrefix("@")) }
                        ).sortedBy { it.first.range.first }
                            .fold(mutableListOf<Triple<MatchResult, String, String>>()) { acc, item ->
                                val lastEnd =
                                    acc
                                        .lastOrNull()
                                        ?.first
                                        ?.range
                                        ?.last ?: -1
                                if (item.first.range.first > lastEnd) acc.add(item)
                                acc
                            }

                    var lastIndex = 0
                    matches.forEach { (match, tag, annotation) ->
                        append(text.substring(lastIndex, match.range.first))

                        val linkStyle =
                            TextLinkStyles(
                                style =
                                    SpanStyle(
                                        color = linkColor,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                            )

                        when (tag) {
                            "EMAIL" -> {
                                addLink(
                                    clickable =
                                        LinkAnnotation.Clickable(
                                            tag = tag,
                                            styles = linkStyle,
                                            linkInteractionListener = { onEmailClick(annotation) },
                                        ),
                                    start = length,
                                    end = length + match.value.length,
                                )
                            }

                            "URL" -> {
                                addLink(
                                    clickable =
                                        LinkAnnotation.Clickable(
                                            tag = tag,
                                            styles = linkStyle,
                                            linkInteractionListener = { onUrlClick(annotation) },
                                        ),
                                    start = length,
                                    end = length + match.value.length,
                                )
                            }

                            "HASHTAG" -> {
                                addLink(
                                    clickable =
                                        LinkAnnotation.Clickable(
                                            tag = tag,
                                            styles = linkStyle,
                                            linkInteractionListener = { onHashtagClick(annotation) },
                                        ),
                                    start = length,
                                    end = length + match.value.length,
                                )
                            }

                            "MENTION" -> {
                                addLink(
                                    clickable =
                                        LinkAnnotation.Clickable(
                                            tag = tag,
                                            styles = linkStyle,
                                            linkInteractionListener = { onMentionClick(annotation) },
                                        ),
                                    start = length,
                                    end = length + match.value.length,
                                )
                            }
                        }

                        append(match.value)
                        lastIndex = match.range.last + 1
                    }
                    if (lastIndex < text.length) append(text.substring(lastIndex))
                }
            }

        Text(
            text = annotatedString,
            style =
                TextStyle(
                    fontSize = fontSize,
                    color = color,
                    lineHeight = (fontSize.value * 1.6).sp,
                ),
            modifier = modifier,
        )
    }

    @Composable
    @Suppress("ktlint:standard:function-naming")
    fun PremiumPopup(
        billingViewModel: BillingViewModel,
        forceDonotMessageRow: Boolean = false,
        show: (Boolean, AdRemoveButtonType) -> Unit,
    ) {
        val context = LocalContext.current
        val activity = context.findActivity()
        val isAdRemoved by billingViewModel.isAdRemoved.collectAsStateWithLifecycle()
        val premiumData by billingViewModel.premiumData.collectAsStateWithLifecycle()
        val isDonotAain by billingViewModel.doNotShowAgain.collectAsStateWithLifecycle()
        val coroutine = rememberCoroutineScope()

        RLog.d("In App Purchase", "isAdRemoved : $isAdRemoved isDonotAain : $isDonotAain")
        if (isAdRemoved) return

        RLog.d("In App Purchase", "premiumData : $premiumData")

        when (val state = premiumData) {
            is UiState.Loading, UiState.Idle -> {
                // 로딩 인디케이터
                LoadingArea(isLoading = true)
            }

            is UiState.Success -> {
                RLog.d(
                    "In App Purchase",
                    "forceDonotMessageRow : $forceDonotMessageRow isPromotionActive : ${state.data.isPromotionActive}",
                )
                PremiumBottomSheet(
                    data = state.data,
                    colors = premiumDefault(),
                    onPurchaseClick = {
                        activity?.let {
                            RLog.d("IAP", "purchase")
                            billingViewModel.purchase(activity)
                            show(false, AdRemoveButtonType.PurChase)
                        } ?: run {
                            RLog.d("In App Purchase", "activity not founded error")
                        }
                    },
                    showDoNotShowAgain = if (forceDonotMessageRow) null else isDonotAain,
                    onRestoreClick = {
                        billingViewModel.restore()
                        show(false, AdRemoveButtonType.Restore)
                    },
                    onDismiss = {
                        show(false, AdRemoveButtonType.DisMiss)
                    },
                    onDoNotShowAgain = {
                        coroutine.launch {
                            RLog.d("In App Purchase", "[APP] do not check : $it")
                            billingViewModel.markDoNotShowAgain(it)
                            show(false, AdRemoveButtonType.DoNotAgain)
                        }
                    },
                )
            }

            is UiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}

enum class AdRemoveButtonType {
    PurChase,
    Restore,
    DoNotAgain,
    DisMiss,
}
