package com.example.ta_nizar_app.timer

class TimerModel {
    var name: String? = null
    var state: String? = null
    var run: Boolean? = null
    var counter: Int = 0

    constructor()
    constructor(
        name: String,
        state: String,
        run: Boolean,
        counter: Int
    ){
        this.name = name
        this.state = state
        this.run = run
        this.counter = counter
    }
}