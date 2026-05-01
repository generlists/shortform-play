package com.sean.ratel.android.ui.home.shortform

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.TopicItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.collections.flatMap
import kotlin.collections.mapValues

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

        private val _topicList = MutableStateFlow<Map<String, TopicItem>>(mapOf())

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
            _topicList.value = list.topicList.topicList
            RLog.d("LLLLLLLLLLLLL", "setVideoMap")
            setCategoryByYouTubeVideoList()
        }

        fun initData() {
            RLog.d("LLLLLLLLLLLLL", "initData")
            // 참조를 바꿔야 변경 가능
            // _categoryByContents.value.clear()
            _categoryByContents.value = mutableMapOf()
            // _moreIndex.value.clear()
            _moreIndex.value = mutableMapOf()

            setCategoryByYouTubeVideoList()
        }

        private fun setCategoryByYouTubeVideoList() {
            var categoryMap =
                _shortsList.value
                    .groupBy { it.shortsVideoModel?.category }
                    .filterKeys { it != null }
                    .mapKeys { it.key ?: "99" }

            categoryMap += getTopicVideoList(_topicList.value)
            RLog.d("LLLLLLLLLLLLL", "setCategoryByYouTubeVideoList")
            setVideoMap(categoryMap)
        }

        private fun setVideoMap(categoryMap: Map<String, List<MainShortsModel>>) {
            categoryMap.entries.forEach {
                setContent(it.key, it.value.toMutableList())
            }
        }

        private fun getTopicVideoList(topicList: Map<String, TopicItem>): Map<String, List<MainShortsModel>> =
            topicList.mapValues { (_, topicItem) ->
                listOfNotNull(topicItem.popularlist, topicItem.viewlist, topicItem.subscriberlist)
                    .flatMap { filterList -> filterList.topicList }
                    .flatMap { groupItem -> groupItem.topicList }
                    .map { it.copy(shortsVideoModel = it.shortsVideoModel?.copy(categoryName = topicItem.topicName)) }
            }

        fun maxMoreIndex(categoryKey: String): Int {
            // key = 22 , value 1, key = 23 , value 1, key = 24 , value 3, key = 25 , value 1, key = 17 , value 1, key = 20 , value 3, key = 10 , value 2
            RLog.d(TAG, "$categoryKey ,  count :${_categoryVideoMap[categoryKey]?.values?.count()}")
            return _categoryVideoMap[categoryKey]?.values?.count() ?: 0
        }

        private fun setContent(
            categoryKey: String,
            list: List<MainShortsModel>,
        ) {
            val chunkMap: MutableMap<Int, MutableList<MainShortsModel>> = mutableMapOf()

            val chunks = list.chunked(MORE_MAX_COUNT)

            chunks.forEachIndexed { index, chunk ->

                chunkMap[index] = chunk.toMutableList()
            }

            _categoryVideoMap[categoryKey] = chunkMap

            val firstVisibleList =
                if (chunks.isNotEmpty()) {
                    chunks.first().toMutableList()
                } else {
                    mutableListOf()
                }
            // 새 맵으로 갈아 끼어야한다.
            _categoryByContents.update { currentMap ->
                currentMap.toMutableMap().apply {
                    this[categoryKey] = firstVisibleList
                }
            }

            RLog.d(
                "LLLLLLLLLLLLL",
                "setContent categoryKey=$categoryKey, full=${list.size}, first=${firstVisibleList.size}, chunkCount=${chunkMap.size}",
            )
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
            RLog.d("LLLLLLLLLLLLL", "moreContent _categoryByContents.value ${_categoryByContents.value.get("10")?.size}")
        }

        companion object {
            private const val MORE_MAX_COUNT = 10
            val TAG = "ShortFormViewModel"
        }
    }
