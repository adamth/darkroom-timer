package com.adamth.darkroomhelper.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.adamth.darkroomhelper.classes.DarkroomTimer
import com.adamth.darkroomhelper.R
import com.adamth.darkroomhelper.enums.TimerStatus
import kotlinx.android.synthetic.main.timer_recycler_item.view.*

class TimerAdapter(context: Context, items: ArrayList<DarkroomTimer>) : Adapter<ViewHolder>() {

    private var mItems: ArrayList<DarkroomTimer> = items
    var mContext: Context = context
    private var activeTimer: Int = -1
    var timerRunning = false

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.timer_recycler_item, parent, false), this)
        return holder
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    // Clean all elements of the recycler
    fun reset() {
        mItems.forEach { item ->
            item.reset()
        }
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        mItems.removeAt(position)
        notifyItemRemoved(position)
        if (activeTimer == position) {
            activeTimer = -1
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.timerValueTextView.text = mItems[position].currentTime()
        holder.timerNameTextView.text = mItems[position].name
        var timer = mItems.get(position)

        if (timer.status == TimerStatus.ACTIVE) {
            holder.actionImage.setImageResource(R.drawable.ic_pause_circle_outline_white_63dp)
        }

        if (timer.status == TimerStatus.COMPLETE) {
            holder.actionImage.setImageResource(R.drawable.ic_check_white_63dp)
        }

        if (timer.status == TimerStatus.READY) {
            holder.actionImage.setImageResource(R.drawable.ic_play_circle_outline_white_63dp)
        }

        holder.parent.setOnClickListener {
            if (activeTimer == position || activeTimer == -1) {
                if (timer.status != TimerStatus.COMPLETE) {
                    activeTimer = position
                    timerRunning = timer.toggleTimer(holder)
                }
            } else {
                val previouslyActiveTimer = mItems[activeTimer]
                if (previouslyActiveTimer.status != TimerStatus.ACTIVE) {
                    // Current timer is not running - update active timer and toggle it
                    activeTimer = position
                    timerRunning = timer.toggleTimer(holder)
                }
            }
        }
    }
}

class ViewHolder (view: View, timerAdapter: TimerAdapter) : RecyclerView.ViewHolder(view) {
    val mTimerAdapter = timerAdapter
    // Holds the TextView that will add each animal to
    val timerNameTextView: TextView = view.timer_name_text_view
    val timerValueTextView: TextView = view.timer_value_text_view
    val parent: View = view.timer_parent_layout
    val actionImage: ImageView = view.action_image
}