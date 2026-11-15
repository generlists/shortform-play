package com.sean.ratel.android.ui.home.main.itemview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.BarBg
import com.sean.ratel.android.ui.theme.BarBorder

private val BAR_SHAPE = RoundedCornerShape(14.dp)

/** 홈 맨 위 "내 필터 적용 중 · 주제 3 · 키워드 2 [관리]" (시안 B1). 누르면 필터 관리로 간다. */
@Suppress("ktlint:standard:function-naming")
@Composable
fun FilterStatusBar(
    topics: Int,
    keywords: Int,
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(BAR_SHAPE)
            .background(BarBg)
            .border(BorderStroke(1.dp, BarBorder), BAR_SHAPE)
            .clickable(onClick = onManage)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painterResource(R.drawable.ic_filter_funnel),
            contentDescription = null,
            tint = APP_TEXT_COLOR,
            modifier = Modifier.size(18.dp),
        )
        Text(
            stringResource(R.string.home_filter_bar, topics, keywords),
            color = Color(0xFFDDDDDD),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        Text(stringResource(R.string.home_filter_manage), color = APP_TEXT_COLOR, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

/** 쇼츠 목록 아래 "필터로 N개를 빼고 보여드렸어요 · 숨긴 영상 보기" (시안 B1). */
@Suppress("ktlint:standard:function-naming")
@Composable
fun FilterHiddenNote(
    count: Int,
    onViewHidden: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painterResource(R.drawable.ic_eye_off_small),
            contentDescription = null,
            tint = Color(0xFF8E9096),
            modifier = Modifier.size(14.dp),
        )
        Text(stringResource(R.string.home_filter_note, count), color = Color(0xFF8E9096), fontSize = 12.sp, modifier = Modifier.weight(1f))
        TextButton(onClick = onViewHidden) {
            Text(
                stringResource(R.string.home_filter_note_view),
                color = Color(0xFFDDDDDD),
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

/** 필터가 없을 때 홈 맨 위 한 줄 배너 "[깔때기] 내 필터 만들기 / 안 보고 싶은 건 빼고 보기 [닫기]". 적용 중 막대(시안 B1)와 같은 크기·모양이다. */
@Suppress("ktlint:standard:function-naming")
@Composable
fun FilterCreateBanner(
    onCreate: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(BAR_SHAPE)
            .background(Color(0xFF0E1408))
            .border(BorderStroke(1.dp, APP_TEXT_COLOR), BAR_SHAPE)
            .clickable(onClick = onCreate)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(APP_TEXT_COLOR),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_filter_funnel),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.home_banner_create),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                stringResource(R.string.home_banner_short),
                color = Color(0xFFC9CBD0),
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
        // 나중에: 글자 대신 닫기 아이콘(한 줄에 맞추기 위해). 누르면 배너를 숨긴다
        IconButton(onClick = onLater) {
            Icon(
                painterResource(R.drawable.ic_suggest_small_close),
                contentDescription = stringResource(R.string.home_banner_later),
                tint = Color(0xFF8E9096),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
