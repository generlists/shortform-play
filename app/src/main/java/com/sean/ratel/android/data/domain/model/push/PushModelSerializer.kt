package com.sean.ratel.android.data.domain.model.push

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

object PushModelSerializer : JsonContentPolymorphicSerializer<PushModel>(PushModel::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out PushModel> {
        val obj = element as JsonObject
        val type = obj["type"]?.jsonPrimitive?.content

        return when (type) {
            AppPushType.Update.name -> PushAppUpdateModel.serializer()
            AppPushType.Upload.name -> PushUploadModel.serializer()
            AppPushType.Recommend.name -> PushRecommendModel.serializer()
            else -> error("Unknown PushModel type: $type")
        }
    }
}
