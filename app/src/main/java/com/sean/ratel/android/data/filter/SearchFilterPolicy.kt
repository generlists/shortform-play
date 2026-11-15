package com.sean.ratel.android.data.filter

import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.SearchResultModel

/** 검색 결과에 필터를 어떻게 적용했는지. 화면이 안내 문구를 고르는 데 쓴다. */
enum class SearchFilterMode {
    /** 필터가 없다(기본). */
    OFF,

    /** 필터를 적용했다. 걸러진 것이 있으면 "숨긴 영상 N개 보기"를 보여 준다. */
    FILTERED,

    /** 검색어가 제외 항목과 겹쳐서 필터를 적용하지 않았다. */
    QUERY_OVERLAP,

    /** 결과가 전부 필터에 걸려서 빈 화면이 되지 않게 전체를 보여 준다. */
    ALL_HIDDEN,

    /** 웹 링크·알림 등 사용자가 직접 찾아온 검색이라 필터를 적용하지 않았다. */
    DEEP_LINK,

    /** 사용자가 "숨긴 영상 보기"를 눌러 모두 보는 중이다. */
    REVEALED,
}

/** 화면에 알려 줄 상태(안내 문구 선택용). */
data class SearchFilterInfo(
    val mode: SearchFilterMode = SearchFilterMode.OFF,
    val hiddenCount: Int = 0,
)

data class SearchFilterView<T>(
    val visible: List<T>,
    val hiddenCount: Int,
    val mode: SearchFilterMode,
)

/**
 * 검색에서 필터를 쓸지 정하는 규칙. 원칙: **사용자가 직접 요청한 것은 필터가 숨기지 않는다.**
 *
 * 1. 웹 링크 등으로 들어온 검색 → 필터 없이 보여 준다.
 * 2. 사용자가 "숨긴 영상 보기"를 눌렀다 → 필터 없이 보여 준다.
 * 3. 검색어가 제외 항목과 겹친다(예: 정치를 뺐는데 "정치 토론"을 검색) → 필터 없이 보여 준다.
 * 4. 필터를 적용했더니 하나도 안 남는다 → 필터 없이 보여 준다(빈 화면 방지).
 * 5. 그 밖에는 필터를 적용하고, 걸러진 개수를 알려 준다.
 */
object SearchFilterPolicy {
    fun keyword(
        items: List<SearchResultModel>,
        filter: CompiledFilter,
        query: String,
        fromDeepLink: Boolean,
        revealed: Boolean,
    ): SearchFilterView<SearchResultModel> {
        if (filter.isNoOp || items.isEmpty()) return SearchFilterView(items, 0, SearchFilterMode.OFF)
        if (fromDeepLink) return SearchFilterView(items, 0, SearchFilterMode.DEEP_LINK)
        if (filter.matchesText(query)) return SearchFilterView(items, 0, SearchFilterMode.QUERY_OVERLAP)

        val kept = items.filter { filter.keep(it) }
        val hidden = items.size - kept.size
        return when {
            hidden == 0 -> SearchFilterView(items, 0, SearchFilterMode.FILTERED)
            revealed -> SearchFilterView(items, hidden, SearchFilterMode.REVEALED)
            kept.isEmpty() -> SearchFilterView(items, hidden, SearchFilterMode.ALL_HIDDEN)
            else -> SearchFilterView(kept, hidden, SearchFilterMode.FILTERED)
        }
    }

    /**
     * 일간 검색(날짜·카테고리로 둘러보는 목록). 목록 위치 번호가 끝 화면과 이어지므로 **원본 단계에서 한 번** 걸러서
     * 화면과 끝 화면이 같은 목록을 쓰게 한다. 전부 걸리면 원본을 그대로 쓴다.
     */
    fun daily(
        items: List<MainShortsModel>,
        filter: CompiledFilter,
        fromDeepLink: Boolean,
    ): SearchFilterView<MainShortsModel> {
        if (filter.isNoOp || items.isEmpty()) return SearchFilterView(items, 0, SearchFilterMode.OFF)
        if (fromDeepLink) return SearchFilterView(items, 0, SearchFilterMode.DEEP_LINK)
        val kept = items.filter { filter.keep(it) }
        val hidden = items.size - kept.size
        return when {
            hidden == 0 -> SearchFilterView(items, 0, SearchFilterMode.FILTERED)
            kept.isEmpty() -> SearchFilterView(items, hidden, SearchFilterMode.ALL_HIDDEN)
            else -> SearchFilterView(kept, hidden, SearchFilterMode.FILTERED)
        }
    }
}
