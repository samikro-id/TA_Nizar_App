package com.example.ta_nizar_app.home

class HomeModel {
    var name: String? = null
    var id: String? = null
    var state: String? = null

    constructor()
    constructor(
        name: String,
        id: String,
        state: String
    ){
        this.name = name
        this.id = id
        this.state = state
    }
}