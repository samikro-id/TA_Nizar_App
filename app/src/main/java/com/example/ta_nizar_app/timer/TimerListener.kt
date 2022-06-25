package com.example.ta_nizar_app.timer

interface TimerListener {
    fun onItemClick(position: Int)

    fun onSwClick(position: Int, on: Boolean)

    fun onNpChanged(position: Int, newVal: Int)
}