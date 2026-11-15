package com.sean.ratel.android.data.filter

/**
 * 사용자가 만든 제외 필터. 서버(/shortformplay/v2/me/filters)와 동기화되는 부분이다.
 * 기본 인스턴스는 아무것도 거르지 않는다(필터를 만들기 전에는 기존 화면과 똑같다).
 */
data class ExcludeFilter(
    /** 제외 주제 id (예: male_idol). 주제 묶음(topics.json)이 단어 목록으로 풀어 준다. */
    val topics: List<String> = emptyList(),
    /** 제외 키워드. 제목, 설명, 카테고리, 채널명에서 찾는다. */
    val keywords: List<String> = emptyList(),
    /** 차단한 채널 id. */
    val blockedChannels: List<String> = emptyList(),
    /** "빼지 않기"로 예외 처리한 영상 id. 다른 모든 규칙보다 우선한다. */
    val exceptions: List<String> = emptyList(),
    /** 서버가 저장할 때마다 올리는 번호. 0 = 서버에 저장한 적 없음. */
    val version: Int = 0,
) {
    val hasRules: Boolean
        get() = topics.isNotEmpty() || keywords.isNotEmpty() || blockedChannels.isNotEmpty()
}

/** 이 기기에서만 쓰는 옵션(서버와 동기화하지 않음). 기본값은 꺼짐. */
data class FilterOptions(
    /** 싫어요를 누른 영상을 목록에서 뺀다. */
    val hideDisliked: Boolean = false,
    /** 이미 본 영상을 목록에서 뺀다. */
    val hideWatched: Boolean = false,
)

/** 기기에 쌓인 신호(싫어요, 시청 기록). 옵션이 켜졌을 때만 채워서 엔진에 넘긴다. */
data class FilterSignals(
    val dislikedVideoIds: Set<String> = emptySet(),
    val watchedVideoIds: Set<String> = emptySet(),
)

/**
 * 저장되는 필터 전체 상태.
 * @param dirty 서버에 아직 올리지 못한 변경이 있다(오프라인에서 저장했거나 업로드가 실패했다). 다음 동기화 때 다시 올린다.
 */
data class FilterState(
    val filter: ExcludeFilter = ExcludeFilter(),
    val options: FilterOptions = FilterOptions(),
    val dirty: Boolean = false,
)
