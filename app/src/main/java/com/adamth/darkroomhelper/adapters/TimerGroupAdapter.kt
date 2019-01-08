package com.adamth.darkroomhelper.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.adamth.darkroomhelper.R
import com.adamth.darkroomhelper.classes.TimerGroup
import kotlinx.android.synthetic.main.timer_group_recycler_item.view.*

class TimerGroupAdapter(context: Context, items: ArrayList<TimerGroup>) : Adapter<TimerGroupViewHolder>() {

    private var mItems: ArrayList<TimerGroup> = items
    private var mContext: Context = context

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): TimerGroupViewHolder {
        return TimerGroupViewHolder(LayoutInflater.from(mContext).inflate(R.layout.timer_group_recycler_item, parent, false))
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun removeAt(position: Int) {
        mItems.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: TimerGroupViewHolder, position: Int) {
        holder.timerGroupNameTextView.text = mItems[position].name

        holder.parent.setOnClickListener {
            Log.d("Thing", "thing")
        }
    }
}

class TimerGroupViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val timerGroupNameTextView: TextView = view.timer_group_name_text
    val parent: View = view.timer_group_parent_layout
}