package com.sean.ratel.android.ui.my

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.BarBg
import com.sean.ratel.android.ui.theme.BarBorder

private val CHIP_SHAPE = RoundedCornerShape(50)
private val CARD_SHAPE = RoundedCornerShape(20.dp)

@StringRes
private fun AccountMessage.textRes(): Int =
    when (this) {
        AccountMessage.SIGNED_IN -> R.string.my_msg_signed_in
        AccountMessage.NO_ACCOUNT -> R.string.my_msg_no_account
        AccountMessage.SIGN_IN_FAILED -> R.string.my_msg_signin_failed
        AccountMessage.SIGNED_OUT -> R.string.my_msg_signed_out
        AccountMessage.ACCOUNT_DELETED -> R.string.my_msg_deleted
        AccountMessage.ACCOUNT_DELETE_FAILED -> R.string.my_msg_delete_failed
        AccountMessage.FILTER_SAVED -> R.string.filter_msg_saved
        AccountMessage.FILTER_SAVE_PENDING -> R.string.filter_msg_pending
        AccountMessage.NEED_SIGN_IN -> R.string.filter_msg_need_signin
    }

/** ViewModel 이 알려 주는 결과를 토스트로 보여 준다. [onMessage] 로 화면별 후속 동작(예: 저장하면 뒤로)을 붙인다. */
@Suppress("ktlint:standard:function-naming")
@Composable
internal fun AccountMessageEffect(
    viewModel: AccountViewModel,
    onMessage: (AccountMessage) -> Unit = {},
) {
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            Toast.makeText(context, message.textRes(), Toast.LENGTH_SHORT).show()
            onMessage(message)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
internal fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(CARD_SHAPE)
            .background(BarBg)
            .border(BorderStroke(1.dp, BarBorder), CARD_SHAPE)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

/** 주제 선택용 칩. 선택하면 라임색으로 채워진다. */
@Suppress("ktlint:standard:function-naming")
@Composable
internal fun ToggleChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .heightIn(min = 44.dp)
            .clip(CHIP_SHAPE)
            .background(if (selected) APP_TEXT_COLOR else BarBg)
            .border(1.dp, if (selected) APP_TEXT_COLOR else BarBorder, CHIP_SHAPE)
            .selectable(selected = selected, role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/** 읽기 전용 칩(마이 화면 요약). */
@Suppress("ktlint:standard:function-naming")
@Composable
internal fun InfoChip(text: String) {
    Box(
        Modifier
            .heightIn(min = 32.dp)
            .clip(CHIP_SHAPE)
            .background(Color(0xFF1C1C1E))
            .border(1.dp, BarBorder, CHIP_SHAPE)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White, fontSize = 13.sp)
    }
}

/** 지울 수 있는 칩(키워드·채널). × 영역이 눌린다. */
@Suppress("ktlint:standard:function-naming")
@Composable
internal fun RemovableChip(
    text: String,
    removeDescription: String,
    onRemove: () -> Unit,
) {
    Row(
        Modifier
            .heightIn(min = 40.dp)
            .clip(CHIP_SHAPE)
            .background(APP_TEXT_COLOR)
            .padding(start = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onRemove, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = removeDescription,
                tint = Color.Black,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
internal fun StatusPill(
    text: String,
    active: Boolean,
) {
    Box(
        Modifier
            .clip(CHIP_SHAPE)
            .background(if (active) APP_TEXT_COLOR else Color.Transparent)
            .border(1.dp, if (active) APP_TEXT_COLOR else BarBorder, CHIP_SHAPE)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            color = if (active) Color.Black else APP_SUBTITLE_TEXT_COLOR,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
internal fun BusyIndicator() {
    CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        strokeWidth = 2.dp,
        color = Color.Black,
    )
}
