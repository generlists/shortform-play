package com.sean.ratel.android.data.domain.model.push

import com.sean.ratel.android.data.common.STRINGS.NOTIFICATON_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import so.smartlab.common.push.fcm.data.domain.PushType

@Serializable(with = PushModelSerializer::class)
sealed class PushModel {
    abstract val id: String
    abstract val type: AppPushType
    abstract val title: String
    abstract val body: String
    abstract val isRead: Boolean
    abstract val linkUrl: String
    abstract val createAt: Long
}

@Serializable
@SerialName("Update")
data class PushAppUpdateModel(
    override var id: String,
    override var type: AppPushType,
    override val title: String,
    override val body: String,
    override val isRead: Boolean,
    override val linkUrl: String,
    override val createAt: Long,
    val version: String,
    val force: Boolean,
) : PushModel()

@Serializable
@SerialName("Upload")
data class PushUploadModel(
    override var id: String,
    override var type: AppPushType,
    override val title: String,
    override val body: String,
    override val isRead: Boolean,
    override val linkUrl: String,
    override val createAt: Long,
    val date: String,
    val videoId: String,
    val thumbUrl: String,
    val channelThumbUrl: String,
) : PushModel()

@Serializable
@SerialName("Recommend")
data class PushRecommendModel(
    override var id: String,
    override var type: AppPushType,
    override val title: String,
    override val body: String,
    override val isRead: Boolean,
    override val linkUrl: String,
    override val createAt: Long,
    val videoId: String,
    val thumbUrl: String,
    val channelThumbUrl: String,
) : PushModel()

fun Map<String, String>.toPushModel(): PushModel =
    when (PushType.valueOf(this["type"] ?: "")) {
        PushType.Update -> {
            PushAppUpdateModel(
                id = this["id"] ?: "",
                type = AppPushType.valueOf(this["type"] ?: ""),
                title = this["title"] ?: NOTIFICATON_NAME,
                body = this["body"] ?: "",
                version = this["version"] ?: "",
                force = this["force"]?.toBoolean() ?: false,
                isRead = false,
                createAt = System.currentTimeMillis(),
                linkUrl = this["linkUrl"] ?: "shortformplay://home",
            )
        }

        PushType.Upload -> {
            PushUploadModel(
                id = this["id"] ?: "",
                type = AppPushType.valueOf(this["type"] ?: ""),
                title = this["title"] ?: NOTIFICATON_NAME,
                body = this["body"] ?: "",
                date = this["data"] ?: "",
                thumbUrl = this["thumbUrl"] ?: "",
                channelThumbUrl = this["channelThumbUrl"] ?: "",
                videoId = this["videoId"] ?: "",
                isRead = false,
                createAt = System.currentTimeMillis(),
                linkUrl = this["linkUrl"] ?: "shortformplay://home",
            )
        }

        PushType.Recommend -> {
            PushRecommendModel(
                id = this["id"] ?: "",
                type = AppPushType.valueOf(this["type"] ?: ""),
                title = this["title"] ?: NOTIFICATON_NAME,
                body = this["body"] ?: "",
                thumbUrl = this["thumbUrl"] ?: "",
                channelThumbUrl = this["channelThumbUrl"] ?: "",
                videoId = this["videoId"] ?: "",
                isRead = false,
                createAt = System.currentTimeMillis(),
                linkUrl = this["linkUrl"] ?: "shortformplay://home",
            )
        }
    }
