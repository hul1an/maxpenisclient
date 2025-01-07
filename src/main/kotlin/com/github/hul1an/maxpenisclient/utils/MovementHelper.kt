package com.github.hul1an.maxpenisclient.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.BlockPos
import kotlin.math.abs

class MovementHelper {
    private val cooldown: TimeHelper

    init {
        cooldown = TimeHelper()
    }

    val mc = Minecraft.getMinecraft()

    fun setKey(key: String, down: Boolean) {
        when (key) {
            "a" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.keyCode, down)
            "d" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.keyCode, down)
            "s" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.keyCode, down)
            "w" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, down)
            "space" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.keyCode, down)
            "shift" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.keyCode, down)
            "leftclick" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.keyCode, down) // use for mining blocks :)) no more packets
            "sprint" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, down)
        }
    }

    fun isKeyDown(key: String): Boolean {
        return when (key) {
            "a" -> mc.gameSettings.keyBindLeft.isKeyDown
            "d" -> mc.gameSettings.keyBindRight.isKeyDown
            "s" -> mc.gameSettings.keyBindBack.isKeyDown
            "w" -> mc.gameSettings.keyBindForward.isKeyDown
            "space" -> mc.gameSettings.keyBindJump.isKeyDown
            "shift" -> mc.gameSettings.keyBindSneak.isKeyDown
            "leftclick" -> mc.gameSettings.keyBindAttack.isKeyDown
            "sprint" -> mc.gameSettings.keyBindSprint.isKeyDown
            else -> false
        }
    }

    fun onTickLeftClick() {
        KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode)
    }

    fun setKeysBasedOnYaw(yaw: Double, jump: Boolean = true) {
        try {
           // println("setKeysBasedOnYaw called with yaw: $yaw, jump: $jump")
            this.stopMovement()

            if (yaw >= -50.0 && yaw <= 50.0) {
                this.setKey("w", true)
                this.setKey("sprint", true)
            }
            if (yaw >= -135.5 && yaw <= -7.0) {
                this.setKey("a", true)
            }
            if (yaw >= 7.0 && yaw <= 135.5) {
                this.setKey("d", true)
            }
            if (yaw <= -135.5 || yaw >= 135.5) {
                this.setKey("s", true)
            }

            val player = mc.thePlayer
            if (player != null) {
                val shouldJump = abs(player.motionX) + abs(player.motionZ) < 0.02 && this.cooldown.hasReached(500) && jump && UtilsClass().playerIsCollided()
               // println("Setting space key: $shouldJump")
                this.setKey("space", shouldJump)
            } else {
                println("Player instance is null")
            }
        } catch (e: Exception) {
            println("Error in setKeysBasedOnYaw: ${e.message}")
            e.printStackTrace()
        }
    }

    fun setKeysForStraightLine(yaw: Double, jump: Boolean = true) {
        this.stopMovement()
        when {
            yaw in -22.5..22.5 -> { // Forwards
                this.setKey("w", true)
            }
            yaw in -67.5..-22.5 -> { // Forwards + Right
                this.setKey("w", true)
                this.setKey("a", true)
            }
            yaw in -112.5..-67.5 -> { // Right
                this.setKey("a", true)
            }
            yaw in -157.5..-112.5 -> { // Backwards + Right
                this.setKey("a", true)
                this.setKey("s", true)
            }
            yaw in -180.0..-157.5 || yaw in 157.5..180.0 -> { // Backwards
                this.setKey("s", true)
            }
            yaw in 22.5..67.5 -> { // Forwards + Left
                this.setKey("w", true)
                this.setKey("d", true)
            }
            yaw in 67.5..112.5 -> { // Left
                this.setKey("d", true)
            }
            yaw in 112.5..157.5 -> { // Backwards + Left
                this.setKey("s", true)
                this.setKey("d", true)
            }
        }
        val player = mc.thePlayer
        if (player != null) {
            this.setKey("space", player.isInWater || (abs(player.motionX) + abs(player.motionZ) < 0.02 && this.cooldown.hasReached(500) && jump && UtilsClass().playerIsCollided()))
        } else {
            println("Player instance is null")
        }
    }

    fun setCooldown() {
        this.cooldown.reset()
    }

    fun stopMovement() {
        this.setKey("w", false)
        this.setKey("a", false)
        this.setKey("s", false)
        this.setKey("d", false)
        this.setKey("sprint", false)
    }

    fun unpressKeys() {
        this.stopMovement()
        this.setKey("shift", false)
    }

    fun isAllReleased(): Boolean {
        return (mc.gameSettings.keyBindLeft.isKeyDown || mc.gameSettings.keyBindRight.isKeyDown || mc.gameSettings.keyBindBack.isKeyDown || mc.gameSettings.keyBindForward.isKeyDown)
    }

    fun getWalkablePoints(pos: BlockPos): List<Array<Double>> {
        val points = mutableListOf<Array<Double>>()
        points.add(arrayOf(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5))

        var x = -0.8
        while (x <= 0.8) {
            var z = -0.8
            while (z <= 0.8) {
                points.add(arrayOf(pos.x + 0.5 + x, pos.y + 1.0, pos.z + 0.5 + z))
                z += 0.4
            }
            x += 0.4
        }

        return points
    }
}