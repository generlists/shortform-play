package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.local.pref.FilterPreference
import com.sean.ratel.android.data.local.pref.RecentVideoPreference
import com.sean.ratel.android.data.local.pref.YouTubeEndPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 내 제외 필터의 저장과 컴파일.
 * 필터가 없으면(기본) [compiled] 는 아무것도 거르지 않는 [CompiledFilter.NoOp] 만 내보낸다.
 */
@Singleton
class FilterRepository
    @Inject
    constructor(
        private val preference: FilterPreference,
        private val topicPacks: TopicPackRepository,
        private val recentVideoPreference: RecentVideoPreference,
        private val youTubeEndPreference: YouTubeEndPreference,
    ) {
        val state: Flow<FilterState> = preference.state

        suspend fun save(state: FilterState) = preference.save(state)

        /** 로그아웃·탈퇴 시 호출. 필터가 지워져 피드가 원래대로 돌아온다. */
        suspend fun clear() = preference.clear()

        suspend fun topicPackSet(): TopicPackSet = topicPacks.get()

        /** 온보딩(처음 실행 때 필터 만들기)을 이미 봤는지. 건너뛰어도 봤다고 기록한다. */
        val onboardingSeen: Flow<Boolean> = preference.onboardingSeen

        suspend fun markOnboardingSeen() = preference.markOnboardingSeen()

        /** 홈의 "내 필터 만들기" 배너를 "나중에"로 닫았는지. */
        val homeBannerDismissed: Flow<Boolean> = preference.homeBannerDismissed

        suspend fun dismissHomeBanner() = preference.dismissHomeBanner()

        /**
         * 지금 상태의 필터를 컴파일해서 내보낸다. 필터·옵션이 바뀌거나, 필요한 신호(싫어요·시청 기록)가 바뀌면 다시 내보낸다.
         * @param countryCode 선택한 국가(KR, US …) — 주제 단어를 어느 언어로 풀지 정한다
         */
        @OptIn(ExperimentalCoroutinesApi::class)
        fun compiled(countryCode: String?): Flow<CompiledFilter> =
            preference.state
                .flatMapLatest { state ->
                    val needsSignals = state.options.hideDisliked || state.options.hideWatched
                    if (!state.filter.hasRules && !needsSignals) {
                        flowOf(CompiledFilter.NoOp)
                    } else {
                        // 옵션이 켜진 신호만 구독한다(시청 기록은 읽을 때마다 JSON 을 파싱하므로)
                        combine(
                            if (state.options.hideDisliked) youTubeEndPreference.dislikedVideoIds else flowOf(emptySet()),
                            if (state.options.hideWatched) recentVideoPreference.watchedVideoIds else flowOf(emptySet()),
                        ) { disliked, watched -> FilterSignals(disliked, watched) }
                            .let { signals ->
                                flow {
                                    val terms =
                                        topicPacks.get().termsFor(
                                            state.filter.topics,
                                            FilterLanguages.forCountry(countryCode),
                                        )
                                    signals.collect { emit(CompiledFilter.compile(state.filter, state.options, it, terms)) }
                                }
                            }
                    }
                }.flowOn(Dispatchers.Default)
    }
