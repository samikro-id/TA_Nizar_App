package com.example.ta_nizar_app.home

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ta_nizar_app.R
import com.example.ta_nizar_app.bluetooth.BluetoothListener
import com.example.ta_nizar_app.bluetooth.BluetoothModel

class HomeHolder(itemView: View): RecyclerView.ViewHolder(itemView)  {
    private val itemName = itemView.findViewById(R.id.tvBtName) as TextView
    private val itemState = itemView.findViewById(R.id.tvBtState) as TextView

    fun bindContent(itemHome: HomeModel){
        itemName.text = itemHome.name
        itemState.text = itemHome.state
    }

    fun bind(position:Int, listener: HomeListener){
        itemName.setOnClickListener{ listener.onItemClick(position)}
    }
}