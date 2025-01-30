package com.sean.ratel.android.ui.home.shortform

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@Suppress("ktlint:standard:property-naming")
@HiltViewModel
class ShortFormViewModel
    @Inject
    constructor() : ViewModel() {
        private val _categoryByContents =
            MutableStateFlow<MutableMap<String, List<MainShortsModel>>>(
                mutableMapOf(),
            )
        val categoryByContents: StateFlow<Map<String, List<MainShortsModel>>> = _categoryByContents

        private val _categoryVideoMap =
            mutableStateMapOf<String, MutableMap<Int, MutableList<MainShortsModel>>>()

        private val _moreIndex = MutableStateFlow<MutableMap<String, Int>>(mutableMapOf())
        val moreIndex: StateFlow<MutableMap<String, Int>> = _moreIndex

        private val _shortsList = MutableStateFlow<MutableList<MainShortsModel>>(mutableListOf())

        fun setMoreEvent(
            categoryKey: String,
            moreIndex: Int,
        ) {
            _moreIndex.value =
                _moreIndex.value.toMutableMap().apply {
                    this[categoryKey] = moreIndex
                }
        }

        fun mainVideoData(list: MainShortFormList) {
            if (_shortsList.value.isNotEmpty()) {
                _shortsList.value.clear()
            }
            val zipList =
                (
                    (list.topFiveList.fiveList.flatMap { it.value }) +
                        (
                            list.shortformVideoList.videoSearchList.searchList +
                                list.shortformVideoList.videoCommentList.commentList +
                                list.shortformVideoList.videoLikeList.likeList
                        ) +
                        (
                            list.channelVideoList.channelSearchList.searchList +
                                list.channelVideoList.channelLikeList.likeList
                        ) +
                        list.editorPickList.pickList +
                        list.channelSubscriptionList.subscriptionList +
                        list.channelSubscriptionUpList.subscriptionUpList +
                        list.shortformRecommendList.recommendList
                ).toMutableList().distinct()

            _shortsList.value.addAll(zipList)

            setCategoryByYouTubeVideoList()
        }

        fun initData() {
            // 참조를 바꿔야 변경 가능
            // _categoryByContents.value.clear()
            _categoryByContents.value = mutableMapOf()
            // _moreIndex.value.clear()
            _moreIndex.value = mutableMapOf()

            setCategoryByYouTubeVideoList()
        }

        private fun setCategoryByYouTubeVideoList() {
            val categoryMap =
                _shortsList.value
                    .groupBy { it.shortsVideoModel?.category }
                    .filterKeys { it != null }
                    .mapKeys { it.key ?: "99" }

            setVideoMap(categoryMap)
        }

        private fun setVideoMap(categoryMap: Map<String, List<MainShortsModel>>) {
            categoryMap.entries.forEach { setContent(it.key, it.value.toMutableList()) }
        }

        fun maxMoreIndex(categoryKey: String): Int {
            // key = 22 , value 1, key = 23 , value 1, key = 24 , value 3, key = 25 , value 1, key = 17 , value 1, key = 20 , value 3, key = 10 , value 2
            RLog.d(TAG, "$categoryKey ,  count :${_categoryVideoMap[categoryKey]?.values?.count()}")
            return _categoryVideoMap[categoryKey]?.values?.count() ?: 0
        }

        private fun setContent(
            categoryKey: String,
            list: MutableList<MainShortsModel>,
        ) {
            val map = mutableMapOf<Int, MutableList<MainShortsModel>>()

            if (list.size > MORE_MAX_COUNT) {
                for ((key, i) in (list.indices step MORE_MAX_COUNT).withIndex()) {
                    val valueList =
                        if (i + MORE_MAX_COUNT <= list.size) {
                            list.slice(i until i + MORE_MAX_COUNT)
                        } else {
                            list.slice(i until list.size)
                        }
                    map[key] = valueList.toMutableList()

                    _categoryVideoMap[categoryKey] = map
                    if (i == 0) {
                        _categoryByContents.value[categoryKey] = valueList.toMutableList()
                    }
                }
            } else {
                _categoryVideoMap[categoryKey] = map
                _categoryByContents.value.put(categoryKey, list.toMutableList())
            }

            // 로그 확인 코드 (선택사항)
//        RLog.d(
//            "hbungshin",
//            _categoryVideoMap.map { it }.joinToString { "key = ${it.key} , value ${(it.value).size}" })
        }

        fun moreContent(
            categoryKey: String,
            childIndex: Int,
        ) {
            val childMap = _categoryVideoMap[categoryKey]
            val nextList = childMap?.get(childIndex)

            nextList?.let {
                // 현재 Map을 가져와서 변경 작업 수행
                val updatedMap = _categoryByContents.value.toMutableMap()
                val existingList = updatedMap[categoryKey] ?: emptyList()

                // 변경된 리스트 생성 후 Map에 업데이트
                updatedMap[categoryKey] = existingList + nextList
                _categoryByContents.value = updatedMap // 전체 Map을 새로 할당하여 StateFlow 업데이트
            }
        }

        companion object {
            private const val MORE_MAX_COUNT = 10
            val TAG = "ShortFormViewModel"
        }
    }
