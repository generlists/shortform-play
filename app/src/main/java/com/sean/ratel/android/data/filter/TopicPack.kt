package com.sean.ratel.android.data.filter

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * 주제 묶음 — "남자 아이돌" 같은 주제 하나를 언어별 단어 목록으로 풀어 주는 사전.
 * 사용자는 주제만 고르고, 앱이 선택한 국가의 언어에 맞는 단어를 키워드 필터로 풀어서 쓴다.
 *
 * JSON 형식(assets/topics.json, 나중에 Firebase Storage 로 갱신):
 * { "version": 1, "topics": [ { "id": "male_idol",
 *     "names": { "ko": "남자 아이돌", "en": "Boy bands" },
 *     "terms": { "ko": ["남돌", ...], "en": ["boy band", ...] } } ] }
 */
data class TopicPack(
    val id: String,
    val names: Map<String, String>,
    val terms: Map<String, List<String>>,
)

data class TopicPackSet(
    val version: Int = 0,
    val topics: List<TopicPack> = emptyList(),
) {
    private val byId: Map<String, TopicPack> by lazy { topics.associateBy { it.id } }

    /** 화면에 보여줄 주제 이름. 해당 언어가 없으면 영어, 그것도 없으면 id. */
    fun nameOf(
        topicId: String,
        language: String,
    ): String {
        val names = byId[topicId]?.names ?: return topicId
        return names[language] ?: names[FALLBACK_LANGUAGE] ?: names.values.firstOrNull() ?: topicId
    }

    /** 고른 주제들을 주어진 언어들의 단어 목록으로 푼다. 알 수 없는 주제 id 는 건너뛴다. */
    fun termsFor(
        topicIds: Collection<String>,
        languages: List<String>,
    ): Map<String, List<String>> {
        val out = LinkedHashMap<String, List<String>>()
        topicIds.forEach { id ->
            val pack = byId[id] ?: return@forEach
            val terms = languages.flatMap { pack.terms[it].orEmpty() }.distinct()
            if (terms.isNotEmpty()) out[id] = terms
        }
        return out
    }

    companion object {
        const val FALLBACK_LANGUAGE = "en"
    }
}

object TopicPackParser {
    /** 형식이 어긋난 항목은 건너뛰고, JSON 자체가 깨졌으면 빈 묶음을 돌려준다(앱은 주제 필터만 못 쓴다). */
    fun parse(json: String?): TopicPackSet {
        if (json.isNullOrBlank()) return TopicPackSet()
        return try {
            val root = JsonParser.parseString(json).asJsonObject
            val topics =
                root
                    .getAsJsonArraySafe("topics")
                    .mapNotNull { parseTopic(it) }
            TopicPackSet(version = root.get("version")?.takeIf { it.isJsonPrimitive }?.asInt ?: 0, topics = topics)
        } catch (e: Exception) {
            TopicPackSet()
        }
    }

    private fun parseTopic(element: JsonElement): TopicPack? {
        if (!element.isJsonObject) return null
        val obj = element.asJsonObject
        val id = obj.get("id")?.takeIf { it.isJsonPrimitive }?.asString?.trim().orEmpty()
        if (id.isEmpty()) return null
        val names = LinkedHashMap<String, String>()
        obj.getAsJsonObjectSafe("names").entrySet().forEach { (lang, value) ->
            if (value.isJsonPrimitive) names[lang] = value.asString
        }
        val terms = LinkedHashMap<String, List<String>>()
        obj.getAsJsonObjectSafe("terms").entrySet().forEach { (lang, value) ->
            if (value.isJsonArray) {
                terms[lang] = value.asJsonArray.filter { it.isJsonPrimitive }.map { it.asString }
            }
        }
        return TopicPack(id, names, terms)
    }

    private fun JsonObject.getAsJsonArraySafe(name: String): JsonArray = get(name)?.takeIf { it.isJsonArray }?.asJsonArray ?: JsonArray()

    private fun JsonObject.getAsJsonObjectSafe(name: String): JsonObject =
        get(name)?.takeIf { it.isJsonObject }?.asJsonObject ?: JsonObject()
}

/** 선택한 국가의 영상이 쓰는 언어. 주제 단어는 그 언어와 영어(제목에 영어가 섞이는 경우가 많다)로 푼다. */
object FilterLanguages {
    fun forCountry(countryCode: String?): List<String> {
        val primary =
            when (countryCode?.uppercase()) {
                "KR" -> "ko"
                "JP" -> "ja"
                "TW" -> "zh"
                "TH" -> "th"
                "ID" -> "id"
                else -> "en" // US, CA(en) 등
            }
        return listOf(primary, TopicPackSet.FALLBACK_LANGUAGE).distinct()
    }
}
