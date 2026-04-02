package com.sean.ratel.android.ui.push

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.google.gson.Gson
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.android.permission.PermissionProvider
import com.sean.ratel.android.data.domain.model.push.PushModel
import com.sean.ratel.android.data.domain.model.push.toPushModel
import com.sean.ratel.android.data.local.pref.PushPreference
import com.sean.ratel.android.data.repository.PushNotificationManager
import com.sean.ratel.android.ui.home.setting.SettingsItems
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.Navigator
import com.sean.ratel.android.ui.push.item.PushUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import so.smartlab.common.push.PushSDK
import so.smartlab.common.push.fcm.data.domain.PushEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class PushViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        val navigator: Navigator,
        val imageLoader: ImageLoader,
        private val pushSDK: PushSDK,
        private val pushPreference: PushPreference,
        private val notificationManager: PushNotificationManager,
        private val permissionManager: PermissionProvider,
    ) : ViewModel() {
        val gson = Gson()
        private val _hasPermission = MutableStateFlow<Boolean>(permissionManager.has(permissionManager.requiredPermissions()))

        val hasPermission: StateFlow<Boolean> = _hasPermission

        private val _pushUiEvent =
            MutableSharedFlow<Boolean>(
                replay = 0,
                extraBufferCapacity = 10,
            )
        val pushUiEvent = _pushUiEvent.asSharedFlow()

        fun setOpenSettings(b: Boolean) {
            viewModelScope.launch {
                pushPreference.saveOpenSetting(b)
            }
        }

        val goOpenSetting =
            pushPreference.saveOpenSetting.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

        val fromPermissionPage: StateFlow<Boolean> =
            pushPreference.getFromPermissionPage().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

        fun setFromPermissionPage(fromPermissionPage: Boolean) {
            viewModelScope.launch {
                pushPreference.setFromPermissionPage(fromPermissionPage)
            }
        }

        init {

            viewModelScope.launch {
                pushSDK.events.collect { event ->
                    RLog.d("PUSH_TEST", "$event")
                    when (event) {
                        is PushEvent.RegisterResult -> {
                            pushPreference.saveToken(event.token)
                        }

                        is PushEvent.UnregisterResult -> {
                            pushPreference.saveToken(null)
                        }

                        is PushEvent.Message -> {
                            // 상단 벨 표시를 위해 이벤트
                            notificationManager.show(context, event.data)
                            pushPreference.saveNewPush(true)
                            updatePush(event.data)
                            _pushUiEvent.tryEmit(true)
                        }

                        is PushEvent.TokenUpdated -> {
                            pushPreference.saveToken(event.token)
                        }
                    }
                }
            }
        }

        private val _permissionState = MutableStateFlow<Boolean>(false)
        val permissionState: StateFlow<Boolean> = _permissionState

        fun setPermissionState(permission: Boolean) {
            _permissionState.value = permission
        }

        fun registerPush() {
            viewModelScope.launch {
                val currentToken = pushPreference.getSaveToken().first()

                RLog.d("SSKKKKKK", "push currentToken : $currentToken")
                if (currentToken == null) {
                    RLog.d("SSKKKKKK", "push registerPush  : registerPush")
                    pushSDK.register()
                }
            }
            notificationManager.createChannels(context)
        }

        fun unRegisterPush() {
            viewModelScope.launch {
                RLog.d("PUSH_TEST", "push unRegisterPush  : ${pushPreference.getSaveToken().first()}")
                pushSDK.unRegister(pushPreference.getSaveToken().first())
            }
            notificationManager.createChannels(context)
        }

        fun goNotificationPage() {
            navigator.navigateTo(Destination.Notifcation.route, false)

            viewModelScope.launch {
                pushPreference.saveNewPush(false)
            }
        }

        fun refreshPermission(item: SettingsItems?) {
            _hasPermission.value =
                permissionManager.has(permissionManager.requiredPermissions())

            if (item == null) {
                saveAppUpdatePush(_hasPermission.value)
                saveUploadPush(_hasPermission.value)
                saveRecommendPush(_hasPermission.value)
            }
        }

        fun setMainPermission(): Boolean {
            _hasPermission.value =
                permissionManager.has(permissionManager.requiredPermissions())
            return _hasPermission.value
        }

        fun refreshPermission() {
            _hasPermission.value =
                permissionManager.has(permissionManager.requiredPermissions())

            RLog.d("PUSH_TEST", "_hasPermission : ${_hasPermission.value}")

            saveAppUpdatePush(_hasPermission.value)
            saveUploadPush(_hasPermission.value)
            saveRecommendPush(_hasPermission.value)
        }

        fun requiredPermissions() = permissionManager.requiredPermissions()

        fun shouldShowRationale() = permissionManager.shouldShowRationale(permissionManager.requiredPermissions())

        fun openAppSettings() {
            permissionManager.openAppSettings()
        }

        val permissionDeniedCount: StateFlow<Int> =
            pushPreference.getPermissionDeniedCount().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0,
            )

        val requestedBefore: StateFlow<Boolean> =
            pushPreference.notificationRequestedBefore.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

        val appUpdatePush: StateFlow<Boolean> =
            pushPreference.getAppUpdatePush().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = true,
            )

        val uploadPush: StateFlow<Boolean> =
            pushPreference.getUploadPush().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = true,
            )

        val recommendPush: StateFlow<Boolean> =
            pushPreference.getRecommendPush().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = true,
            )

        fun setRequestedBefore(value: Boolean) {
            viewModelScope.launch {
                pushPreference.setNotificationRequestedBefore(value)
            }
        }

        fun saveAppUpdatePush(value: Boolean) {
            viewModelScope.launch {
                RLog.d("hbungshin", "saveAppUpdatePush : $value")
                pushPreference.saveAppUpdatePush(value)
            }
        }

        fun saveUploadPush(value: Boolean) {
            viewModelScope.launch {
                RLog.d("hbungshin", "saveUploadPush : $value")
                pushPreference.saveUploadPush(value)
            }
        }

        fun saveRecommendPush(value: Boolean) {
            viewModelScope.launch {
                RLog.d("hbungshin", "saveRecommendPush : $value")
                pushPreference.saveRecommendPush(value)
            }
        }

        fun onNotificationPermissionResult(granted: Boolean) {
            viewModelScope.launch {
                pushPreference.setNotificationRequestedBefore(true)

                if (granted) {
                    pushPreference.resetNotificationDeniedCount()
                } else {
                    pushPreference.incrementNotificationDeniedCount()
                }
                refreshPermission(SettingsItems.SERVICE_PUSH)
            }
        }

        val notificationPushList: StateFlow<List<PushModel>> =
            pushPreference
                .getPushList()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = emptyList(),
                )

        private val _hasLoadedOnce = MutableStateFlow(false)
        val hasLoadedOnce: StateFlow<Boolean> = _hasLoadedOnce

        @OptIn(FlowPreview::class)
        val notificationPushUiList: StateFlow<List<PushUiItem>> =
            notificationPushList
                // 기다려다가 최종값만 받음 ->연속적으로 값이 올때 100 기다렸다가 리턴
                .debounce(100)
                .onEach {
                    _hasLoadedOnce.value = true
                }.map { pushList ->
                    fun Long.toLocalDate(): LocalDate =
                        Instant
                            .ofEpochMilli(this)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                    buildList {
                        var lastDate: LocalDate? = null

                        pushList.forEach { push ->
                            val currentDate = push.createAt.toLocalDate()

                            if (lastDate != currentDate) {
                                add(PushUiItem.DateHeader(currentDate))
                                lastDate = currentDate
                            }

                            add(PushUiItem.Content(push))
                        }
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = emptyList(),
                )

        val hasNewPush: StateFlow<Boolean> =
            pushPreference
                .getNewPush()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = false,
                )

        fun updatePush(data: Map<String, String>) {
            viewModelScope.launch {
                val currentList = pushPreference.getPushList().first()
                val pushModel = data.toPushModel()

                val existing =
                    currentList.find {
                        it.id == pushModel.id
                    }

                // 완전히 같은 값이면 아무것도 안 함
                if (existing == pushModel) {
                    return@launch
                }

                val filteredList =
                    currentList.filterNot {
                        it.type == pushModel.type &&
                            it.linkUrl == pushModel.linkUrl
                    }
                // 200 개 limit
                val updatedList = listOf(pushModel) + currentList.take(199)

                pushPreference.updatePush(updatedList)
            }
        }

        fun saveNewPush() {
            viewModelScope.launch {
                RLog.d("KKKMMMMMMM", "saveNewPush")
                pushPreference.saveNewPush(false)
            }
        }

        fun deleteNotification(target: PushModel) {
            viewModelScope.launch {
                pushPreference.deletePushItem(target)
            }
        }

        fun updateReadFlag(
            id: String,
            isRead: Boolean,
        ) {
            viewModelScope.launch {
                pushPreference.updateReadFlag(id, isRead)
            }
        }
    }
