package com.sean.ratel.android.ui.push.item

import com.sean.ratel.android.data.domain.model.push.PushModel
import java.time.LocalDate

sealed class PushUiItem {
    data class DateHeader(
        val date: LocalDate,
    ) : PushUiItem()

    data class Content(
        val push: PushModel,
    ) : PushUiItem()
}
