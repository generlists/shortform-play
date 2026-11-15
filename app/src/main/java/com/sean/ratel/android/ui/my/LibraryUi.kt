package com.sean.ratel.android.ui.my

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.data.library.FolderKind
import com.sean.ratel.android.data.library.FolderSummary
import com.sean.ratel.android.data.library.LibraryFolder
import com.sean.ratel.android.data.library.LibraryLimits
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.BarBg
import com.sean.ratel.android.ui.theme.BarBorder

private val FOLDER_CARD_SHAPE = RoundedCornerShape(14.dp)
private const val FOLDER_COLUMNS = 2

/** 고정 폴더(좋아요·나중에 볼 영상)는 언어에 맞는 이름으로, 사용자 폴더는 저장된 이름으로. */
@Suppress("ktlint:standard:function-naming")
@Composable
fun folderDisplayName(folder: LibraryFolder): String =
    when (folder.kind) {
        FolderKind.LIKES -> stringResource(R.string.library_system_likes)
        FolderKind.WATCH_LATER -> stringResource(R.string.library_system_watch_later)
        FolderKind.CUSTOM -> folder.name
    }

/** 보관함이 알려 주는 안내(한도 초과, 이름 중복 …)를 토스트로 띄운다. */
@Suppress("ktlint:standard:function-naming")
@Composable
fun LibraryMessageEffect(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    val itemLimit = stringResource(R.string.library_msg_item_limit, LibraryLimits.ITEMS_MAX)
    val folderLimit = stringResource(R.string.library_msg_folder_limit, LibraryLimits.FOLDERS_MAX)
    val nameTaken = stringResource(R.string.library_msg_name_taken)
    val nameInvalid = stringResource(R.string.library_msg_name_invalid, LibraryLimits.NAME_MAX)
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            val text =
                when (message) {
                    LibraryMessage.ITEM_LIMIT -> itemLimit
                    LibraryMessage.FOLDER_LIMIT -> folderLimit
                    LibraryMessage.NAME_TAKEN -> nameTaken
                    LibraryMessage.NAME_INVALID -> nameInvalid
                }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }
}

/** 폴더 이름 입력(새 폴더, 이름 바꾸기). */
@Suppress("ktlint:standard:function-naming")
@Composable
fun FolderNameDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = Color.White,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(LibraryLimits.NAME_MAX) },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.library_name_hint), color = APP_SUBTITLE_TEXT_COLOR) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onConfirm(name) }),
                shape = RoundedCornerShape(12.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = APP_TEXT_COLOR,
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        cursorColor = APP_TEXT_COLOR,
                    ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text(stringResource(R.string.library_confirm), color = APP_TEXT_COLOR, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.my_cancel), color = Color.White) }
        },
    )
}

/** 마이 탭의 "내 폴더"(시안 04): 폴더 카드 2열 + "새 폴더". */
@Suppress("ktlint:standard:function-naming")
@Composable
fun LibraryFoldersCard(
    folders: List<FolderSummary>,
    onOpen: (LibraryFolder) -> Unit,
    onNewFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.library_folders_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onNewFolder) {
                Text(stringResource(R.string.library_new_folder), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 13.sp)
            }
        }
        folders.chunked(FOLDER_COLUMNS).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { summary -> FolderCard(summary, onClick = { onOpen(summary.folder) }, modifier = Modifier.weight(1f)) }
                // 홀수 개면 마지막 줄의 빈 칸을 채워 카드 폭을 맞춘다
                repeat(FOLDER_COLUMNS - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun FolderCard(
    summary: FolderSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .heightIn(min = 76.dp)
            .clip(FOLDER_CARD_SHAPE)
            .background(Color(0xFF1C1C1E))
            .border(BorderStroke(1.dp, BarBorder), FOLDER_CARD_SHAPE)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val cover = summary.covers.firstOrNull()
        Box(
            Modifier
                .width(40.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BarBg),
        ) {
            if (cover != null) NetworkImage(url = cover, contentDescription = null, modifier = Modifier.size(width = 40.dp, height = 52.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                folderDisplayName(summary.folder),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(stringResource(R.string.library_count, summary.count), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 12.sp)
        }
    }
}

/** "최근 본 영상 ›" 한 줄(시안 B2·04) — 누르면 최근 본 영상 목록으로 간다. */
@Suppress("ktlint:standard:function-naming")
@Composable
fun RecentWatchRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.library_recent), color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(
            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_arrow_right_small),
            contentDescription = null,
            tint = Color(0xFF8E9096),
            modifier = Modifier.size(18.dp),
        )
    }
}
