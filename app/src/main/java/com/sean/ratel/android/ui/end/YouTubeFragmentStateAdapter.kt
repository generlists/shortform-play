package com.sean.ratel.android.ui.end

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.sean.ratel.android.data.dto.MainShortsModel

class YouTubeFragmentStateAdapter(
    activity: FragmentActivity,
    val viewPager2: ViewPager2?,
    private val dataList: List<MainShortsModel>,
) : FragmentStateAdapter(activity) {
    private val fragmentMap = mutableMapOf<Int, Fragment>()

    override fun containsItem(itemId: Long): Boolean = super.containsItem(itemId)

    override fun getItemCount(): Int = dataList.size

    override fun getItemId(position: Int): Long = super.getItemId(position)

    override fun createFragment(position: Int): Fragment {
        if (fragmentMap[position] != null) {
            return fragmentMap[position]!!
        }
        val fragment =
            YouTubeEndFragment.newInstance(
                viewPager2,
                position,
                dataList.size,
                dataList[position],
            )
        fragmentMap[position] = fragment
        return fragment
    }
}
