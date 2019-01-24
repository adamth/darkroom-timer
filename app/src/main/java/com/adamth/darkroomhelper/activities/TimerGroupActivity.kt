package com.adamth.darkroomhelper.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.adamth.darkroomhelper.R
import com.adamth.darkroomhelper.adapters.TimerGroupAdapter
import com.adamth.darkroomhelper.classes.DarkroomTimer
import com.adamth.darkroomhelper.classes.TimerGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class TimerGroupActivity : BaseTableViewActivity() {
    private lateinit var mAdapter: TimerGroupAdapter
    private var mItems: ArrayList<TimerGroup> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent navigation buttons from flashing when view is created
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        val timerGroupButton = findViewById<FloatingActionButton>(R.id.timer_group_button)
        timerGroupButton.setImageResource(R.drawable.ic_close_red_24dp)
        timerGroupButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        prepareTimerGroups()
    }

    private fun prepareTimerGroups() {
        val prefsString = getString(R.string.saved_timer_groups)
        val prefs = getSharedPreferences(prefsString, Context.MODE_PRIVATE)
        val timerGroupsJSONString = prefs.getString(getString(R.string.saved_timer_groups), null)
        if (timerGroupsJSONString != null) {
            val type = object : TypeToken<ArrayList<TimerGroup>>() {}.type
            mItems = Gson().fromJson<ArrayList<TimerGroup>>(timerGroupsJSONString, type)
        }

        val recyclerView: RecyclerView = findViewById(R.id.timer_recycler_view)
        mAdapter = TimerGroupAdapter(this, mItems)

        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}
