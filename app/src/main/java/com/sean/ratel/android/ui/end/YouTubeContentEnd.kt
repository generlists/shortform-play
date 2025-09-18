package com.sean.ratel.android.ui.end

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.ui.end.YouTubeContentEndViewModel.PageScrollState
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.utils.UIUtil.findCurrentFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "YouTubeContentEnd"

@Suppress("ktlint:standard:function-naming")
@Composable
fun YouTubeContentEnd(
    mainViewModel: MainViewModel,
    youTubeContentEndViewModel: YouTubeContentEndViewModel,
) {
    val selectedIndex by remember { mainViewModel.selectedIndex }
    val itemClicked = mainViewModel.itemClicked.value
    LaunchedEffect(Unit) {
        youTubeContentEndViewModel.mainShortsData(mainViewModel.mainShorts.value)
        youTubeContentEndViewModel.mainTrendShortsData(mainViewModel.mainTrendShortsList.value)
        youTubeContentEndViewModel.moreTrendShortsData(
            mainViewModel.trendsShorts.value.event_list.values
                .flatMap { it },
        )

        youTubeContentEndViewModel.shortFormVideoData(mainViewModel.shortFormVideoList.value)
        youTubeContentEndViewModel.setRecentlyWatchData(mainViewModel.watchVideoList.value)

        if (itemClicked == Destination.Home.Main.route ||
            itemClicked == Destination.Home.Main.PoplarShortFormMore.route ||
            itemClicked == Destination.Home.Main.EditorPickMore.route ||
            itemClicked == Destination.Home.Main.RecommendMore.route ||
            itemClicked == Destination.Home.Main.RankingChannelMore.route ||
            itemClicked == Destination.Home.Main.RankingSubscriptionMore.route ||
            itemClicked == Destination.Home.Main.RankingSubscriptionUpMore.route ||
            itemClicked == Destination.Home.Main.RecentlyWatchMore.route ||
            itemClicked == Destination.Home.Main.TrendShortsMore.route
        ) {
            when (mainViewModel.viewType.value) {
                ViewType.ImageFlow -> youTubeContentEndViewModel.setImageFlowData()
                ViewType.PopularSearchShortForm,
                ViewType.PopularLikeShortForm,
                ViewType.PopularCommentShortForm,
                ->
                    youTubeContentEndViewModel.setPopularShortFormData(
                        mainViewModel.viewType.value,
                        selectedIndex,
                    )

                ViewType.EditorPick -> youTubeContentEndViewModel.setEditorPickData(selectedIndex)
                ViewType.Recommend -> youTubeContentEndViewModel.setRecommendData(selectedIndex)
                ViewType.ChannelSearchRanking,
                ViewType.ChannelLikeRanking,
                ->
                    youTubeContentEndViewModel.setChannelRankingData(
                        mainViewModel.viewType.value,
                        selectedIndex,
                    )

                ViewType.SubscriptionRanking ->
                    youTubeContentEndViewModel.setSubscriptionRankingData(
                        selectedIndex,
                    )

                ViewType.SubscriptionRankingUp ->
                    youTubeContentEndViewModel.setSubscriptionRankingUpData(
                        selectedIndex,
                    )

                ViewType.RecentlyWatch -> {
                    youTubeContentEndViewModel.setWatchData(selectedIndex)
                }
                ViewType.MainTrendShorts -> {
                    youTubeContentEndViewModel.setMainTrendShortsData(selectedIndex)
                }
                ViewType.TrendShortsMore -> {
                    youTubeContentEndViewModel.setMoreTendShortsData(selectedIndex)
                }

                else -> Unit
            }
        } else if (mainViewModel.itemClicked.value == Destination.Home.ShortForm.route) {
            youTubeContentEndViewModel.setShortFormVideoData(selectedIndex)
        }
    }

    DisplayUI(youTubeContentEndViewModel, mainViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun DisplayUI(
    youTubeContentEndViewModel: YouTubeContentEndViewModel,
    mainViewModel: MainViewModel,
) {
    val popularShorFormList by youTubeContentEndViewModel.popularShortsFormList.collectAsState()
    val imageFlowList by youTubeContentEndViewModel.imageFlowShortsList.collectAsState()
    val editorPickList by youTubeContentEndViewModel.editorPickList.collectAsState()
    val channelRankingList by youTubeContentEndViewModel.channelRankingList.collectAsState()
    val subscriptionRankingList by youTubeContentEndViewModel.subscriptionRankingList.collectAsState()
    val subscriptionRankingUpList by youTubeContentEndViewModel.subscriptionRankingUpList.collectAsState()
    val recommendList by youTubeContentEndViewModel.recommendShortsList.collectAsState()
    val categoryShortFromVideo by youTubeContentEndViewModel.shortFormVideoList.collectAsState()
    val watchList by youTubeContentEndViewModel.watchList.collectAsState()
    val mainTrendsShorts by youTubeContentEndViewModel.mainTrendShortsList.collectAsState()
    val moreTrendsShorts by youTubeContentEndViewModel.moreTrendShortsList.collectAsState()
    val activity = LocalContext.current as FragmentActivity

    if ((
            popularShorFormList?.isNotEmpty() == true ||
                imageFlowList?.isNotEmpty() == true ||
                editorPickList?.isNotEmpty() == true ||
                mainTrendsShorts?.isNotEmpty() == true ||
                moreTrendsShorts?.isNotEmpty() == true

        ) ||
        channelRankingList.isNotEmpty() ||
        subscriptionRankingList.isNotEmpty() ||
        subscriptionRankingUpList.isNotEmpty() ||
        recommendList.isNotEmpty() ||
        categoryShortFromVideo.isNotEmpty() ||
        watchList.isNotEmpty()
    ) {
        val endList =
            getEndData(mainViewModel.viewType.collectAsState().value, youTubeContentEndViewModel)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
        ) {
            endList?.let {
                FragmentViewPagerWithData(
                    activity,
                    mainViewModel,
                    it,
                )
            }
        }
        mainViewModel.setPIPButtonClickState(false)
        mainViewModel.setItemClicked(null, 0)
    } else {
        RLog.e(TAG, "No data available in mainShortsList")
    }
}

private fun getEndData(
    viewType: ViewType,
    youTubeContentEndViewModel: YouTubeContentEndViewModel,
): List<MainShortsModel>? =
    when (viewType) {
        ViewType.ImageFlow -> youTubeContentEndViewModel.imageFlowShortsList.value
        ViewType.PopularSearchShortForm,
        ViewType.PopularLikeShortForm,
        ViewType.PopularCommentShortForm,
        ->
            youTubeContentEndViewModel.popularShortsFormList.value

        ViewType.EditorPick -> youTubeContentEndViewModel.editorPickList.value
        ViewType.Recommend -> youTubeContentEndViewModel.recommendShortsList.value
        ViewType.ChannelSearchRanking, ViewType.ChannelLikeRanking -> youTubeContentEndViewModel.channelRankingList.value
        ViewType.SubscriptionRanking -> youTubeContentEndViewModel.subscriptionRankingList.value
        ViewType.SubscriptionRankingUp -> youTubeContentEndViewModel.subscriptionRankingUpList.value
        ViewType.ShortFormVideo -> youTubeContentEndViewModel.shortFormVideoList.value
        ViewType.RecentlyWatch -> youTubeContentEndViewModel.watchList.value
        ViewType.MainTrendShorts -> youTubeContentEndViewModel.mainTrendShortsList.value
        ViewType.TrendShortsMore -> youTubeContentEndViewModel.moreTrendShortsList.value
        else -> null
    }

@Suppress("ktlint:standard:function-naming")
@Composable
fun FragmentViewPagerWithData(
    fragmentActivity: FragmentActivity,
    mainViewModel: MainViewModel,
    // 데이터를 동적으로 전달할 리스트
    contentList: List<MainShortsModel>,
) {
    // implementation("androidx.compose.foundation:foundation:1.5.0") 뷰페이저는 인덱스를 잘못가지고 와서 아직 안정성이 떨어짐
    // 좀더 딥하게 파파야함.
    FragmentContainer(fragmentActivity, mainViewModel, contentList)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun FragmentContainer(
    fragmentActivity: FragmentActivity,
    mainViewModel: MainViewModel,
    contentList: List<MainShortsModel>,
) {
    val context = LocalContext.current
    val corutineScope = rememberCoroutineScope()
    val endBackButtonAction by mainViewModel.endBack.collectAsState()
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val viewPager =
        remember {
            ViewPager2(context).apply {
                adapter =
                    YouTubeFragmentStateAdapter(
                        fragmentActivity,
                        this,
                        contentList,
                    ) // 데이터 기반 어댑터 설정
                setOffscreenPageLimit(1)
                orientation = ViewPager2.ORIENTATION_VERTICAL

                // 페이지 스크롤 상태, 페이지 선택 등의 이벤트 처리
                registerOnPageChangeCallback(
                    object : ViewPager2.OnPageChangeCallback() {
                        var previousPosition = 0
                        var scrollState = PageScrollState.IDLE

                        override fun onPageScrolled(
                            position: Int,
                            positionOffset: Float,
                            positionOffsetPixels: Int,
                        ) {
                            if (position > previousPosition) {
                                // 아래로 스크롤 중
                                RLog.d(
                                    TAG,
                                    "Scrolling Downwards $position previousPosition : $previousPosition $position",
                                )
                                scrollState = PageScrollState.SCROLL_DOWNWARDS
                            } else if (position < previousPosition) {
                                // 위로 스크롤 중
                                RLog.d(TAG, "Scrolling Upwards $position")
                                scrollState = PageScrollState.SCROLL_UPWARDS
                            }
                            previousPosition = position
                        }

                        override fun onPageSelected(position: Int) {
                            RLog.d(TAG, "position : $position")
                            mainViewModel.setCurrentSelection(position)
                        }

                        override fun onPageScrollStateChanged(state: Int) {
                            RLog.d(TAG, "onPageScrollStateChanged")
                            when (state) {
                                ViewPager2.SCROLL_STATE_IDLE -> {
                                    findCurrentFragment(
                                        context,
                                        previousPosition,
                                        PageScrollState.IDLE,
                                    )
                                }

                                ViewPager2.SCROLL_STATE_DRAGGING -> {
                                    findCurrentFragment(
                                        context,
                                        previousPosition,
                                        if (previousPosition ==
                                            0
                                        ) {
                                            PageScrollState.SCROLL_DOWNWARDS
                                        } else {
                                            scrollState
                                        },
                                    )
                                }

                                ViewPager2.SCROLL_STATE_SETTLING -> {
                                    findCurrentFragment(
                                        context,
                                        previousPosition,
                                        PageScrollState.IDLE,
                                    )
                                }
                            }
                        }
                    },
                )
            }
        }
    AndroidView(
        modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding),
        factory = { _ -> viewPager },
    )
    // 초기화

    BackHandler(enabled = true) {
        viewPager.adapter = null
        mainViewModel.runNavigationBack(Destination.YouTube.route)
        corutineScope.launch {
            mainViewModel.setRecentVideo()
            mainViewModel.setPIPClick(Pair(false, null))
        }
    }

    if (endBackButtonAction) {
        viewPager.adapter = null
        mainViewModel.setEndBack(false)
        LaunchedEffect(Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                mainViewModel.setRecentVideo()
                mainViewModel.setPIPClick(Pair(false, null))
            }
        }
    }

    mainViewModel.setViewPager(viewPager)
}
