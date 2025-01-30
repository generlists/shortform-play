package com.sean.ratel.android.ui.home.main

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@Suppress("ktlint:standard:property-naming")
@HiltViewModel
class MainVideoViewModel
    @Inject
    constructor(
        val navigator: Navigator,
    ) : ViewModel() {
        private val _shortsList = MutableStateFlow<MutableList<MainShortsModel>>(mutableListOf())
        val shortsList: StateFlow<MutableList<MainShortsModel>> = _shortsList

        private val _shortsListMap = mutableStateMapOf<Int, MutableList<MainShortsModel>>()

        private val _moreIndex = MutableStateFlow<Int>(0)
        val moreIndex: StateFlow<Int> get() = _moreIndex

        fun setMorEVent(moreIndex: Int): Int {
            _moreIndex.value = moreIndex
            return _moreIndex.value
        }

        fun mainShortData(list: MutableList<MainShortsModel>) {
            setContent(list)
        }

        private fun setContent(list: MutableList<MainShortsModel>) {
            if (_shortsList.value.size > 0) _shortsList.value.clear()

            if (list.size <= MORE_MAX_COUNT) {
                _shortsList.value.addAll(list)
            } else {
                for ((key, i) in (list.indices step MORE_MAX_COUNT).withIndex()) {
                    val valueList =
                        if (i + MORE_MAX_COUNT <= list.size) {
                            list.slice(i until i + MORE_MAX_COUNT)
                        } else {
                            list.slice(i until list.size)
                        }
                    _shortsListMap[key] = valueList.toMutableList()
                }

                _shortsListMap[moreIndex.value]?.let {
                    _shortsList.value.addAll(it)
                }
            }

            // 로그 확인 코드 (선택사항)
            RLog.d(
                TAG,
                _shortsListMap.map { it }.joinToString { "key = ${it.key} , value ${(it.value).size}" },
            )
        }

        fun moreContent(index: Int): MutableList<MainShortsModel>? {
            _shortsListMap[index]?.let {
                _shortsList.value.addAll(it.toMutableList())
                return it.toMutableList()
            }

            return null
        }

        fun initData() {
            // 참조를 바꿔야 변경 가능
            // _shortsList.value.clear()
            _shortsList.value = mutableListOf()

            // 새로운 데이터를 추가
            setMorEVent(0)
            _shortsListMap[moreIndex.value]?.let {
                _shortsList.value = it.toMutableList() // 새로운 리스트로 할당
            }
        }

        fun maxMoreIndex(): Int = _shortsListMap.keys.size

        companion object {
            const val TAG = "MainVideoViewModel"
            private const val MORE_MAX_COUNT = 10
        }
    }
