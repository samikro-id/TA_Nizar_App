package com.example.ta_nizar_app.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ta_nizar_app.R
import com.example.ta_nizar_app.bluetooth.BluetoothHolder
import com.example.ta_nizar_app.bluetooth.BluetoothListener
import com.example.ta_nizar_app.bluetooth.BluetoothModel

class HomeAdapter (
    private val context: Context,
    private val data: List<HomeModel>,
    private val listener: HomeListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    companion object{
        private const val ITEM_BLUETOOTH = 0
    }

    override fun getItemViewType(position: Int): Int{
        return super.getItemViewType(position)
        return when (data[position]){
            is HomeModel -> ITEM_BLUETOOTH
            else -> throw IllegalArgumentException("Undefined view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        TODO("Not yet implemented")
        return HomeHolder(mLayoutInflater.inflate(R.layout.item_button, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        TODO("Not yet implemented")
        val homeHolder = holder as HomeHolder
        homeHolder.bindContent(data[position] as HomeModel)
        homeHolder.bind(position, listener)
    }

    override fun getItemCount(): Int {
//        TODO("Not yet implemented")
        return data.size
    }
}