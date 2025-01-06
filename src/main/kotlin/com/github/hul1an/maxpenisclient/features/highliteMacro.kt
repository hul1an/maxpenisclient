package com.github.hul1an.maxpenisclient.features

import com.github.hul1an.maxpenisclient.MyConfig
import com.github.hul1an.maxpenisclient.utils.MovementHelper
import com.github.hul1an.maxpenisclient.utils.Rotations
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

class HighliteMacro {



    val config = MyConfig()
    val movementHelper = MovementHelper()
    val rotations = Rotations()

    private var finalAge: Int = 3
    private var Enabled: Boolean
    private var whitelist: Array<Any>
    private var sides: Array<DoubleArray> = arrayOf(
        doubleArrayOf(0.5, 0.01, 0.5),
        doubleArrayOf(0.5, 0.98, 0.5),
        doubleArrayOf(0.01, 0.5, 0.5),
        doubleArrayOf(0.98, 0.5, 0.5),
        doubleArrayOf(0.5, 0.5, 0.01),
        doubleArrayOf(0.5, 0.5, 0.99)
    )

    init { //equivalent to constructor in js
        this.Enabled = false
        this.whitelist = emptyArray()
        //toggle works on keybind and is called from ModCore.kt :)
        when(config.finalAge){
            0 -> this.finalAge = 3 //youngite
            1 -> this.finalAge = 11 //timeite
            2 -> this.finalAge = 10 //obsolite
        }



    }



    //todo

    //register when the highlite macro keybind in config is pressed and call toggle function: done
    //when enabled = true start bot
    //warp wizard
    //enter rift
    //use waypoints to move to eye
    //use waypoints to move to start waypoint for mining
    //if a vein has a player in it, go to it but do not mine, simply rotate and move to next waypoint
    //start mining/aging
    //move to next waypoint when vein depleted
    //when 32x youngite start mining timeite
    //when 32x timite start mining obsolite
    //when 16x obsolite craft highlite
    //start mining youngite again
    //when final vein waypoint is reached, start new path to the bottom
    //when chat message Rift is Collapsing! is recieved, stop bot
    //when enabled = false stop bot

    fun toggle() {
        System.out.println("toggled")
        this.Enabled = !this.Enabled
        if(this.Enabled) {
            System.out.println("currently enabled")
        }
    }

    fun stopBot() {
        this.Enabled = false
        movementHelper.stopMovement()
        movementHelper.setKey("shift", down = false)
        movementHelper.setKey("leftclick", down = false)
        rotations.stopRotate()
    }
}

class MiningBlock(val blockid: Int, val metadata: Int) {

    fun equals(block: Block): Boolean {
        val blockState = block.defaultState
        return block.getMetaFromState(blockState) == this.metadata && Block.getIdFromBlock(block) == this.blockid
    }

    fun equalsNumbers(blockid: Int, metadata: Int): Boolean {
        return blockid == this.blockid && metadata == this.metadata
    }
}

class MineTarget(val pos: BlockPos, val point: Vec3, block: Block) {
    val metadata: Int = block.getMetaFromState(block.defaultState)
    val blockid: Int = Block.getIdFromBlock(block)
}

class MineVein(val positions: Array<BlockPos>)