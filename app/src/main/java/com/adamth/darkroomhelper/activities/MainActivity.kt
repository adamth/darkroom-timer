package com.adamth.darkroomhelper.activities

import android.content.Intent
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
import kotlinx.android.synthetic.main.delete_timer.view.*
import java.lang.NumberFormatException






class MainActivity : AppCompatActivity() {
    private var mItems: ArrayList<DarkroomTimer> = ArrayList()
    private lateinit var mAdapter: TimerAdapter

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
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        prepareTimers()
        prepareAddButton()

        val redLightButton = findViewById<FloatingActionButton>(R.id.red_light_button)
        redLightButton.setOnClickListener {
            val intent = Intent(this, RedLightActivity::class.java)
            startActivity(intent)
        }
    }

    private fun prepareTimers() {
        mItems.add(DarkroomTimer(60, "Developer"))
        mItems.add(DarkroomTimer(30, "Stop"))
        mItems.add(DarkroomTimer(60, "Fixer"))
        prepareRecycler()
    }

    private fun prepareRecycler() {
        val recyclerView: RecyclerView = findViewById(R.id.timer_recycler_view)
        mAdapter = TimerAdapter(this, mItems)
        val displayMetrics = DisplayMetrics()
        val refreshView = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_container)

        // Swipe to delete
        val swipeToDeleteHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Inflate the dialog view
                val dialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.delete_timer, null)
                val currentItem = mItems.get(viewHolder.adapterPosition)
                dialogView.timer_name_text.text = currentItem.name

                val alertDialog = AlertDialog.Builder(this@MainActivity).create()
                alertDialog.setView(dialogView)
                alertDialog.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                alertDialog.show()

                alertDialog.window.decorView.systemUiVisibility = this@MainActivity.window.decorView.systemUiVisibility

                dialogView.cancel_delete_button.setOnClickListener{
                    alertDialog.dismiss()
                    mAdapter.notifyDataSetChanged()
                }

                dialogView.confirm_delete_button.setOnClickListener{
                    alertDialog.dismiss()
                    mAdapter.removeAt(viewHolder.adapterPosition)
                }
            }
        }
        val itemTouchHelperDelete = ItemTouchHelper(swipeToDeleteHandler)
        itemTouchHelperDelete.attachToRecyclerView(recyclerView)

        // Swipe to edit
        val swipeToEditHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Inflate the dialog view
                val dialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.add_timer, null)
                dialogView.validation_error_text.visibility = View.GONE
                val currentItem = mItems.get(viewHolder.adapterPosition)
                dialogView.timer_name_edit_text.setText(currentItem.name)
                dialogView.minutes_edit_text.setText(currentItem.minutes())
                dialogView.seconds_edit_text.setText(currentItem.seconds())

                val alertDialog = AlertDialog.Builder(this@MainActivity).create()
                alertDialog.setView(dialogView)

                alertDialog.show()

                dialogView.cancel_edit_button.setOnClickListener{
                    alertDialog.dismiss()
                    mAdapter.notifyDataSetChanged()
                }

                dialogView.confirm_edit_button.setOnClickListener{
                    val timerName = dialogView.timer_name_edit_text.text.toString()
                    try {
                        val minutes = dialogView.minutes_edit_text.text.toString().toInt()
                        val seconds = dialogView.seconds_edit_text.text.toString().toInt()
                        val totalSeconds = (minutes * 60) + seconds
                        mItems[viewHolder.adapterPosition] = DarkroomTimer(totalSeconds, timerName)
                        mAdapter.notifyDataSetChanged()

                        alertDialog.dismiss()
                    } catch (nfe: NumberFormatException) {
                        dialogView.validation_error_text.text = "Please enter a valid time"
                        dialogView.visibility = View.VISIBLE
                    }
                }
            }
        }
        val itemTouchHelperEdit = ItemTouchHelper(swipeToEditHandler)
        itemTouchHelperEdit.attachToRecyclerView(recyclerView)

        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val params = refreshView.layoutParams
        params.height = height - 70

        refreshView.layoutParams = params

        // Item divider
        val dividerItem = DividerItemDecoration(this, LinearLayout.VERTICAL)
        recyclerView.addItemDecoration(dividerItem)

        // Pull down to refresh
        val swipeContainer: SwipeRefreshLayout = findViewById(R.id.swipe_refresh_container)
        swipeContainer.setOnRefreshListener {
            if (!mAdapter.timerRunning) {
                mAdapter.reset()
            } else {
                val toast = Toast.makeText(this, "Can't reset timers while a timer is running", Toast.LENGTH_SHORT)
                val toastView = toast.view
                toastView.background.setColorFilter(getColor(R.color.colorPrimaryRed), PorterDuff.Mode.SRC_IN)
                val toastText = toastView.findViewById<TextView>(android.R.id.message)
                toastText.setTextColor(getColor(R.color.colorWhiteRed))
                toast.show()
            }
            swipeContainer.isRefreshing = false
        }

        swipeContainer.setColorSchemeColors(
            getColor(R.color.colorWhiteRed),
            getColor(R.color.colorPrimaryDarkRed),
            getColor(R.color.colorAccentRed),
            getColor(R.color.colorPrimaryRed)
        )
        swipeContainer.setProgressBackgroundColorSchemeColor(getColor(R.color.colorPrimaryRed))

        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun prepareAddButton() {
        val addButton = this.findViewById<FloatingActionButton>(R.id.add_timer_button)
        
        addButton.setOnClickListener{
            // Inflate the dialog view
            val dialogView = LayoutInflater.from(this).inflate(R.layout.add_timer, null)
            dialogView.validation_error_text.visibility = View.GONE

            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setView(dialogView)

            alertDialog.show()

            dialogView.cancel_edit_button.setOnClickListener{
                alertDialog.dismiss()
            }

            dialogView.confirm_edit_button.setOnClickListener{
                val timerName = dialogView.timer_name_edit_text.text.toString()
                try {
                    val minutes = dialogView.minutes_edit_text.text.toString().toInt()
                    val seconds = dialogView.seconds_edit_text.text.toString().toInt()
                    val totalSeconds = (minutes * 60) + seconds

                    mItems.add(DarkroomTimer(totalSeconds, timerName))
                    mAdapter.notifyDataSetChanged()

                    alertDialog.dismiss()
                } catch (nfe: NumberFormatException) {
                    dialogView.validation_error_text.text = "Please enter a valid time"
                    dialogView.visibility = View.VISIBLE
                }
            }
        }
    }
}

