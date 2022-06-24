package com.example.ta_nizar_app.timer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ta_nizar_app.R

class TimerAdapter(
    private val context: Context,
    private val data: List<TimerModel>,
    private val listener: TimerListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    companion object{
        private const val ITEM_TIMER = 0
    }

    override fun getItemViewType(position: Int): Int{
        return super.getItemViewType(position)
        return when (data[position]){
            is TimerModel -> ITEM_TIMER
            else -> throw IllegalArgumentException("Undefined view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        TODO("Not yet implemented")
        return TimerHolder(mLayoutInflater.inflate(R.layout.item_timer, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        TODO("Not yet implemented")
        val timerHolder = holder as TimerHolder
        timerHolder.bind(position, listener)
        timerHolder.bindContent(data[position] as TimerModel)
    }

    override fun getItemCount(): Int {
//        TODO("Not yet implemented")
        return data.size
    }
}