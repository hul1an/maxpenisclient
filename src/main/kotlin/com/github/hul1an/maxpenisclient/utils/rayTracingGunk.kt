package com.github.hul1an.maxpenisclient.utils

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.world.World
import kotlin.math.abs
import kotlin.math.floor

class RayTraceUtils {

    var sides: Array<DoubleArray> = arrayOf(
        doubleArrayOf(0.01, 0.5, 0.5),
        doubleArrayOf(0.99, 0.5, 0.5),
        doubleArrayOf(0.5, 0.5, 0.01),
        doubleArrayOf(0.5, 0.5, 0.99),
        doubleArrayOf(0.5, 0.04, 0.5),
        doubleArrayOf(0.5, 0.96, 0.5)
    )

    fun setSides(sides: Array<DoubleArray>) {
        this.sides = sides
    }

    fun returnPointsFromSides(sides: Array<DoubleArray>, blockPos: BlockPos): List<DoubleArray> {
        val returnArray = mutableListOf(
            doubleArrayOf(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5),
            doubleArrayOf(blockPos.x + 0.5, blockPos.y + 0.3, blockPos.z + 0.5),
            doubleArrayOf(blockPos.x + 0.5, blockPos.y + 0.7, blockPos.z + 0.5)
        )
        sides.forEach { side ->
            returnArray.add(doubleArrayOf(blockPos.x + side[0], blockPos.y + side[1], blockPos.z + side[2]))
        }
        val eyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)
        for (v in sides.indices) {
            val x = blockPos.x
            val y = blockPos.y
            val z = blockPos.z
            val (sideX, sideY, sideZ) = sides[v]
            if (sideX == 0.99 && x + sideX > eyes.xCoord) continue
            if (sideX == 0.01 && x + sideX < eyes.xCoord) continue
            if (sideY == 0.99 && y + sideY > eyes.yCoord) continue
            if (sideY == 0.01 && y + sideY < eyes.yCoord) continue
            if (sideZ == 0.99 && z + sideZ > eyes.zCoord) continue
            if (sideZ == 0.01 && z + sideZ < eyes.zCoord) continue
            for (i in 0..8) {
                val point = 0.1 * i + 0.1
                if (sideZ == 0.01 || sideZ == 0.99) {
                    for (s in 0..8) {
                        val pointZ = 0.1 * s + 0.1
                        returnArray.add(doubleArrayOf(x + pointZ, y + point, z + sideZ))
                    }
                }
                if (sideY == 0.01 || sideY == 0.99) {
                    for (s in 0..8) {
                        val pointY = 0.1 * s + 0.1
                        returnArray.add(doubleArrayOf(x + point, y + sideY, z + pointY))
                    }
                }
                if (sideX == 0.01 || sideX == 0.99) {
                    for (s in 0..8) {
                        val pointX = 0.1 * s + 0.1
                        returnArray.add(doubleArrayOf(x + sideX, y + point, z + pointX))
                    }
                }
            }
        }
        return returnArray
    }

    fun getLittlePointsOnBlock(pos: BlockPos): List<DoubleArray> {
        val returnArray = mutableListOf<DoubleArray>()
        val sides = arrayOf(
            doubleArrayOf(0.5, 0.5, 0.5),
            doubleArrayOf(0.5, 0.25, 0.5),
            doubleArrayOf(0.5, 0.75, 0.5),
            doubleArrayOf(0.05, 0.5, 0.5),
            doubleArrayOf(0.95, 0.5, 0.5),
            doubleArrayOf(0.5, 0.5, 0.05),
            doubleArrayOf(0.5, 0.5, 0.95),
            doubleArrayOf(0.5, 0.05, 0.5),
            doubleArrayOf(0.5, 0.95, 0.5)
        )
        sides.forEach { side ->
            returnArray.add(doubleArrayOf(pos.x + side[0], pos.y + side[1], pos.z + side[2]))
        }
        return returnArray
    }

    fun check(block: Block): Boolean {
        return block.material != Material.air
    }

    fun toFloat(number: Double): Float {
        return number.toFloat()
    }

    fun getPointOnBlock(blockPos: BlockPos, vector: Vec3 = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f), mcCast: Boolean = false, performance: Boolean = false): DoubleArray? {
        val points = if (performance) getLittlePointsOnBlock(blockPos) else returnPointsFromSides(sides, blockPos)
        for (point in points) {
            if (mcCast) {
                if (canSeePointMC(blockPos, point, vector)) {
                    return point
                }
                continue
            }
            if (canSeePoint(blockPos, point, vector)) {
                return point
            }
        }
        return null
    }



    fun canHitVec3(vector1: Vec3, vector2: Vec3): Boolean {
        val castResult = Minecraft.getMinecraft().theWorld.rayTraceBlocks(vector1, vector2)
        return castResult != null && castResult.blockPos == BlockPos(vector2)
    }

    fun canSeePointMC(blockPos: BlockPos, point: DoubleArray, vector: Vec3 = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)): Boolean {
        val castResult = Minecraft.getMinecraft().theWorld.rayTraceBlocks(vector, Vec3(point[0], point[1], point[2]))
        return castResult != null && castResult.blockPos == blockPos
    }

    fun canSeePoint(blockPos: BlockPos, point: DoubleArray, vector: Vec3 = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)): Boolean {
        val vectorX = point[0].toFloat() - vector.xCoord
        val vectorY = point[1].toFloat() - vector.yCoord
        val vectorZ = point[2].toFloat() - vector.zCoord
        val castResult = raytraceBlocks(
            Vec3(vector.xCoord, vector.yCoord, vector.zCoord),
            Vec3(vectorX.toDouble(), vectorY.toDouble(), vectorZ.toDouble()),
            60,
            ::check,
            true,
            false
        )
        return castResult.isNotEmpty() && castResult[0] == blockPos
    }

    fun rayTracePlayerBlocks(reach: Int = 60, checkFunction: ((Block) -> Boolean)? = null): List<BlockPos> {
        val eyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)
        return raytraceBlocks(Vec3(eyes.xCoord, eyes.yCoord, eyes.zCoord), null, reach, checkFunction, false, false)
    }

    fun rayTraceBetweenPoints(begin: DoubleArray, end: DoubleArray): List<BlockPos> {
        val vectorX = end[0] - begin[0]
        val vectorY = end[1] - begin[1]
        val vectorZ = end[2] - begin[2]
        val distance = Math.ceil(Math.sqrt((vectorX * vectorX) + (vectorY * vectorY) + (vectorZ * vectorZ))).toInt()
        return raytraceBlocks(Vec3(begin[0], begin[1], begin[2]), Vec3(vectorX, vectorY, vectorZ), distance, null, false, false)
    }

    fun rayTraceBlocks(reach: Int, vec: DoubleArray, direction: DoubleArray): List<BlockPos> {
        return raytraceBlocks(Vec3(vec[0], vec[1], vec[2]), Vec3(direction[0], direction[1], direction[2]), reach, null, false, false)
    }

    fun raytraceBlocks(
        startPos: Vec3? = null,
        directionVector: Vec3? = null,
        distance: Int = 60,
        blockCheckFunc: ((Block) -> Boolean)? = null,
        returnWhenTrue: Boolean = false,
        stopWhenNotAir: Boolean = true
    ): List<BlockPos> {
        // Set default values to send a raycast from the player's eye pos, along the player's look vector.
        val player = Minecraft.getMinecraft().thePlayer
        val start = startPos ?: player.getPositionEyes(1.0f)
        val direction = directionVector ?: player.lookVec

        val endPos = direction.normalize().multiply(distance.toDouble()).add(start)

        return traverseVoxels(start, endPos, blockCheckFunc, returnWhenTrue, stopWhenNotAir) as List<BlockPos>
    }
    fun Vec3.multiply(factor: Double): Vec3 {
        return Vec3(this.xCoord * factor, this.yCoord * factor, this.zCoord * factor)
    }

    data class RayTraceResult(val hit: BlockPos, val intersection: Vec3)

    fun traverseVoxels(
        start: Vec3,
        end: Vec3,
        blockCheckFunc: ((Block) -> Boolean)? = null,
        returnWhenTrue: Boolean = false,
        stopWhenNotAir: Boolean = false,
        returnIntersection: Boolean = false
    ): Any? {
        val direction = Vec3(end.xCoord - start.xCoord, end.yCoord - start.yCoord, end.zCoord - start.zCoord)
        val step = Vec3(Math.signum(direction.xCoord), Math.signum(direction.yCoord), Math.signum(direction.zCoord))
        val thing = Vec3(1.0 / direction.xCoord, 1.0 / direction.yCoord, 1.0 / direction.zCoord)
        val tDelta = Vec3(Math.min(thing.xCoord * step.xCoord, 1.0), Math.min(thing.yCoord * step.yCoord, 1.0), Math.min(thing.zCoord * step.zCoord, 1.0))
        var tMax = Vec3(
            abs((floor(start.xCoord) + step.xCoord.coerceAtLeast(0.0) - start.xCoord) * thing.xCoord),
            abs((floor(start.yCoord) + step.yCoord.coerceAtLeast(0.0) - start.yCoord) * thing.yCoord),
            abs((floor(start.zCoord) + step.zCoord.coerceAtLeast(0.0) - start.zCoord) * thing.zCoord)
        )

        var currentPos = BlockPos(floor(start.xCoord), floor(start.yCoord), floor(start.zCoord))
        val endPos = BlockPos(floor(end.xCoord), floor(end.yCoord), floor(end.zCoord))
        var intersectionPoint = start

        val path = mutableListOf<BlockPos>()
        var iters = 0
        while (true && iters < 1000) {
            iters++

            val currentBlock = Minecraft.getMinecraft().theWorld.getBlockState(currentPos).block
            if (blockCheckFunc != null && blockCheckFunc(currentBlock)) {
                if (returnWhenTrue) {
                    if (returnIntersection) return RayTraceResult(currentPos, intersectionPoint)
                    return currentPos
                }
                break
            }

            if (stopWhenNotAir && currentBlock.material != Material.air) {
                if (returnIntersection) return RayTraceResult(currentPos, intersectionPoint)
                break
            }

            path.add(currentPos)

            if (currentPos == endPos) break

            val minIndex = listOf(tMax.xCoord, tMax.yCoord, tMax.zCoord).indexOf(listOf(tMax.xCoord, tMax.yCoord, tMax.zCoord).minOrNull())
            when (minIndex) {
                0 -> {
                    tMax = tMax.add(Vec3(tDelta.xCoord, 0.0, 0.0))
                    currentPos = currentPos.add(step.xCoord.toInt(), 0, 0)
                }
                1 -> {
                    tMax = tMax.add(Vec3(0.0, tDelta.yCoord, 0.0))
                    currentPos = currentPos.add(0, step.yCoord.toInt(), 0)
                }
                2 -> {
                    tMax = tMax.add(Vec3(0.0, 0.0, tDelta.zCoord))
                    currentPos = currentPos.add(0, 0, step.zCoord.toInt())
                }
            }

            intersectionPoint = Vec3(start.xCoord + tDelta.xCoord * direction.xCoord, start.yCoord + tDelta.yCoord * direction.yCoord, start.zCoord + tDelta.zCoord * direction.zCoord)
        }
        if (returnWhenTrue) return null
        return path
    }
}

val rayTraceUtils = RayTraceUtils()