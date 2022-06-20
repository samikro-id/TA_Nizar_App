package com.example.ta_nizar_app.bluetooth

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ta_nizar_app.R
import com.example.ta_nizar_app.bluetooth.BluetoothListener
import com.example.ta_nizar_app.bluetooth.BluetoothModel

class BluetoothHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val itemName = itemView.findViewById(R.id.tvBluetoothName) as TextView
    private val itemAddress = itemView.findViewById(R.id.tvBluetoothAddress) as TextView

    fun bindContent(itemBluetooth: BluetoothModel){
        itemName.text = itemBluetooth.name
        itemAddress.text = itemBluetooth.address
    }

    fun bind(position:Int, listener: BluetoothListener){
        itemName.setOnClickListener{ listener.onItemClick(position)}
    }
}
