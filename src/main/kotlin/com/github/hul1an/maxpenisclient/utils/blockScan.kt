package com.github.hul1an.maxpenisclient.utils

import io.netty.util.internal.EmptyArrays
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraft.util.BlockPos
import net.minecraft.block.Block
import net.minecraft.block.state.BlockState
import net.minecraft.init.Blocks
import kotlin.math.*


open class YouFuckedUpException(message: String) : Exception(message)

class BlockScanClass {
    var Enabled: Boolean = false

    val utils = UtilsClass()
    val mathUtils = MathUtilsClass()




    init {//same as dependencies in js

    MinecraftForge.EVENT_BUS.register((this)) // this might be necessary idk
        }

    val mc = Minecraft.getMinecraft()

    //functions go here

    fun toggle() {
        this.Enabled = !Enabled
    }

    @SubscribeEvent
    fun onRenderWorld (event: RenderWorldLastEvent) {
        //every frame (i think)
        if (!this.Enabled) {
            return
        }
    }

    object scanSize {
        fun defineSize(x:Double, y:Double, z:Double) {
        }
        var xOffset : Double = 5.0
        var yOffset : Double = 1.0
        var zOffset : Double = 5.0

        var xScanSize: Double = 11.0
        var yScanSize: Double = 5.0
        var zScanSize: Double = 11.0
    }

    fun scanPlayerArea(position: Vec3): MutableList<BlockPos> {
        val blockList: MutableList<BlockPos> = mutableListOf()

        val world = Minecraft.getMinecraft().theWorld
        if (world == null) {
            throw YouFuckedUpException("World is null")
        }

        for (xi in 0 until scanSize.xScanSize.toInt()) {
            for (yi in 0 until scanSize.yScanSize.toInt()) {
                for (zi in 0 until scanSize.zScanSize.toInt()) {
                    val blockX = position.xCoord + (xi.toDouble() - scanSize.xOffset)
                    val blockY = position.yCoord + (yi.toDouble() - scanSize.yOffset)
                    val blockZ = position.zCoord + (zi.toDouble() - scanSize.zOffset)

                    try {
                        val pos = BlockPos(blockX, blockY, blockZ)
                        val blockState = world.getBlockState(pos) ?: continue
                        val block = blockState.block
                        val metadata = block.getMetaFromState(blockState)
                        if (metadata == 3 && block.registryName == "minecraft:stained_glass_pane") {
                            blockList.add(pos)
                        }
                    } catch (e: YouFuckedUpException) {
                        println("Your Scan feature shit the bed")
                    }
                }
            }
        }
        return blockList
    }


    //Full sort scan takes a scan of all blocks in a set area, then filters them out so only blocks that are: visible + reachable are in the list, and it is sorted too be closest too futhest
    fun fullSortScan():MutableList<BlockPos> {
        var playerPos: Vec3 = Vec3(
            Minecraft.getMinecraft().thePlayer.position.x.toDouble(),
            Minecraft.getMinecraft().thePlayer.position.y.toDouble(),
            Minecraft.getMinecraft().thePlayer.position.z.toDouble()
        )
        var reach : Double = 4.0

        var list = distSort(rayTraceFilter(reachFilter(scanPlayerArea(playerPos),reach)))

        return list
    }
    //Dist Sort Sorts all BlockPos in order from closest too furthest from player eyes
    fun distSort(blockList: MutableList<BlockPos>): MutableList<BlockPos> {
        return blockList.sortedBy{mathUtils.getDistanceToPlayerEyes(it.x, it.y, it.z)["distance"]}.toMutableList()
    }
    //Reach Filter filters out all blocks that the player cannot reach within a set distance from the eyes
    fun reachFilter(blockList: MutableList<BlockPos>, reach: Double): MutableList<BlockPos> {
        val filteredList: MutableList<BlockPos> = mutableListOf()
        for (blockPos in blockList) {
            val distance = mathUtils.getDistanceToPlayerEyes(blockPos.x, blockPos.y, blockPos.z)["distance"]
            if (distance != null && distance < reach) {
                filteredList.add(blockPos)
            }
        }
        return filteredList
    }
    //RayTrace Filter filters out all blocks that are not visible to the player
    fun rayTraceFilter(blockList: MutableList<BlockPos>): MutableList<BlockPos> {
        val filteredList: MutableList<BlockPos> = mutableListOf()
        val playerEyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)

        for (blockPos in blockList) {
            val point = rayTraceUtils.getPointOnBlock(blockPos, playerEyes, mcCast = true)
            if (point != null) {
                filteredList.add(blockPos)
            }
        }
        return filteredList
    }
}


