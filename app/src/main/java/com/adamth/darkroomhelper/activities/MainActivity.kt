package com.adamth.darkroomhelper.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
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
import android.view.WindowManager
import android.widget.LinearLayout
import com.adamth.darkroomhelper.classes.DarkroomTimer
import com.adamth.darkroomhelper.R
import com.adamth.darkroomhelper.adapters.TimerAdapter
import kotlinx.android.synthetic.main.add_timer.view.*
import android.util.DisplayMetrics
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.adamth.darkroomhelper.classes.SwipeToDeleteCallback
import com.adamth.darkroomhelper.classes.SwipeToEditCallback
import com.adamth.darkroomhelper.classes.TimerGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.delete_timer.view.*
import java.lang.NumberFormatException

class MainActivity : BaseTableViewActivity() {
    private var mItems: ArrayList<DarkroomTimer> = ArrayList()
    private lateinit var mAdapter: TimerAdapter
    private var mPrefs: SharedPreferences? = null
    private var timerGroupUUID: String? = null
    private val SET_TIMER_GROUP_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPrefs = this.getPreferences(Context.MODE_PRIVATE)

        prepareTimers()
        prepareAddButton()

        // select timer group button
        val timerGroupButton = findViewById<FloatingActionButton>(R.id.timer_group_button)
        timerGroupButton.setOnClickListener {
            val intent = Intent(this, TimerGroupActivity::class.java)
            startActivityForResult(intent, SET_TIMER_GROUP_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SET_TIMER_GROUP_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val testData = data?.getBooleanExtra("test", false)
                Log.d("thinh", testData.toString())
            }
        }
    }

    private fun saveTimers() {
        val prefString = timerGroupUUID
        val itemsJSONString = Gson().toJson(mItems)
        val editor = mPrefs!!.edit()
        editor.putString(prefString, itemsJSONString)
        editor.commit()
    }

    private fun prepareTimers() {
        if (timerGroupUUID == null) {
            val prefsString = getString(R.string.saved_timer_groups)
            val prefs = getSharedPreferences(prefsString, Context.MODE_PRIVATE)
            val timerGroupsJSONString = prefs.getString(getString(R.string.saved_timer_groups), null)
            val type = object : TypeToken<ArrayList<TimerGroup>>() {}.type
            val savedTimerGroups: ArrayList<TimerGroup>? = Gson().fromJson<ArrayList<TimerGroup>>(timerGroupsJSONString, type)
            if(savedTimerGroups != null && savedTimerGroups.count() > 0) {
                timerGroupUUID = savedTimerGroups[0].uuid.toString()
            } else {
                // create first timer group
                val timerGroup = TimerGroup("Default")
                timerGroupUUID = timerGroup.uuid.toString()
                timerGroup.save()
            }
        }


        val timersJSONString = getPreferences(MODE_PRIVATE).getString(getString(R.string.saved_timers), null)
        val type = object : TypeToken<ArrayList<DarkroomTimer>>() {}.type
        val savedItems: ArrayList<DarkroomTimer>? = Gson().fromJson<ArrayList<DarkroomTimer>>(timersJSONString, type)

        if (savedItems == null) {
            mItems.add(DarkroomTimer(120, "Pre Soak"))
            mItems.add(DarkroomTimer(195, "Developer"))
            mItems.add(DarkroomTimer(260, "Bleach"))
            mItems.add(DarkroomTimer(360, "Fixer"))
        } else {
            mItems = savedItems
        }

        saveTimers()

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
                alertDialog?.setView(dialogView)
                alertDialog.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                alertDialog.show()

                // user taps outside dialog
                alertDialog.setOnCancelListener{
                    mAdapter.notifyDataSetChanged()
                }

                alertDialog.window.decorView.systemUiVisibility = this@MainActivity.window.decorView.systemUiVisibility

                dialogView.cancel_delete_button.setOnClickListener{
                    alertDialog.dismiss()
                    mAdapter.notifyDataSetChanged()
                }

                dialogView.confirm_delete_button.setOnClickListener{
                    alertDialog.dismiss()
                    val index = viewHolder.adapterPosition
                    mAdapter.removeAt(index)
                    saveTimers()
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

                // user taps outside dialog
                alertDialog.setOnCancelListener{
                    mAdapter.notifyDataSetChanged()
                }

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
                        saveTimers()

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
                    saveTimers()

                    alertDialog.dismiss()
                } catch (nfe: NumberFormatException) {
                    dialogView.validation_error_text.text = "Please enter a valid time"
                    dialogView.visibility = View.VISIBLE
                }
            }
        }
    }
}

