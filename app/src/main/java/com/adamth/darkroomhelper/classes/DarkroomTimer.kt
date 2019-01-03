package com.adamth.darkroomhelper.classes

import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.adamth.darkroomhelper.R
import com.adamth.darkroomhelper.adapters.ViewHolder
import com.adamth.darkroomhelper.enums.TimerStatus

class DarkroomTimer(seconds: Int, name: String) {
    var name: String = name
    var status: TimerStatus = TimerStatus.READY

    private var currentTime = seconds
    private val initialTime = seconds
    private var running = false
    private lateinit var countDownTimer: CountDownTimer

    fun currentTime(): String {
        return "${this.minutes()}:${this.seconds()}"
    }

    fun minutes(): String {
        val minutes = currentTime / 60
        var minutesString: String

        if (minutes < 10) {
            minutesString = "0" + minutes.toString()
        } else {
            minutesString = minutes.toString()
        }

        return minutesString
    }

    fun seconds(): String {
        val seconds = currentTime % 60
        var secondsString: String

        if (seconds < 10) {
            secondsString = "0" + seconds.toString()
        } else {
            secondsString = seconds.toString()
        }

        return secondsString
    }

    private fun startTimer(view: ViewHolder) {
        running = true
        status = TimerStatus.ACTIVE
        countDownTimer = object: CountDownTimer(currentTime.toLong() * 1000, 1000) {
            override fun onFinish() {
                status = TimerStatus.COMPLETE
                updateTimerView(view)
            }

            override fun onTick(millisUntilFinished: Long) {
                currentTime = millisUntilFinished.toInt() / 1000
                updateTimerView(view)
            }

        }.start()
        updateTimerView(view)
    }

    private fun stopTimer() {
        running = false
        status = TimerStatus.READY
        countDownTimer.cancel()
    }

    fun toggleTimer(view: ViewHolder): Boolean {
        if (running) {
            stopTimer()
            updateTimerView(view)
            return false
        } else {
            startTimer(view)
            return true
        }
    }

    private fun updateTimerView(view: ViewHolder) {
        val timerTextView: TextView = view.timerValueTextView
        timerTextView.text = currentTime()
        val actionImage: ImageView = view.actionImage
        actionImage.setImageResource(R.drawable.ic_play_circle_outline_white_63dp)

        if (status == TimerStatus.ACTIVE) {
            actionImage.visibility = View.VISIBLE
            actionImage.setImageResource(R.drawable.ic_pause_circle_outline_white_63dp)
        }

        if (status == TimerStatus.COMPLETE) {
            actionImage.visibility = View.VISIBLE
            actionImage.setImageResource(R.drawable.ic_check_white_63dp)
            view.mTimerAdapter.timerRunning = false
        }
    }

    fun reset() {
        Log.d("Adapter", "Called")
        currentTime = initialTime
        status = TimerStatus.READY
    }
}