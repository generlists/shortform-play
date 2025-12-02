package com.sean.ratel.android.data.repository

import com.sean.ratel.android.data.dto.MainShortsModel
import javax.inject.Inject
import javax.inject.Singleton

// parcel 로 못넘겨서
@Singleton
class SearchResultDataRepository
    @Inject
    constructor() {
        private var searchCache: List<MainShortsModel>? = null

        fun setResults(results: List<MainShortsModel>) {
            searchCache = results
        }

        fun getResults(): List<MainShortsModel>? = searchCache

        fun clearCache() {
            searchCache = null
        }
    }
