package com.example.ta_nizar_app.timer

interface TimerListener {
    fun onItemClick(position: Int, timerSecond: Int)

    fun onSwClick(position: Int, on: Boolean)
}