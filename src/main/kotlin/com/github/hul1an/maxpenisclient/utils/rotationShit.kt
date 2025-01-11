package com.github.hul1an.maxpenisclient.utils

import com.github.hul1an.maxpenisclient.MyConfig
import net.minecraft.client.Minecraft
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.Vector
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class Rotations {
    val config = MyConfig

    private var SPEED = config.rotationSmoothness.toInt()
    private var RANDOMNESS = 10
    private var TREMOR_FREQUENCY = 40

    private var update = true
    private var rotate = false
    private var yawOnly = false
    private var reachedEnd = false
    private var pitch = 0.0
    private var target: Vec3? = null
    private var startYaw = 0.0f
    private var startPitch = 0.0f
    private var currentYaw = 0.0
    private var currentPitch = 0.0
    private var actions = mutableListOf<() -> Unit>()

    private var rotateCircle = false
    private var circles = mutableListOf<Any>()
    private var circleYaw = 0.0
    private var circlePitch = 0.0
    private var constant = 1.0
    private var precision = 1.0

    init {
        MinecraftForge.EVENT_BUS.register(this)

        val executor = Executors.newScheduledThreadPool(1) // Create a single-threaded pool

        executor.scheduleAtFixedRate({
            if (rotate) {
                val target = this.target?.let { this.getAngles(it) }

                val interpolated = target?.let { interpolate(it) }
                if (target != null) {
                    if (abs(get180Yaw(abs(target.yaw - Minecraft.getMinecraft().thePlayer.rotationYaw))) <= 1.0 && abs((target.pitch - Minecraft.getMinecraft().thePlayer.rotationPitch)) <= 1.0) {
                        triggerEndRotation()
                        return@scheduleAtFixedRate
                    }
                }

                if (interpolated != null) {
                    Minecraft.getMinecraft().thePlayer.rotationYaw = interpolated.yaw
                }
                if (interpolated != null) {
                    Minecraft.getMinecraft().thePlayer.rotationPitch = interpolated.pitch
                }
            }
        }, 0, 10, TimeUnit.MILLISECONDS)
    }



    @SubscribeEvent
    fun onWorldUnload(event: net.minecraftforge.event.world.WorldEvent.Unload) {
        stopRotate()
    }



    fun get180Yaw(yaw: Float): Float {
        var adjustedYaw = yaw
        while (adjustedYaw > 180) adjustedYaw -= 360
        while (adjustedYaw < -180) adjustedYaw += 360
        return adjustedYaw
    }


    fun stopRotate() {
        this.rotate = false
        this.target = null
        this.yawOnly = false
    }

    fun interpolate(targetRotation: NewRotation, speed: Int = SPEED): NewRotation {
        this.SPEED = config.rotationSmoothness.toInt()
        val lastRotation = NewRotation(Minecraft.getMinecraft().thePlayer.rotationYaw, Minecraft.getMinecraft().thePlayer.rotationPitch)

        // Get diffs and distance to vec
        val deltaYaw = MathHelper.wrapAngleTo180_float(targetRotation.yaw - lastRotation.yaw)
        val deltaPitch = targetRotation.pitch - lastRotation.pitch
        val distance = sqrt((deltaYaw * deltaYaw + deltaPitch * deltaPitch).toDouble())

        // Apply rotation speed and distance modifiers
        val diffYaw = abs(MathHelper.wrapAngleTo180_float(startYaw - targetRotation.yaw))
        val diffPitch = abs(MathHelper.wrapAngleTo180_float(startPitch - targetRotation.pitch))

        val maxYaw = speed / 2 * abs(deltaYaw / distance) * fader(diffYaw)
        val maxPitch = speed / 2 * abs(deltaPitch / distance) * fader(diffPitch)

        // Clamp yaw change based on distance to vec
        // Randomize movement speed (variance decreasing exponentially as target approaches)
        val moveYaw = MathHelper.clamp_float(deltaYaw, -maxYaw.toFloat(), maxYaw.toFloat()) + randomness() * fader(deltaYaw)
        val movePitch = MathHelper.clamp_float(deltaPitch, -maxPitch.toFloat(), maxPitch.toFloat()) + randomness() * fader(deltaPitch)

        var newYaw = lastRotation.yaw + moveYaw
        var newPitch = lastRotation.pitch + movePitch
        val sensitivity = getSensitivity()

        // Add tremors based on sensitivity
        val tremorStrength = abs(sensitivity) * 0.2
        newYaw += (tremorStrength * kotlin.math.sin(TREMOR_FREQUENCY * System.currentTimeMillis().toDouble())).toFloat()
        newPitch += (tremorStrength * kotlin.math.cos(TREMOR_FREQUENCY * System.currentTimeMillis().toDouble())).toFloat()

        // Apply sensitivity
        for (i in 1..(Minecraft.getMinecraft().gameSettings.mouseSensitivity / 20 + Math.random() * 10).toInt()) {
            val adjustedRotations = applySensitivity(NewRotation(newYaw, newPitch))

            newYaw = adjustedRotations.yaw
            newPitch = MathHelper.clamp_float(adjustedRotations.pitch, -90f, 90f)
        }

        return NewRotation(newYaw, newPitch)
    }

    fun applySensitivity(rotation: NewRotation): NewRotation {
        val currentRotation = NewRotation(Minecraft.getMinecraft().thePlayer.rotationYaw, Minecraft.getMinecraft().thePlayer.rotationPitch)

        val multiplier = getSensitivity().toDouble().pow(3.0) * 8 * 0.15
        val yaw = currentRotation.yaw + (Math.round((rotation.yaw - currentRotation.yaw) / multiplier) * multiplier).toFloat()
        val pitch = currentRotation.pitch + (Math.round((rotation.pitch - currentRotation.pitch) / multiplier) * multiplier).toFloat()

        return NewRotation(yaw, MathHelper.clamp_float(pitch, -90f, 90f))
    }

    fun randomness(): Float {
        return (Math.random() - 0.5).toFloat() * RANDOMNESS
    }

    fun fader(diff: Float): Float {
        return 1 - Math.exp(-Math.abs(diff) * 0.02).toFloat()
    }

    fun getSensitivity(): Float {
        return (Minecraft.getMinecraft().gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6).toFloat() + 0.2f
    }

    fun rotateTo(vector: Vec3, precision: Float = 1.0f, yawOnly: Boolean = false, pitch: Float = 0.0f) {
        if (Minecraft.getMinecraft().thePlayer == null) {
            println("Player is null")
            return
        }
        rotate = true
        val vec = vector
        if (!reachedEnd && target != null && rotate && vec.xCoord == target!!.xCoord && vec.yCoord == target!!.yCoord && vec.zCoord == target!!.zCoord) return
        target = Vec3(vec.xCoord, vec.yCoord, vec.zCoord)
        this.yawOnly = yawOnly
        this.pitch = pitch.toDouble()
        startYaw = Minecraft.getMinecraft().thePlayer.rotationYaw
        startPitch = Minecraft.getMinecraft().thePlayer.rotationPitch
        this.precision = precision.toDouble()
        reachedEnd = false
    }

    fun onEndRotation(callBack: () -> Unit) {
        actions.add(callBack)
    }

    fun triggerEndRotation() {
        rotate = false

        val finalRot = getAngles(target!!)
        val sensFix = applySensitivity(NewRotation(Minecraft.getMinecraft().thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(finalRot.yaw - Minecraft.getMinecraft().thePlayer.rotationYaw), finalRot.pitch))

        Minecraft.getMinecraft().thePlayer.rotationYaw = sensFix.yaw
        Minecraft.getMinecraft().thePlayer.rotationPitch = sensFix.pitch

        actions.forEach { it() }
        actions.clear()
        yawOnly = false
        reachedEnd = true
    }

    fun getAngles(vec: Any): NewRotation {
        return if (vec is NewRotation) vec else getAnglesFromVec(Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f), vec as Vec3)
    }

    fun getAnglesFromVec(from: Vec3, to: Vec3): NewRotation {
        val deltaX = to.xCoord - from.xCoord
        val deltaY = to.yCoord - from.yCoord
        val deltaZ = to.zCoord - from.zCoord

        val yaw = (atan2(deltaZ, deltaX) * (180 / Math.PI)).toFloat() - 90
        val pitch = -(atan2(deltaY, sqrt((deltaX * deltaX + deltaZ * deltaZ).toDouble())) * (180 / Math.PI)).toFloat()
        return NewRotation(yaw, pitch)


    }

    fun setYaw(yaw: Float) {
        val random = Math.random() * 0.08

        var endRot = Minecraft.getMinecraft().thePlayer.rotationYaw + (yaw - Minecraft.getMinecraft().thePlayer.rotationYaw)
        if (endRot > 0) endRot += random.toFloat()
        else endRot -= random.toFloat()

        Minecraft.getMinecraft().thePlayer.rotationYaw = applySensitivity(NewRotation(endRot, 0.0f)).yaw
    }

    fun setPitch(pitch: Float) {
        Minecraft.getMinecraft().thePlayer.rotationPitch = applySensitivity(NewRotation(0.0f, pitch + (Math.random() * 0.08).toFloat())).pitch
    }
}

class NewRotation(val yaw: Float, val pitch: Float)