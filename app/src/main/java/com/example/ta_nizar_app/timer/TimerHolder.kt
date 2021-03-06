package com.example.ta_nizar_app.timer

import android.R.attr.order
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.NumberPicker.OnValueChangeListener
import androidx.recyclerview.widget.RecyclerView
import com.example.ta_nizar_app.R
import java.lang.String
import kotlin.Int


class TimerHolder (itemView: View): RecyclerView.ViewHolder(itemView)  {
    private val itemTmName = itemView.findViewById(R.id.tvTmName) as TextView
    private val itemSwOnOff = itemView.findViewById(R.id.swOnOff) as Switch
    private val itemStartStop = itemView.findViewById(R.id.btStart) as Button
    private val itemNpTimer = itemView.findViewById(R.id.npTimer) as NumberPicker
    private val itemNpTimerTen = itemView.findViewById(R.id.npTimerTen) as NumberPicker
    private val itemNpTimerHundred = itemView.findViewById(R.id.npTimerHundred) as NumberPicker
    private val itemNpTimerThousand = itemView.findViewById(R.id.npTimerThousand) as NumberPicker

    fun bindContent(itemTime: TimerModel){
        itemTmName.text = itemTime.name

        when(itemTime.state){
            "OFF" -> itemSwOnOff.isChecked = false
            else -> itemSwOnOff.isChecked = true
        }

        when(itemTime!!.run){
            true -> itemStartStop.text = "STOP"
            else -> {itemStartStop.text = "START"}
        }

        var cal = itemTime.counter;

        if(cal > 10000){
            cal = cal % 10000
        }
        itemNpTimerThousand.value = cal.div(1000)

        if(cal > 1000){
            cal = cal % 1000
        }
        itemNpTimerHundred.value = cal.div(100)

        if(cal > 100){
            cal = cal % 100
        }
        itemNpTimerTen.value = cal.div(10)

        if(cal > 10){
            cal = cal % 10
        }
        itemNpTimer.value = cal

    }

    fun bind(position:Int, listener: TimerListener){
        itemNpTimer.minValue = 0
        itemNpTimer.maxValue = 9
        itemNpTimerTen.minValue = 0
        itemNpTimerTen.maxValue = 9
        itemNpTimerHundred.minValue = 0
        itemNpTimerHundred.maxValue = 9
        itemNpTimerThousand.minValue = 0
        itemNpTimerThousand.maxValue = 9

        itemStartStop.setOnClickListener{
            listener.onItemClick(position)
        }

        itemSwOnOff.setOnClickListener {
            listener.onSwClick(position, itemSwOnOff.isChecked)
        }

        itemNpTimerThousand.setOnValueChangedListener(OnValueChangeListener { picker, oldVal, newVal ->
            var tmSecond : Int = 0
            tmSecond = (itemNpTimerThousand.value * 1000) + (itemNpTimerHundred.value * 100) + (itemNpTimerTen.value * 10) + itemNpTimer.value

            listener.onNpChanged(position, tmSecond)
        })

        itemNpTimerHundred.setOnValueChangedListener(OnValueChangeListener { picker, oldVal, newVal ->
            var tmSecond : Int = 0
            tmSecond = (itemNpTimerThousand.value * 1000) + (itemNpTimerHundred.value * 100) + (itemNpTimerTen.value * 10) + itemNpTimer.value

            listener.onNpChanged(position, tmSecond)
        })

        itemNpTimerTen.setOnValueChangedListener(OnValueChangeListener { picker, oldVal, newVal ->
            var tmSecond : Int = 0
            tmSecond = (itemNpTimerThousand.value * 1000) + (itemNpTimerHundred.value * 100) + (itemNpTimerTen.value * 10) + itemNpTimer.value

            listener.onNpChanged(position, tmSecond)
        })

        itemNpTimer.setOnValueChangedListener(OnValueChangeListener { picker, oldVal, newVal ->
            var tmSecond : Int = 0
            tmSecond = (itemNpTimerThousand.value * 1000) + (itemNpTimerHundred.value * 100) + (itemNpTimerTen.value * 10) + itemNpTimer.value

            listener.onNpChanged(position, tmSecond)
        })
    }
}