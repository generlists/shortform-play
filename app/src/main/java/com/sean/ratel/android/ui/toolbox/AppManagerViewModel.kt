package com.sean.ratel.android.ui.toolbox

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.android.data.android.toolbox.PhoneAppProvider
import com.sean.ratel.android.data.domain.model.toolbox.AppManagerInfo
import com.sean.ratel.android.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class
AppManagerViewModel
    @Inject
    constructor(
        val navigator: Navigator,
        private val phoneAppProvider: PhoneAppProvider,
        @ApplicationContext val context: Context,
    ) : ViewModel() {
        private val _contents = mutableStateListOf<AppManagerInfo>()
        val contents: List<AppManagerInfo> = _contents

        init {
            viewModelScope.launch {
                phoneAppList(context)
            }
        }

        private suspend fun phoneAppList(context: Context) {
            // I/O 작업이나 네트워크 요청 시 주로 Dispatchers.IO 사용
            phoneAppProvider.fetchInstalledApps(context).collect {
                _contents.addAll(it)
                _contents.sortedBy { it.lastUpdateTime }
            }
        }

        suspend fun appDelete(
            deleteAppLauncher: ActivityResultLauncher<Intent>,
            packageName: String,
        ) {
            phoneAppProvider.removeInstalledApp(deleteAppLauncher, packageName)
        }

        suspend fun appDeleteUpdataContent(packageName: String?) {
            withContext(Dispatchers.IO) {
                packageName.let { _contents.removeIf { it.packageName == packageName } }
            }
        }

        suspend fun phoneAppListOrder(filter: Int) {
            withContext(Dispatchers.IO) {
                val sortedList =
                    when (filter) {
                        0 -> {
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            _contents.sortedByDescending { LocalDate.parse(it.lastUpdateTime, formatter) }
                        }

                        1 -> _contents.sortedByDescending { (it.appTitle) }
                        2 -> _contents.sortedByDescending { (it.apkSize).toDouble() }
                        else -> _contents.sortedBy { it.lastUpdateTime }
                    }
                // 전체로 바꾸는 것은 compose 가 안되서 지우고 다시 추가
                _contents.clear()
                _contents.addAll(sortedList)
            }
        }

        fun goStore(
            context: Context,
            updateUrl: String,
            appName: String,
        ) {
            phoneAppProvider.goStore(context, updateUrl, appName)
        }

        fun goDetailAppInfo(
            context: Context,
            packageName: String,
        ) {
            phoneAppProvider.goDetailAppInfo(context, packageName)
        }

        companion object {
            val TAG = "AppManagerViewModel"
        }
    }
