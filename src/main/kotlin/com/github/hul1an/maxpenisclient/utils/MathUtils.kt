package com.github.hul1an.maxpenisclient.utils

import net.minecraft.util.Vec3
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3i
import net.minecraft.entity.Entity
import net.minecraft.client.Minecraft
import java.math.RoundingMode
import net.minecraft.entity.EntityLivingBase
import kotlin.math.*

class MathUtilsClass {

    data class Vector(val x: Double, val y: Double, val z: Double)

    fun convertToVector(input: Any): Vector {
        return when (input) {
            is Vector -> input
            is Array<*> -> Vector((input[0] as Number).toDouble(), (input[1] as Number).toDouble(), (input[2] as Number).toDouble())
            is BlockPos, is Vec3i -> Vector((input as Vec3i).x.toDouble(), input.y.toDouble(), input.z.toDouble())
            is Vec3 -> Vector(input.xCoord, input.yCoord, input.zCoord)
            is Entity -> Vector(input.posX, input.posY, input.posZ)
            else -> throw IllegalArgumentException("Unsupported input type")
        }
    }

    fun distanceToPlayerPoint(point: Vec3): Map<String, Double> {
        val eyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)
        return calculateDistance(
            arrayOf(eyes.xCoord, eyes.yCoord, eyes.zCoord),
            arrayOf(point.xCoord, point.yCoord, point.zCoord)
        )
    }

    fun distanceToPlayer(point: Array<Double>): Map<String, Double> {
        val eyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)
        return calculateDistance(
            arrayOf(eyes.xCoord, eyes.yCoord, eyes.zCoord),
            point
        )
    }

    fun distanceToPlayerFeet(point: Array<Double>): Map<String, Double> {
        val player = Minecraft.getMinecraft().thePlayer
        return calculateDistance(
            arrayOf(player.posX, player.posY, player.posZ),
            point
        )
    }

    fun distanceToPlayerCenter(point: Array<Double>): Map<String, Double> {
        val player = Minecraft.getMinecraft().thePlayer
        val eyes = player.getPositionEyes(1.0f)
        return calculateDistance(
            arrayOf(eyes.xCoord, player.posY + (player.height / 2), eyes.zCoord),
            point
        )
    }

    fun distanceToPlayerCT(entity: Entity): Map<String, Double> {
        val eyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)
        return calculateDistance(
            arrayOf(eyes.xCoord, eyes.yCoord, eyes.zCoord),
            arrayOf(entity.posX, entity.posY, entity.posZ)
        )
    }

    fun distanceToPlayerMC(entity: EntityLivingBase): Map<String, Double> {
        val eyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)
        return calculateDistance(
            arrayOf(eyes.xCoord, eyes.yCoord, eyes.zCoord),
            arrayOf(entity.posX, entity.posY, entity.posZ)
        )
    }

    fun calculateDistanceBP(pos1: BlockPos, pos2: BlockPos): Map<String, Double> {
        val diffX = pos1.x - pos2.x
        val diffY = pos1.y - pos2.y
        val diffZ = pos1.z - pos2.z
        val distanceFlat = sqrt(((diffX * diffX) + (diffZ * diffZ)).toDouble())
        val distance = sqrt((distanceFlat * distanceFlat) + (diffY * diffY))
        return mapOf(
            "distance" to distance,
            "distanceFlat" to distanceFlat,
            "distanceY" to diffY.toDouble()
        )
    }

    private fun calculateDistance(p1: Array<Double>, p2: Array<Double>): Map<String, Double> {
        val diffX = p1[0] - p2[0]
        val diffY = p1[1] - p2[1]
        val diffZ = p1[2] - p2[2]
        val distanceFlat = sqrt((diffX * diffX) + (diffZ * diffZ))
        val distance = sqrt((distanceFlat * distanceFlat) + (diffY * diffY))
        return mapOf(
            "distance" to distance,
            "distanceFlat" to distanceFlat,
            "distanceY" to diffY * diffY
        )
    }


    fun getDistanceToPlayer(xInput: Any, yInput: Any, zInput: Any): Map<String, Double> {
        var x = xInput
        var y = yInput
        var z = zInput
        if (xInput !is Number) {
            val vector = convertToVector(xInput)
            x = vector.x
            y = vector.y
            z = vector.z
        }
        return getDistance(
            Minecraft.getMinecraft().thePlayer.posX,
            Minecraft.getMinecraft().thePlayer.posY,
            Minecraft.getMinecraft().thePlayer.posZ,
            x as Double,
            y as Double,
            z as Double
        )
    }

    fun getDistanceToPlayerEyes(xInput: Any, yInput: Any, zInput: Any): Map<String, Double> {
        var x = xInput
        var y = yInput
        var z = zInput
        if (xInput !is Number) {
            val vector = convertToVector(xInput)
            x = vector.x
            y = vector.y
            z = vector.z
        }
        val eyeVector = convertToVector(Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f))
        return getDistance(
            eyeVector.x,
            eyeVector.y,
            eyeVector.z,
            x as Double,
            y as Double,
            z as Double
        )
    }

    fun getDistance(xInput1: Any, yInput1: Any, zInput1: Any, xInput2: Any, yInput2: Any, zInput2: Any): Map<String, Double> {
        var x1 = xInput1
        var y1 = yInput1
        var z1 = zInput1
        var x2 = xInput2
        var y2 = yInput2
        var z2 = zInput2
        if (xInput1 !is Number) {
            val vector = convertToVector(xInput1)
            x1 = vector.x
            y1 = vector.y
            z1 = vector.z
        }
        if (xInput2 !is Number) {
            val vector = convertToVector(xInput2)
            x2 = vector.x
            y2 = vector.y
            z2 = vector.z
        }
        val diffX = (x1 as Double) - (x2 as Double)
        val diffY = (y1 as Double) - (y2 as Double)
        val diffZ = (z1 as Double) - (z2 as Double)
        val disFlat = sqrt((diffX * diffX) + (diffZ * diffZ))
        val dis = sqrt((diffY * diffY) + (disFlat * disFlat))
        return mapOf(
            "distance" to dis,
            "distanceFlat" to disFlat,
            "differenceY" to diffY
        )
    }

    fun fastDistance(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
        return hypot(hypot(x1 - x2, y1 - y2), z1 - z2)
    }

    fun toFixed(input: Double): Int {
        return input.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toInt()
    }

    fun angleToPlayer(point: Array<Double>): Map<String, Double> {
        val angles = calculateAngles(Vec3(point[0], point[1], point[2]))
        val yaw = angles.first
        val pitch = angles.second
        val distance = sqrt(yaw * yaw + pitch * pitch)
        return mapOf(
            "distance" to distance,
            "yaw" to yaw,
            "pitch" to pitch,
            "yawAbs" to abs(yaw),
            "pitchAbs" to abs(pitch)
        )
    }

    fun degreeToRad(degrees: Double): Double {
        return degrees * (PI / 180)
    }

    fun radToDegree(radians: Double): Double {
        return radians * (180 / PI)
    }

    fun wrapTo180(yaw: Double): Double {
        var wrappedYaw = yaw
        while (wrappedYaw > 180) wrappedYaw -= 360
        while (wrappedYaw < -180) wrappedYaw += 360
        return wrappedYaw
    }

    fun calculateAngles(vector: Any): Pair<Double, Double> {
        var vecX = 0.0
        var vecY = 0.0
        var vecZ = 0.0

        when (vector) {
            is Vec3 -> {
                vecX = vector.xCoord
                vecY = vector.yCoord
                vecZ = vector.zCoord
            }
            is BlockPos, is Vec3i -> {
                vecX = (vector as Vec3i).x.toDouble()
                vecY = vector.y.toDouble()
                vecZ = vector.z.toDouble()
            }
            is Array<*> -> {
                vecX = (vector[0] as Number).toDouble()
                vecY = (vector[1] as Number).toDouble()
                vecZ = (vector[2] as Number).toDouble()
            }
            is Entity -> {
                vecX = vector.posX
                vecY = vector.posY
                vecZ = vector.posZ
            }
        }

        val eyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)
        val diffX = vecX - eyes.xCoord
        val diffY = vecY - eyes.yCoord
        val diffZ = vecZ - eyes.zCoord
        val dist = sqrt(diffX * diffX + diffZ * diffZ)
        var pitch = -atan2(dist, diffY)
        var yaw = atan2(diffZ, diffX)
        pitch = ((pitch * 180.0) / Math.PI + 90.0) * -1.0 - Minecraft.getMinecraft().thePlayer.rotationPitch
        pitch %= 180.0
        while (pitch >= 180.0) pitch -= 180.0
        while (pitch < -180.0) pitch += 180.0
        yaw = (yaw * 180.0) / Math.PI - 90.0 - Minecraft.getMinecraft().thePlayer.rotationYaw
        yaw %= 360.0
        while (yaw >= 180.0) yaw -= 360.0
        while (yaw <= -180.0) yaw += 360.0

        return Pair(yaw, pitch)
    }

    fun diff(a: Double, b: Double): Double {
        return if (a > b) a - b else b - a
    }
}