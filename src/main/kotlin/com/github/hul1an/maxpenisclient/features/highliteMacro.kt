package com.github.hul1an.maxpenisclient.features

import com.github.hul1an.maxpenisclient.MyConfig
import com.github.hul1an.maxpenisclient.utils.MovementHelper

class HighliteMacro {

    val config = MyConfig()
    val movementHelper = MovementHelper()

    private var finalAge: Int = 3
    private var Enabled: Boolean

    init { //equivalent to constructor in js
        this.Enabled = false

        when(config.finalAge){
            0 -> this.finalAge = 3 //youngite
            1 -> this.finalAge = 11 //timeite
            2 -> this.finalAge = 10 //obsolite
        }

    }

    //todo

    //register when the highlite macro keybind in config is pressed and call toggle function
    //when enabled = true start bot
    //
    //when enabled = false stop bot



    fun stopBot() {
        this.Enabled = false
        movementHelper.stopMovement()
        movementHelper.setKey("shift", down = false)
        movementHelper.setKey("leftclick", down = false)
    }
}