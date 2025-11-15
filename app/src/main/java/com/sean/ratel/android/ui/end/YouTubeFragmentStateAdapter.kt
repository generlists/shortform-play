package com.sean.ratel.android.ui.end

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.dto.MainShortsModel

class YouTubeFragmentStateAdapter(
    activity: FragmentActivity,
    private val fromSearch:Boolean,
    val viewPager2: ViewPager2?,
    private var dataList: List<MainShortsModel>,
) : FragmentStateAdapter(activity) {
    override fun containsItem(itemId: Long): Boolean =
        dataList.any {
            it.shortsVideoModel
                ?.videoId
                .hashCode()
                .toLong() == itemId
        }

    override fun getItemCount(): Int = dataList.size

    override fun getItemId(position: Int): Long {
        if (dataList.isEmpty() || position < 0 || position >= dataList.size) {
            RLog.w("Adapter", "getItemId called with invalid position=$position, dataList.size=${dataList.size}")
            return RecyclerView.NO_ID
        }
        val id = dataList[position].shortsVideoModel?.videoId ?: return position.toLong()
        return id.hashCode().toLong()
    }

    override fun createFragment(position: Int): Fragment {
        return YouTubeEndFragment.newInstance(
            viewPager2,
            position,
            fromSearch,
            dataList.size,
            dataList[position],
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<MainShortsModel>) {
        if (newList != dataList) {
            dataList = newList
            notifyDataSetChanged()
        }
    }
}
