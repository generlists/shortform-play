package com.sean.ratel.android.ui.end

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.RemoteConfig.END_AD_POSITION
import com.sean.ratel.android.data.common.RemoteConfig.getRemoteConfigIntValue
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.databinding.YoutubeVideoEndBinding
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.interstitialAd
import com.sean.ratel.android.ui.common.ShortFormCommonAlertDialog
import com.sean.ratel.android.ui.common.UpdateStateBar
import com.sean.ratel.android.ui.pip.PIPViewModel
import com.sean.ratel.android.ui.pip.PipResult
import com.sean.ratel.android.ui.theme.Background_op_20
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.NetworkUtil
import com.sean.ratel.android.utils.TimeUtil.formatTimeFromFloat
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerAdapterImpl
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerImpl
import com.sean.ratel.player.core.data.player.youtube.adaptor.YouTubeStreamPlayerAdapter
import com.sean.ratel.player.core.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlayerError
import com.sean.ratel.player.core.util.launch
import com.sean.ratel.player.core.util.repeatOnStart
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 레거시 코드 라이브러리를 래핑하여 쓰는데 compose 한계(AndroidView) 로 인해서 Fragment 로 End 는 개발하고
 * 내부적으로 부분적 compose 적용 예정
 */
@AndroidEntryPoint
@Suppress("ktlint:standard:property-naming")
class YouTubeEndFragment(
    val pager: ViewPager2?,
) : Fragment() {
    private var _binding: YoutubeVideoEndBinding? = null

    private val binding get() = _binding!!

    lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var youTubeStreamPlayer: YouTubeStreamPlayer
    private lateinit var youtubeStreamPlayerAdapter: YouTubeStreamPlayerAdapter

    private var mainShortsModel: MainShortsModel? = null
    private var createPosition = 0

    //    private var selectedPosition = 0
    private var totalSize = 0

    // hilt 로 주입했을경우 아래처럼 선천해야함
    private val youtubeContentEndViewModel: YouTubeContentEndViewModel by viewModels()
    private val pipViewModel: PIPViewModel by viewModels()

    // 최근본 영상 저장 하기위
    lateinit var mainViewModel: MainViewModel // lateinit으로 선언

    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var iFramePlayerOptions: IFramePlayerOptions

    @Inject
    lateinit var youtubeStreamPlayerTracker: YouTubePlayerTracker

    constructor() : this(null)

    init {
        repeatOnStart {
            combine(
                youTubeStreamPlayer.playbackState,
                mainViewModel.currentSelection,
            ) { playbackState, currentSelection ->

                Pair(playbackState, currentSelection)
            }.collect { combinedResult ->
                val (state, currentSelection) = combinedResult
                when (state) {

                    is YouTubeStreamPlaybackState.Prepared ->
                        handlePreparedState(
                            currentSelection,
                            state,
                        )

                    YouTubeStreamPlaybackState.UnStarted -> handleUnstartedState(currentSelection)
                    YouTubeStreamPlaybackState.Buffering -> handleBufferingState(currentSelection)
                    YouTubeStreamPlaybackState.Paused -> handlePausedState(currentSelection)
                    YouTubeStreamPlaybackState.Playing -> handlePlayingState(currentSelection)
                    YouTubeStreamPlaybackState.Ended -> handleEndedState(currentSelection)
                    else -> Unit
                }
            }
        }

        repeatOnStart {

            youTubeStreamPlayer.currentTime.collect { currentTime ->
                if (currentTime > 0f) {
                    mainViewModel.recentVideo(mainShortsModel, currentTime)
                }
            }
        }
    }

    fun onClickPipButton() {
        val videoSize = youTubeStreamPlayer.getVideoSize()
        val isPlaying = youTubeStreamPlayer.isPlaying()
        val visibleRect = Rect()
        binding.root.getGlobalVisibleRect(visibleRect)

        val enterPipMode =
            pipViewModel.enterPipMode(
                requireActivity(),
                videoSize,
                visibleRect,
                isPlaying = isPlaying,
            )

        when (enterPipMode) {
            PipResult.NoSystemFeature ->
                Toast
                    .makeText(
                        requireActivity(),
                        requireActivity().getString(R.string.pip_memory_error),
                        Toast.LENGTH_LONG,
                    ).show()

            PipResult.NoPermission -> youtubeContentEndViewModel.runSettingView()
            else -> Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onPause() {
        super.onPause()

        launch {
            mainViewModel.pipClick.collect { pipClick ->
                if (!pipClick.first) {
                    youTubeStreamPlayer.pause()
                    youTubeStreamPlayer.setMute(true)
                }
            }
        }

        RLog.d(TAG, "onPause $createPosition , $youTubeStreamPlayer")
    }

    override fun onResume() {
        super.onResume()
        mainShortsModel?.shortsVideoModel?.videoId?.let {
            launch {
                mainViewModel.pipClick.collect {
                    if (!it.first) {
                        adRequest()
                    }
                }
            }
            launch {
                if (!youtubeContentEndViewModel.getAutoPlay()) {
                    youTubeStreamPlayer.pause()
                }
            }
        }

        RLog.d(TAG, "onResume $createPosition")
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            mainViewModel.setRecentVideo()
        }
    }

    private fun adRequest() {
        launch {
            mainViewModel.currentSelection
                .distinctUntilChanged()
                .filter { it == createPosition }
                .collect { selection ->
                    if (selection > 0 &&
                        totalSize > getRemoteConfigIntValue(END_AD_POSITION) &&
                        selection % getRemoteConfigIntValue(END_AD_POSITION) == 0
                    ) {
                        youTubeStreamPlayer.pause()

                        if (selection == createPosition) {
                            // Ad 시작 로직
                            youtubeContentEndViewModel.setAdLoading(loading = true)
                            interstitialAd(requireActivity(), adViewModel = adViewModel)
                            // Interstitial Ad 실행 및 상태 관찰
                            combine(
                                adViewModel.interstitialAdComplete,
                                adViewModel.interstitialAdFail,
                            ) { interstitialAdComplete, interstitialAdFail ->
                                Pair(interstitialAdComplete, interstitialAdFail)
                            }.collect { combinedResult ->
                                combinedResult.first?.let {
                                    if (!it) {
                                        adViewModel.showInterstitialAds(context = requireActivity())
                                    } else {
                                        youtubeContentEndViewModel.setAdLoading(loading = false)
                                        youTubeStreamPlayer.start()
                                    }
                                } ?: run {
                                    // 광고 실패
                                    youtubeContentEndViewModel.setAdLoading(loading = false)
                                    youTubeStreamPlayer.start()
                                }
                            }
                        }
                    } else {
                        youTubeStreamPlayer.seekTo(0f)
                        youTubeStreamPlayer.start()
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RLog.d(TAG, "onDestroy $createPosition ,  $youTubeStreamPlayer")
        youTubeStreamPlayer.release()
    }

    override fun onDestroyView() {
        binding.playContainer.removeAllViews()
        super.onDestroyView()

        RLog.d(TAG, "onDestroyView $createPosition ,  $youTubeStreamPlayer")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mainShortsModel =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getParcelable(ARG_SHORTS_DATA, MainShortsModel::class.java)
            } else {
                @Suppress("DEPRECATION")
                arguments?.getParcelable(ARG_SHORTS_DATA)
            }

        createPosition = arguments?.getInt(ARG_POSITION) ?: 0
        totalSize = arguments?.getInt(ARG_TOTAL_SIZE) ?: 0

        _binding = YoutubeVideoEndBinding.inflate(inflater, container, false)

        youTubePlayerView =
            YouTubePlayerView(requireActivity()).apply {
                layoutParams =
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    )
                enableAutomaticInitialization = false
            }
        val view = youTubePlayerView

        // 기존 부모가 있는 경우 제거 후 재추가
        _binding?.playContainer?.removeAllViews()

        _binding?.playContainer?.addView(view)

        youtubeStreamPlayerAdapter = YouTubeStreamPlayerAdapterImpl(youTubePlayerView)

        youTubeStreamPlayer =
            YouTubeStreamPlayerImpl(
                lifecycle,
                autoPlay = false,
                youtubeStreamPlayerAdapter,
                iFramePlayerOptions,
                youtubeStreamPlayerTracker,
            )

        lifecycle.addObserver(youTubePlayerView)

        youTubeStreamPlayer.initPlayer(networkHandle = false, videoId = mainShortsModel?.shortsVideoModel?.videoId)
        youTubePlayerView.matchParent()

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val composeView =
            youTubePlayerView.rootView?.findViewById<ComposeView>(R.id.player_controller)
        // controller
        composeView?.setContent {
            val pipButtonClick = remember { mutableStateOf(false) }
            val currentSelection = remember { mutableStateOf(0) }
            launch {
                combine(
                    mainViewModel.pipClick,
                    mainViewModel.currentSelection,
                ) { pipClick, currentSelection ->
                    Pair(pipClick, currentSelection)
                }.collect { combinedResult ->
                    pipButtonClick.value = combinedResult.first.first
                    currentSelection.value = combinedResult.second
                }
            }
            RLog.d(
                TAG,
                "pipButtonClick : ${pipButtonClick.value} , createPosition : $createPosition",
            )

            if (currentSelection.value == createPosition) {
                if (!pipButtonClick.value) {
                    UpdateStateBar()
                    PlayControllerView(mainShortsModel)
                }
                IsNetWorkAvailAble()
                WifiAlertDiaLog()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                youTubeStreamPlayer.playbackError
                    .combine(mainViewModel.currentSelection) { error, selection ->
                        error to selection
                    }.collect { (error, selection) ->
                        RLog.d(
                            TAG,
                            "Error received: $error , selection : $selection , createPosition : $createPosition",
                        )
                        if (selection == createPosition && error != YouTubeStreamPlayerError.UNKNOWN) {
                            Toast.makeText(requireActivity(), "$error", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

    fun pipButtonState() {
        launch {
            mainViewModel.pipClick.collect {
                val visibleRect = Rect()
                binding.root.getGlobalVisibleRect(visibleRect)

                pipViewModel.updatePipParams(
                    requireActivity(),
                    youTubeStreamPlayer.isPlaying(),
                    youTubeStreamPlayer.getVideoSize(),
                    rect = visibleRect,
                )
            }
        }
    }

    fun pause() {
        youTubeStreamPlayer.pause()
    }

    fun play() {
        youTubeStreamPlayer.start()
    }

    fun release() {
        youTubeStreamPlayer.release()
    }

    @Suppress("ktlint:standard:function-naming")
    @Composable
    fun WifiAlertDiaLog() {
        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(NetworkUtil.getNetworkInfo(context).networkType != "wifi") }
        var settingWifiOnly by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(settingWifiOnly) {
            settingWifiOnly = youtubeContentEndViewModel.getWifiOnlyPlay()
        }

        if (NetworkUtil.isNetworkAvailable(context) && showDialog && settingWifiOnly) {
            ShortFormCommonAlertDialog(
                onDismiss = { buttonClick ->
                    if (buttonClick) {
                        coroutineScope.launch {
                            youtubeContentEndViewModel.setWifiOnlyPlay()
                            youTubeStreamPlayer.start()
                        }
                    } else {
                        youtubeContentEndViewModel.back()
                    }
                    showDialog = false
                },
                stringResource(R.string.alert_wifi_body),
                stringResource(R.string.alert_ok),
                stringResource(R.string.alert_cancel),
            )
        }
    }

    @Suppress("ktlint:standard:function-naming")
    @Composable
    fun IsNetWorkAvailAble() {
        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(!NetworkUtil.isNetworkAvailable(context)) }

        if (showDialog) {
            ShortFormCommonAlertDialog(
                onDismiss = { buttonClick ->
                    if (buttonClick) {
                        youtubeContentEndViewModel.back()
                    }
                    showDialog = false
                },
                stringResource(R.string.alert_no_network),
                stringResource(R.string.alert_ok),
            )
        }
    }

    fun updateSelectPosition() {
        adViewModel.setInterstitialAdComplete(false)
    }

    private suspend fun handlePreparedState(
        selectPosition: Int,
        state: YouTubeStreamPlaybackState.Prepared,
    ) {
        youTubeStreamPlayer = state.youTubeStreamPlayer
        mainShortsModel?.shortsVideoModel?.videoId?.let { videoId ->
            youTubeStreamPlayer.loadVideo(videoId, 0f)
            if (selectPosition == createPosition) {
                if (!youtubeContentEndViewModel.getAutoPlay()) {
                    youTubeStreamPlayer.pause()
                }
            } else {
                youTubeStreamPlayer.pause()
            }
        }
    }

    private fun handleUnstartedState(selectPosition: Int) {
        if (selectPosition == createPosition && !youTubeStreamPlayer.isPlaying()) {
            youTubeStreamPlayer.start()
        }
    }

    private fun handleBufferingState(selectedPosition: Int) {
        if (selectedPosition == createPosition) {
            // Log.d("anatol","handleBufferingState loading = true")
            youtubeContentEndViewModel.setLoading(loading = true)
        }
    }

    private fun handlePausedState(selectPosition: Int) {
        if (selectPosition == createPosition) {
            youtubeContentEndViewModel.setPlaying(isPlaying = false)
            mainViewModel.setPIPButtonClickState(true)
        }
    }

    private suspend fun handlePlayingState(selectPosition: Int) {
        if (selectPosition == createPosition) {
            youtubeContentEndViewModel.setLoading(loading = false)
            youtubeContentEndViewModel.setPlaying(isPlaying = true)
            mainViewModel.setPIPButtonClickState(true)

            if (NetworkUtil.getNetworkInfo(requireContext()).networkType != "wifi" && youtubeContentEndViewModel.getWifiOnlyPlay()) {
                youTubeStreamPlayer.pause()
            }
        }

        launch {
            mainViewModel.currentSelection
                .filter { it == createPosition }
                .distinctUntilChanged()
                .collect { selection ->
                    val isSoundOff = youtubeContentEndViewModel.getSoundOff()

                    if (selection == createPosition) {
                        RLog.d("hbungshin", "selection : $selection , isSoundOff  $isSoundOff")
                        delay(500)
                        youTubeStreamPlayer.setMute(!isSoundOff)
                    }
                }
        }
    }

    private suspend fun handleEndedState(selectPosition: Int) {
        if (selectPosition == createPosition && youtubeContentEndViewModel.getLoopPlay()) {
            youTubeStreamPlayer.seekTo(0f)
            youTubeStreamPlayer.start()
        }
    }

    @Suppress("ktlint:standard:function-naming")
    @Composable
    fun PlayControllerView(mainShortsModel: MainShortsModel?) {
        val isLoading by remember { youtubeContentEndViewModel.isLoading }
        val isAdLoading by remember { youtubeContentEndViewModel.isAdLoading }
        val isPlaying by remember { youtubeContentEndViewModel.isPlaying }
        val topBarHeight = mainViewModel.topBarHeight.collectAsState(53)
        Scaffold(
            topBar = {},
            containerColor = Color.Transparent,
        ) { innerPadding ->

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = topBarHeight.value.dp)
                    .clickable {
                        if (youTubeStreamPlayer.isPlaying()) youTubeStreamPlayer.pause() else youTubeStreamPlayer.start()
                    }.padding(innerPadding),
            ) {
                // 채널, 영상타이틀 오른쪽 좋아요,싫어요,댓글,공유
                // 맨하단 시크바
                Column(
                    Modifier
                        .wrapContentSize()
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BottomContentsArea(mainShortsModel)
                }
            }
        }

        LoadingArea(isLoading)
        if (!isAdLoading) {
            PlayButton(isPlaying, onPlayChange = { if (it) youTubeStreamPlayer.start() })
        }
    }

    @Suppress("ktlint:standard:function-naming")
    @Composable
    fun BottomContentsArea(mainShortsModel: MainShortsModel?) {
        Box(
            Modifier
                .fillMaxSize(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomEnd),
            ) {
                RightContentArea(
                    youtubeContentEndViewModel,
                    mainShortsModel,
                    onSoundChange = { sound ->
                        youTubeStreamPlayer.setMute(!sound)
                    },
                )
                EndBottomContents(mainShortsModel)
                BottomSeekBarArea()
            }
        }
    }

    @Suppress("ktlint:standard:function-naming")
    @Composable
    fun BottomSeekBarArea() {
        val duration by youTubeStreamPlayer.duration.collectAsState()
        val currentTime by youTubeStreamPlayer.currentTime.collectAsState()
        val progressRate = currentTime / duration // kotlin.math.ceil((currentTime / duration))
        var progress by remember { mutableFloatStateOf(0.0f) } // 초기값 30%
        LaunchedEffect(progressRate) {
            progress = progressRate
        }
        TimeArea()
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            BottomSeekBar(
                progress = progress,
                onSeekChanged = { newProgress ->

                    progress = newProgress
                },
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )
        }
    }

    @Suppress("ktlint:standard:function-naming")
    @Composable
    fun TimeArea() {
        val duration by youTubeStreamPlayer.duration.collectAsState()
        val currentTime by youTubeStreamPlayer.currentTime.collectAsState()

        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 10.dp)
                .background(Background_op_20),
        ) {
            Text(
                text = if (currentTime > 0.0f) formatTimeFromFloat(currentTime) else "",
                Modifier
                    .wrapContentSize()
                    .padding(start = 15.dp, top = 5.dp),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (LocalInspectionMode.current) Color.Black else Color.White,
            )
            Text(
                text = if (currentTime > 0.0f) "/" else "",
                Modifier
                    .wrapContentSize()
                    .padding(start = 5.dp, top = 5.dp),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (LocalInspectionMode.current) Color.Black else Color.White,
            )
            Text(
                text = if (currentTime > 0.0f) formatTimeFromFloat(duration) else "",
                Modifier
                    .wrapContentSize()
                    .padding(start = 5.dp, top = 5.dp),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (LocalInspectionMode.current) Color.Black else Color.White,
            )
        }
    }

    companion object {
        private const val ARG_POSITION = "arg_position"
        private const val ARG_TOTAL_SIZE = "arg_total_size"
        private const val ARG_SHORTS_DATA = "arg_shorts_Data"
        private const val TAG = "YouTubeEndFragment"

        fun newInstance(
            viewPager: ViewPager2?,
            position: Int,
            totalSize: Int,
            mainShortsModel: MainShortsModel,
        ): YouTubeEndFragment {
            val fragment = YouTubeEndFragment(viewPager)
            val args =
                Bundle().apply {
                    putInt(ARG_POSITION, position)
                    putInt(ARG_TOTAL_SIZE, totalSize)
                    putParcelable(ARG_SHORTS_DATA, mainShortsModel)
                }
            fragment.arguments = args
            return fragment
        }
    }

    @Suppress("ktlint:standard:function-naming")
    @Preview(showBackground = true)
    @Composable
    private fun PlayControllerPreView() {
        RatelappTheme {
            PlayControllerView(null)
        }
    }

    @Suppress("ktlint:standard:function-naming")
    @Preview(showBackground = true)
    @Composable
    private fun TimeAreaPreView() {
        RatelappTheme {
            TimeArea()
        }
    }
}
