package com.sean.ratel.android.ui.home
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.ui.theme.Red
import com.sean.ratel.android.utils.PhoneUtil.shareAppLinkButton
import com.sean.ratel.android.utils.UIUtil.hasPipPermission

@Suppress("ktlint:standard:function-naming")
@Composable
fun HomeTopBar(
    modifier: Modifier,
    mainViewModel: MainViewModel?,
    isEnd: Boolean,
    historyBack: () -> Unit,
    privacyOptionClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(enabled = false, onClick = {})
                .background(
                    if (isEnd) {
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    // 80%
                                    Color(0xCC000000),
                                    Color.Transparent,
                                ),
                        )
                    } else {
                        Brush.linearGradient(
                            // 동일한 빨간색으로 단일 색상 처리
                            colors = listOf(Red, Red),
                        )
                    },
                ),
        contentAlignment = Alignment.CenterStart,
    ) {
        val density = LocalDensity.current

        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp)
                    .onGloballyPositioned { coordinates ->
                        val topBarHeight =
                            with(density) {
                                coordinates.size.height
                                    .toDp()
                                    .value
                            }
                        mainViewModel?.setTopBarHeight(topBarHeight.toInt())
                    },
            // Row 내에서 수직 중앙 정렬
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val isPrivacy = mainViewModel?.isPrivacyOptionMenu?.collectAsState(false)

            if (isEnd) {
                BackButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    historyBack,
                )
            }

            TitleBox()
            Spacer(modifier = Modifier.weight(1f))

            if (isEnd) PIPButton(mainViewModel)

            SharerIconButton()
            PrivacyOptionMenu(isPrivacy?.value ?: false, privacyOptionClick)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun BackButton(
    modifier: Modifier,
    historyBack: () -> Unit,
) {
    IconButton(
        onClick = historyBack,
        modifier =
            modifier
                .size(32.dp)
                // 아이콘 크기 설정
                .padding(end = 5.dp),
    ) {
        Image(
            // 이미지 리소스
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Back Icon",
            modifier =
                Modifier
                    .height(32.dp)
                    .width(32.dp),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun LoginTypeItem(
    menuExpanded: Boolean,
    onMenuDismiss: () -> Unit,
) {
    DropdownMenu(
        modifier =
            Modifier
                .wrapContentSize()
                .padding(5.dp),
        expanded = menuExpanded,
        offset = DpOffset(0.dp, 0.dp),
        onDismissRequest = onMenuDismiss,
    ) {
        Column {
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun TitleBox() {
    Text(
        stringResource(R.string.title),
        modifier =
            Modifier
                .wrapContentSize(),
        // Text의 크기를 내용에 맞게 설정
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SharerIconButton() {
    val context = LocalContext.current
    IconButton(
        onClick = { shareAppLinkButton(context) },
        modifier = Modifier,
    ) {
        Image(
            // 이미지 리소스
            painter = painterResource(id = R.drawable.ic_share_main),
            contentDescription = stringResource(R.string.end_share),
            modifier = Modifier,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun PIPButton(mainViewModel: MainViewModel?) {
    val context = LocalContext.current
    val pipAction = mainViewModel?.pipClick?.collectAsState(initial = Pair(false, null))
    val pipButtonEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mainViewModel?.buttonClickState?.collect {
            pipButtonEnabled.value = it
        }
    }
    IconButton(
        onClick = {
            if (!context.hasPipPermission()) {
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.setting_pip_go),
                        Toast.LENGTH_SHORT,
                    ).show()

                mainViewModel?.goSettingView()
            } else {
                val action = !(pipAction?.value?.first ?: false)
                val viewPager = pipAction?.value?.second
                mainViewModel?.setPIPClick(Pair(action, viewPager))
            }
        },
        enabled = pipButtonEnabled.value,
        modifier =
            Modifier
                .width(32.dp)
                .height(32.dp),
    ) {
        Image(
            // 이미지 리소스
            painter = painterResource(id = R.drawable.pip_button),
            contentDescription = null,
            modifier =
                Modifier
                    .width(32.dp)
                    .height(32.dp),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun PrivacyOptionMenu(
    isPrivacy: Boolean,
    privacyOptionClick: () -> Unit,
) {
    if (isPrivacy) {
        Box(
            modifier = Modifier.wrapContentSize(),
        ) {
            IconButton(onClick = privacyOptionClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = null,
                    tint = Color.White,
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
        HomeTopBar(Modifier, null, true, {}, {})
    }
}
