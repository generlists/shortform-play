package com.sean.ratel.android.ui.home.shortform

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.TopicItem
import com.sean.ratel.android.data.dto.TopicList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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

            setCategoryByYouTubeVideoList()
            // 로그 확인 코드 (선택사항)
            _categoryVideoMap.size

//            RLog.d(
//
//                "hbungshin",
//
//                _categoryVideoMap.entries.joinToString("\n") {
//
//                    "key = ${it.key}, size = ${it.value.size}"
//
//                })
//            RLog.d(
//                "hbungshin",
//                _categoryVideoMap.map { it }.joinToString { "key = ${it.key} , value ${(it.value).size}" })


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
            var categoryMap =
                _shortsList.value
                    .groupBy { it.shortsVideoModel?.category }
                    .filterKeys { it != null }
                    .mapKeys { it.key ?: "99" }

           // categoryMap += getTopicVideoList(_topicList.value)

            setVideoMap(categoryMap)




        }

        private fun setVideoMap(categoryMap: Map<String, List<MainShortsModel>>) {
            categoryMap.entries.forEach { setContent(it.key, it.value.toMutableList()) }
        }

        private fun getTopicVideoList(topicList: Map<String, TopicItem>):Map<String,List<MainShortsModel>> {

               return topicList.mapValues { (_, topicItem) ->
                    listOfNotNull(topicItem.popularlist, topicItem.viewlist, topicItem.subscriberlist)
                        .flatMap { filterList -> filterList.topicList }
                        .flatMap { groupItem -> groupItem.topicList }
                        .map { it.copy(shortsVideoModel = it.shortsVideoModel?.copy(categoryName = topicItem.topicName)) }
                }

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
                _categoryByContents.value[categoryKey] = list.toMutableList()
            }

        }

    fun moreContent(
        categoryKey: String,
        childIndex: Int,

        ) {

        val nextList = _categoryVideoMap[categoryKey]?.get(childIndex)
        if (nextList.isNullOrEmpty()) {

            RLog.d("SSKKKKK", "nextList empty / categoryKey=$categoryKey, childIndex=$childIndex"
            )

            return

        }

        _categoryByContents.update { currentMap ->

            val existingList = currentMap[categoryKey].orEmpty()

            currentMap.toMutableMap().apply {

                this[categoryKey] = existingList + nextList

            }

        }

        RLog.d(

            "SSKKKKK",

            "categoryKey=$categoryKey, add=${nextList.size}, total=${_categoryByContents.value[categoryKey]?.size}"

        )

    }

        companion object {
            private const val MORE_MAX_COUNT = 10
            val TAG = "ShortFormViewModel"
        }
    }
