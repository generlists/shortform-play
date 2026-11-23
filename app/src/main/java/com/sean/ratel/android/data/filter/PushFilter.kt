package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.ShortsChannelModel
import com.sean.ratel.android.data.dto.ShortsVideoModel
import com.sean.ratel.android.data.repository.SettingRepository
import kotlinx.coroutines.flow.first
import so.smartlab.common.push.fcm.data.domain.PushType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 추천 푸시가 내 제외 필터(주제·키워드·차단 채널·본 영상·싫어요)에 걸리는지 판단한다.
 * 화면 목록과 같은 [CompiledFilter] 를 쓰므로, 목록에서 빠지는 영상은 푸시로도 오지 않는다.
 * 업로드·업데이트 푸시는 거르지 않는다.
 */
@Singleton
class PushFilterGate
    @Inject
    constructor(
        private val filterRepository: FilterRepository,
        private val settingRepository: SettingRepository,
    ) {
        /** 걸리면 이유를, 아니면(또는 추천 푸시가 아니면) null. */
        suspend fun excludeMatch(data: Map<String, String>): ExcludeMatch? {
            if (data["type"] != PushType.Recommend.name) return null
            val countryCode = settingRepository.getLocale().first()
            val filter = filterRepository.compiled(countryCode).first()
            if (filter.isNoOp) return null
            return filter.evaluate(data.toFilterTarget())
        }
    }

/** "[🎯 지금 인기 영상] 제목" 처럼 서버가 붙인 머리말. 머리말 글자가 키워드에 걸리지 않게 뗀다. */
private val TITLE_PREFIX = Regex("^\\[[^\\]]*]\\s*")

/**
 * 추천 푸시 data 를 필터가 판정하는 영상 모델로 바꾼다.
 * 예전 서버(필드가 없는 payload)에서 온 푸시는 videoId·제목·설명만으로 판정한다.
 */
internal fun Map<String, String>.toFilterTarget(): MainShortsModel {
    val title = this["videoTitle"]?.takeIf { it.isNotBlank() } ?: this["title"].orEmpty().replaceFirst(TITLE_PREFIX, "")
    return MainShortsModel(
        shortsChannelModel =
            ShortsChannelModel(
                channelId = this["channelId"].orEmpty(),
                channelThumbNail = this["channelThumbUrl"].orEmpty(),
                publishDate = "",
                channelTitle = this["channelTitle"].orEmpty(),
                channelDescription = "",
            ),
        shortsVideoModel =
            ShortsVideoModel(
                videoId = this["videoId"].orEmpty(),
                thumbNail = this["thumbUrl"].orEmpty(),
                title = title,
                description = this["body"].orEmpty(),
                duration = "",
                category = this["category"].orEmpty(),
                topicName = null,
                categoryName = this["categoryName"].orEmpty(),
            ),
    )
}
