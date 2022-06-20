package com.example.ta_nizar_app.bluetooth

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ta_nizar_app.R

class BluetoothAdapter (
    private val context: Context,
    private val data: List<BluetoothModel>,
    private val listener: BluetoothListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    companion object{
        private const val ITEM_BLUETOOTH = 0
    }

    override fun getItemViewType(position: Int): Int{
        return super.getItemViewType(position)
        return when (data[position]){
            is BluetoothModel -> ITEM_BLUETOOTH
            else -> throw IllegalArgumentException("Undefined view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        TODO("Not yet implemented")
        return BluetoothHolder(mLayoutInflater.inflate(R.layout.item_bluetooth, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        TODO("Not yet implemented")
        val bluetoothHolder = holder as BluetoothHolder
        bluetoothHolder.bindContent(data[position] as BluetoothModel)
        bluetoothHolder.bind(position, listener)
    }

    override fun getItemCount(): Int {
//        TODO("Not yet implemented")
        return data.size
    }
}