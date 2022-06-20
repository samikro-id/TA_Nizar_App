package com.example.ta_nizar_app.bluetooth

class BluetoothModel {
    var name: String? = null
    var address: String? = null

    constructor()
    constructor(
        name: String,
        address: String
    ){
        this.name = name
        this.address = address
    }
}