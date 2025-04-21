package com.example.tsl_app.activities

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tsl_app.activities.searchProcTallyPipe.searchPipeNumber.PipeNumberFragment
import com.example.tsl_app.activities.searchProcTallyPipe.searchTallySheet.TallySheetFragment
import com.example.tsl_app.activities.searchProcTallyPipe.searchProcesssheet.SearchSheetFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SearchSheetFragment()
            1 -> TallySheetFragment()
            2 -> PipeNumberFragment()
            else -> SearchSheetFragment()
        }
    }
}

