package com.sean.ratel.android.ui.my

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.android.data.account.AccountSession
import com.sean.ratel.android.data.account.AccountUser
import com.sean.ratel.android.data.account.SignInResult
import com.sean.ratel.android.data.filter.CompiledFilter
import com.sean.ratel.android.data.filter.ExcludeFilter
import com.sean.ratel.android.data.filter.FilterLanguages
import com.sean.ratel.android.data.filter.FilterMerge
import com.sean.ratel.android.data.filter.FilterOptions
import com.sean.ratel.android.data.filter.FilterRepository
import com.sean.ratel.android.data.filter.FilterSignals
import com.sean.ratel.android.data.filter.FilterState
import com.sean.ratel.android.data.filter.FilterSyncRepository
import com.sean.ratel.android.data.filter.SyncResult
import com.sean.ratel.android.data.filter.TopicPackSet
import com.sean.ratel.android.data.library.LibrarySyncRepository
import com.sean.ratel.android.data.log.GAAccountAnalytics
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/** 화면에 한 번 알려 주는 결과(토스트). */
enum class AccountMessage {
    SIGNED_IN,
    NO_ACCOUNT,
    SIGN_IN_FAILED,
    SIGNED_OUT,
    ACCOUNT_DELETED,
    ACCOUNT_DELETE_FAILED,
    FILTER_SAVED,
    FILTER_SAVE_PENDING,
    NEED_SIGN_IN,
}

/** 편집 중인 필터. 저장하기 전까지는 기기에 저장된 것과 별개다. */
data class FilterDraft(
    val topics: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val blockedChannels: List<String> = emptyList(),
    val hideDisliked: Boolean = true,
    val hideWatched: Boolean = false,
    /** 빼지 않기(예외)로 둔 영상 id. */
    val exceptions: List<String> = emptyList(),
) {
    val hasRules: Boolean get() = topics.isNotEmpty() || keywords.isNotEmpty() || blockedChannels.isNotEmpty()
}

/** 마이 탭과 필터 편집 화면이 함께 쓴다(Activity 범위). */
@HiltViewModel
class AccountViewModel
    @Inject
    constructor(
        private val account: AccountSession,
        private val filterRepository: FilterRepository,
        private val filterSync: FilterSyncRepository,
        private val librarySync: LibrarySyncRepository,
        private val settingRepository: SettingRepository,
        private val gaLog: GALog,
    ) : ViewModel() {
        /** 로그인하지 않았을 때 보여주는 게스트 ID(설치마다 만들어지는 임의 값). */
        val guestId: StateFlow<String?> =
            settingRepository.userIdFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)

        /** 선택한 국가 코드(KR 등). 주제 추천이 어느 언어 단어를 쓸지 정한다. */
        val country: StateFlow<String?> =
            settingRepository.getLocale().stateIn(viewModelScope, SharingStarted.Eagerly, null)

        val user: StateFlow<AccountUser?> =
            account.user.stateIn(viewModelScope, SharingStarted.Eagerly, account.currentUser)

        val filterState: StateFlow<FilterState> =
            filterRepository.state.stateIn(viewModelScope, SharingStarted.Eagerly, FilterState())

        private val _topicPacks = MutableStateFlow(TopicPackSet())
        val topicPacks: StateFlow<TopicPackSet> = _topicPacks.asStateFlow()

        private val _draft = MutableStateFlow(FilterDraft())
        val draft: StateFlow<FilterDraft> = _draft.asStateFlow()

        private val _busy = MutableStateFlow(false)
        val busy: StateFlow<Boolean> = _busy.asStateFlow()

        private val _messages = MutableSharedFlow<AccountMessage>(extraBufferCapacity = MESSAGE_BUFFER)
        val messages: SharedFlow<AccountMessage> = _messages.asSharedFlow()

        init {
            viewModelScope.launch { _topicPacks.value = filterRepository.topicPackSet() }
        }

        fun signIn(
            activity: Activity?,
            source: String = SOURCE_MY,
        ) {
            if (activity == null || _busy.value) return
            viewModelScope.launch {
                _busy.value = true
                try {
                    handleSignIn(signInTracked(activity, source))
                } finally {
                    _busy.value = false
                }
            }
        }

        fun signOut() {
            viewModelScope.launch {
                // 올리지 못한 보관함 변경을 마지막으로 한 번 올려 본 뒤(실패해도 진행) 로그아웃하고, 기기의 보관함을 지운다
                withTimeoutOrNull(LIBRARY_FLUSH_TIMEOUT_MS) { librarySync.flushBeforeSignOut() }
                filterSync.signOut()
                librarySync.clearLocal()
                track(GAAccountAnalytics.Screen.MY, GAAccountAnalytics.Event.LOGOUT)
                _messages.emit(AccountMessage.SIGNED_OUT)
            }
        }

        fun deleteAccount() {
            if (_busy.value) return
            viewModelScope.launch {
                _busy.value = true
                try {
                    val result = filterSync.deleteAccount()
                    if (result is SyncResult.Success) librarySync.clearLocal()
                    track(
                        GAAccountAnalytics.Screen.MY,
                        GAAccountAnalytics.Event.ACCOUNT_DELETE,
                        if (result is SyncResult.Success) GAAccountAnalytics.Action.SUCCESS else GAAccountAnalytics.Action.FAIL,
                    )
                    _messages.emit(
                        if (result is SyncResult.Success) AccountMessage.ACCOUNT_DELETED else AccountMessage.ACCOUNT_DELETE_FAILED,
                    )
                } finally {
                    _busy.value = false
                }
            }
        }

        // ---- 필터 편집 ----

        /** 편집 화면에 들어올 때 — 저장된 필터로 초안을 만든다. 처음 만드는 사람은 싫어요 숨기기를 기본으로 켠다. */
        fun startEdit() {
            val state = filterState.value
            _draft.value =
                if (state.filter.hasRules) {
                    FilterDraft(
                        topics = state.filter.topics,
                        keywords = state.filter.keywords,
                        blockedChannels = state.filter.blockedChannels,
                        hideDisliked = state.options.hideDisliked,
                        hideWatched = state.options.hideWatched,
                        exceptions = state.filter.exceptions,
                    )
                } else {
                    FilterDraft(blockedChannels = state.filter.blockedChannels, exceptions = state.filter.exceptions)
                }
        }

        /** 미리보기에서 "빼지 않기"를 누르고 풀기. */
        fun toggleException(videoId: String) {
            if (videoId.isEmpty()) return
            _draft.value = _draft.value.let { it.copy(exceptions = it.exceptions.toggled(videoId)) }
        }

        /**
         * 초안을 지금 컴파일한다(온보딩 미리보기용). 싫어요·본 영상 신호는 보지 않는다 — 고른 주제·키워드가 무엇을 빼는지만 보여 준다.
         */
        fun compileDraft(draft: FilterDraft): CompiledFilter {
            val terms = _topicPacks.value.termsFor(draft.topics, FilterLanguages.forCountry(country.value))
            return CompiledFilter.compile(draft.toFilter(ExcludeFilter()), FilterOptions(), FilterSignals(), terms)
        }

        /**
         * "숨긴 영상" 화면에서 영상 하나를 빼지 않기(예외)로 두거나 되돌린다. 바로 저장한다(기기 → 서버).
         * 필터는 로그인해야 만들어지므로 로그인하지 않았으면 아무것도 하지 않는다.
         */
        fun setException(
            videoId: String,
            keep: Boolean,
        ) {
            if (videoId.isEmpty()) return
            viewModelScope.launch {
                val state = filterRepository.state.first()
                val current = state.filter.exceptions
                val next = if (keep) (current + videoId).distinct() else current - videoId
                if (next == current) return@launch
                val result = filterSync.saveFromUser(state.filter.copy(exceptions = next), state.options)
                if (result is SyncResult.Failure) _messages.emit(AccountMessage.FILTER_SAVE_PENDING)
            }
        }

        /** 홈 배너를 닫았는지(기본 false). */
        val homeBannerDismissed: StateFlow<Boolean> =
            filterRepository.homeBannerDismissed.stateIn(viewModelScope, SharingStarted.Eagerly, false)

        fun dismissHomeBanner() {
            track(GAAccountAnalytics.Screen.HOME, GAAccountAnalytics.Event.FILTER_BANNER_DISMISS)
            viewModelScope.launch { filterRepository.dismissHomeBanner() }
        }

        fun markOnboardingSeen() {
            viewModelScope.launch { filterRepository.markOnboardingSeen() }
        }

        fun toggleTopic(id: String) {
            _draft.value = _draft.value.let { it.copy(topics = it.topics.toggled(id)) }
        }

        fun addKeyword(raw: String) {
            val keyword = raw.trim()
            if (keyword.isEmpty()) return
            _draft.value =
                _draft.value.let { d ->
                    if (d.keywords.any { it.equals(keyword, ignoreCase = true) }) d else d.copy(keywords = d.keywords + keyword)
                }
        }

        fun removeKeyword(keyword: String) {
            _draft.value = _draft.value.let { it.copy(keywords = it.keywords - keyword) }
        }

        fun removeChannel(channelId: String) {
            _draft.value = _draft.value.let { it.copy(blockedChannels = it.blockedChannels - channelId) }
        }

        fun setHideDisliked(value: Boolean) {
            trackOption("hide_disliked", value)
            _draft.value = _draft.value.copy(hideDisliked = value)
        }

        fun setHideWatched(value: Boolean) {
            trackOption("hide_watched", value)
            _draft.value = _draft.value.copy(hideWatched = value)
        }

        /**
         * 초안을 저장한다. 로그인하지 않았으면 먼저 로그인한 뒤 — 서버에 이미 있는 내 필터와 **합쳐서** — 저장한다
         * (다른 기기에서 만든 필터를 덮어쓰지 않게).
         */
        fun saveDraft(
            activity: Activity?,
            source: String = SOURCE_EDIT,
        ) {
            if (_busy.value) return
            viewModelScope.launch {
                _busy.value = true
                try {
                    val draft = _draft.value
                    val options = FilterOptions(hideDisliked = draft.hideDisliked, hideWatched = draft.hideWatched)
                    if (account.currentUser == null) {
                        if (activity == null) return@launch
                        val signedIn = signInTracked(activity, source)
                        handleSignIn(signedIn)
                        if (signedIn !is SignInResult.Success) return@launch
                        // 서버의 내 필터를 먼저 받아서 합친다
                        filterSync.reconcile()
                        val existing = filterRepository.state.first().filter
                        val result = filterSync.saveFromUser(FilterMerge.union(existing, draft.toFilter(existing)), options)
                        trackFilterSave(draft, options, result, source)
                        report(result)
                    } else {
                        val existing = filterState.value.filter
                        val result = filterSync.saveFromUser(draft.toFilter(existing), options)
                        trackFilterSave(draft, options, result, source)
                        report(result)
                    }
                } finally {
                    _busy.value = false
                }
            }
        }

        /**
         * 재생 화면의 "관심없음" — 고른 채널·주제·키워드를 내 필터에 **더한다**(이미 있던 필터는 그대로).
         * 로그인하지 않았으면 먼저 로그인한 뒤 서버의 내 필터와 합쳐서 더한다. 처음 만드는 필터면 싫어요 숨기기를 켠다.
         * 기기에 저장되면(서버에 못 올려도) [onApplied] 를 부른다.
         */
        fun addToFilter(
            activity: Activity?,
            channelId: String?,
            topics: List<String>,
            keywords: List<String>,
            onApplied: () -> Unit = {},
        ) {
            if (_busy.value) return
            viewModelScope.launch {
                _busy.value = true
                try {
                    if (account.currentUser == null) {
                        if (activity == null) return@launch
                        val signedIn = signInTracked(activity, SOURCE_NOT_INTERESTED)
                        handleSignIn(signedIn)
                        if (signedIn !is SignInResult.Success) return@launch
                        filterSync.reconcile() // 서버에 이미 있는 내 필터를 먼저 받는다
                    }
                    val state = filterRepository.state.first()
                    val current = state.filter
                    val merged =
                        current.copy(
                            topics = current.topics + topics,
                            keywords = current.keywords + keywords,
                            blockedChannels = current.blockedChannels + listOfNotNull(channelId),
                        )
                    val options = if (current.hasRules) state.options else state.options.copy(hideDisliked = true)
                    val result = filterSync.saveFromUser(merged, options)
                    track(
                        GAAccountAnalytics.Screen.PLAYER,
                        GAAccountAnalytics.Event.NOT_INTERESTED_APPLY,
                        GAAccountAnalytics.Action.CLICK,
                        mapOf(
                            GAAccountAnalytics.Param.TOPIC_COUNT to topics.size,
                            GAAccountAnalytics.Param.KEYWORD_COUNT to keywords.size,
                            GAAccountAnalytics.Param.CHANNEL_BLOCKED to (channelId != null),
                        ),
                    )
                    report(result)
                    if (result !is SyncResult.NotSignedIn) onApplied()
                } finally {
                    _busy.value = false
                }
            }
        }

        /** 화면에서만 알 수 있는 일(온보딩 단계, 배너 노출 등)을 GA 로 보낸다. 개수·종류만 보내고 원문은 보내지 않는다. */
        fun track(
            screen: String,
            event: String,
            action: String = GAAccountAnalytics.Action.CLICK,
            params: Map<String, Any> = emptyMap(),
        ) {
            gaLog.sendEvent(screen, event, action, params)
        }

        private fun trackOption(
            option: String,
            value: Boolean,
        ) = track(
            GAAccountAnalytics.Screen.FILTER_EDIT,
            GAAccountAnalytics.Event.FILTER_OPTION_CHANGE,
            GAAccountAnalytics.Action.SELECT,
            mapOf(GAAccountAnalytics.Param.OPTION to option, GAAccountAnalytics.Param.VALUE to value),
        )

        private fun trackFilterSave(
            draft: FilterDraft,
            options: FilterOptions,
            result: SyncResult,
            source: String,
        ) = track(
            GAAccountAnalytics.Screen.FILTER_EDIT,
            GAAccountAnalytics.Event.FILTER_SAVE,
            if (result is SyncResult.Failure) GAAccountAnalytics.Action.FAIL else GAAccountAnalytics.Action.SUCCESS,
            mapOf(
                GAAccountAnalytics.Param.SOURCE to source,
                GAAccountAnalytics.Param.TOPIC_COUNT to draft.topics.size,
                GAAccountAnalytics.Param.KEYWORD_COUNT to draft.keywords.size,
                GAAccountAnalytics.Param.CHANNEL_COUNT to draft.blockedChannels.size,
                "hide_disliked" to options.hideDisliked,
                "hide_watched" to options.hideWatched,
            ),
        )

        /** 로그인을 시도하고 시작·성공·실패·취소를 GA 로 보낸다. */
        private suspend fun signInTracked(
            activity: Activity,
            source: String,
        ): SignInResult {
            val screen = GAAccountAnalytics.Screen.MY
            track(screen, GAAccountAnalytics.Event.LOGIN_START, params = mapOf(GAAccountAnalytics.Param.SOURCE to source))
            val result = account.signInWithGoogle(activity)
            when (result) {
                is SignInResult.Success -> track(screen, GAAccountAnalytics.Event.LOGIN_SUCCESS, GAAccountAnalytics.Action.SUCCESS)
                SignInResult.Cancelled -> track(screen, GAAccountAnalytics.Event.LOGIN_CANCEL)
                SignInResult.NoAccount ->
                    track(
                        screen,
                        GAAccountAnalytics.Event.LOGIN_FAIL,
                        GAAccountAnalytics.Action.FAIL,
                        mapOf(GAAccountAnalytics.Param.REASON to "no_account"),
                    )
                is SignInResult.Failure ->
                    track(
                        screen,
                        GAAccountAnalytics.Event.LOGIN_FAIL,
                        GAAccountAnalytics.Action.FAIL,
                        mapOf(GAAccountAnalytics.Param.REASON to "error"),
                    )
            }
            return result
        }

        private suspend fun handleSignIn(result: SignInResult) {
            _messages.emit(
                when (result) {
                    is SignInResult.Success -> AccountMessage.SIGNED_IN
                    SignInResult.Cancelled -> return
                    SignInResult.NoAccount -> AccountMessage.NO_ACCOUNT
                    is SignInResult.Failure -> AccountMessage.SIGN_IN_FAILED
                },
            )
        }

        private suspend fun report(result: SyncResult) {
            _messages.emit(
                when (result) {
                    SyncResult.Success -> AccountMessage.FILTER_SAVED
                    SyncResult.NotSignedIn -> AccountMessage.NEED_SIGN_IN
                    is SyncResult.Failure -> AccountMessage.FILTER_SAVE_PENDING
                },
            )
        }

        private fun FilterDraft.toFilter(existing: ExcludeFilter) =
            existing.copy(topics = topics, keywords = keywords, blockedChannels = blockedChannels, exceptions = exceptions)

        private fun List<String>.toggled(id: String) = if (id in this) this - id else this + id

        private companion object {
            const val MESSAGE_BUFFER = 4
            const val SOURCE_MY = "my"
            const val SOURCE_EDIT = "edit"
            const val SOURCE_NOT_INTERESTED = "not_interested"
            const val LIBRARY_FLUSH_TIMEOUT_MS = 5_000L
        }
    }
