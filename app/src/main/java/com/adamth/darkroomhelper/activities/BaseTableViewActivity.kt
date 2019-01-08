package com.adamth.darkroomhelper.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import com.adamth.darkroomhelper.classes.DarkroomTimer
import com.adamth.darkroomhelper.R
import com.adamth.darkroomhelper.adapters.TimerAdapter
import kotlinx.android.synthetic.main.add_timer.view.*
import android.util.DisplayMetrics
import android.widget.TextView
import android.widget.Toast
import com.adamth.darkroomhelper.classes.SwipeToDeleteCallback
import com.adamth.darkroomhelper.classes.SwipeToEditCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.delete_timer.view.*
import java.lang.NumberFormatException

abstract class BaseTableViewActivity : AppCompatActivity() {
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        val redLightButton = findViewById<FloatingActionButton>(R.id.red_light_button)
        redLightButton.setOnClickListener {
            val intent = Intent(this, RedLightActivity::class.java)
            startActivity(intent)
        }
        super.onCreate(savedInstanceState)
    }
}

