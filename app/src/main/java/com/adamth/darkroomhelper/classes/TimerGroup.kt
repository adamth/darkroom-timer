package com.adamth.darkroomhelper.classes

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.adamth.DarkroomHelper
import com.adamth.darkroomhelper.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

class TimerGroup(name: String) {
    val uuid: UUID = UUID.randomUUID()
    val name = name

    fun save() {
        val context = DarkroomHelper.getAppContext()
        var items = ArrayList<TimerGroup>()
        val prefsString = context.getString(R.string.saved_timer_groups)
        val prefs = context.getSharedPreferences(prefsString, Context.MODE_PRIVATE)
        val timerGroupsJSONString = prefs.getString(context.getString(R.string.saved_timer_groups), null)

        if (timerGroupsJSONString != null) {
            val type = object : TypeToken<ArrayList<TimerGroup>>() {}.type
            items = Gson().fromJson<ArrayList<TimerGroup>>(timerGroupsJSONString, type)
            var found = false
            items.forEachIndexed { index, timerGroup ->
                if (timerGroup.uuid == this.uuid) {
                    items[index] = this
                    found = true
                    return@forEachIndexed
                }
            }
            if (!found) {
                items.add(this)
            }
        } else {
            items.add(this)
        }
        val itemsJSONString = Gson().toJson(items)
        val editor = prefs!!.edit()
        editor.putString(prefsString, itemsJSONString)
        editor.commit()
    }
}