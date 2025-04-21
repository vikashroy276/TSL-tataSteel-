package com.example.tsl_app.activities

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.tsl_app.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * This is an Activity class named SearchTabScreen that inherits from AppCompatActivity.
 * It is used to set up a screen with tabbed navigation.
 * Create an instance of ViewPagerAdapter (a custom adapter, assumed to handle fragments for each tab).
 * TabLayoutMediator is used to link TabLayout with ViewPager2.
 * Attach the mediator to bind the tabs and ViewPager.
 */
class SearchTabScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabxml)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val backbtn = findViewById<LinearLayout>(R.id.backbtn)

        backbtn.setOnClickListener {
            finish()
        }

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Process Sheet"
                1 -> "Tally Sheet"
                2 -> "Pipe Number"
                else -> null
            }
        }.attach()
    }
}
