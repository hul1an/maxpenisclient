package com.github.hul1an.maxpenisclient.utils

import cc.polyfrost.oneconfig.libs.universal.ChatColor.Companion.FORMATTING_CODE_PATTERN
import net.minecraft.client.Minecraft
import net.minecraft.network.Packet
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3i
import java.util.*

class UtilsClass {

    fun sendPacket(packet: Packet<*>) {
        Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(packet)
    }

    fun isLookingAtPos(pos: BlockPos): Boolean {
        val block = Minecraft.getMinecraft().thePlayer.rayTrace(4.5, 1.0F)?.blockPos
        return block != null && block == pos
    }

    fun isMiningPos(pos: BlockPos): Boolean {
        return this.isLookingAtPos(pos) && Minecraft.getMinecraft().playerController.isHittingBlock
    }

    /**
     * @param pos The BlockPos to check
     * @return whether the block is low enough to walk over
     */
    fun isWalkable(pos: BlockPos): Boolean {
        val world = Minecraft.getMinecraft().theWorld
        val block = world.getBlockState(pos).block
        val material = block.material

        if (material.isLiquid || (!material.isSolid && block.registryName.toString() != "minecraft:snow_layer")) {
            return false
        }

        var totalHeight = 0.0
        val blockType1 = world.getBlockState(pos.up(1)).block
        val blockType2 = world.getBlockState(pos.up(2)).block
        val blockType3 = world.getBlockState(pos.up(3)).block

        if (blockType1.registryName.toString() != "minecraft:air") {
            totalHeight += blockType1.blockBoundsMaxY
        }
        if (blockType2.registryName.toString() != "minecraft:air") {
            totalHeight += blockType2.blockBoundsMaxY
        }
        if (blockType3.registryName.toString() != "minecraft:air") {
            totalHeight += blockType3.blockBoundsMaxY
        }

        return totalHeight < 0.6
    }

    fun playerIsCollided(): Boolean {
        val player = Minecraft.getMinecraft().thePlayer
        val abb = player.entityBoundingBox
        val boxes = this.getBlocks()
        for (box in boxes) {
            if (box.intersectsWith(abb)) {
                return true
            }
        }
        return false
    }

    fun getBlocks(): List<AxisAlignedBB> {
        val cords = listOf(Math.floor(Minecraft.getMinecraft().thePlayer.posX), Math.floor(Minecraft.getMinecraft().thePlayer.posY), Math.floor(Minecraft.getMinecraft().thePlayer.posZ))
        val boxes = mutableListOf<AxisAlignedBB>()
        val world = Minecraft.getMinecraft().theWorld

        for (x in -1..1) {
            for (z in -1..1) {
                for (y in 0..1) {
                    val blockPos = BlockPos(cords[0].toInt() + x, cords[1].toInt() + y, cords[2].toInt() + z)
                    val block = world.getBlockState(blockPos).block
                    if (block.getBlockHardness(world, blockPos) != 1.0f || block.registryName.toString() == "minecraft:air") continue
                    boxes.add(AxisAlignedBB(cords[0] + x - 0.01, cords[1] + y, cords[2] + z - 0.01, cords[0] + x + 1.01, cords[1] + y + 1.0, cords[2] + z + 1.01))
                }
            }
        }
        return boxes
    }

    /**
     * Profiles the specified function with the specified string as profile section name.
     * Uses the minecraft profiler. skidded from Odin
     *
     * @param name The name of the profile section.
     * @param func The code to profile.
     */
    inline fun profile(name: String, func: () -> Unit) {
        startProfile(name)
        func()
        endProfile()
    }
    /**
     * Starts a minecraft profiler section with the specified name + "Maxpenis: ".
     * */
    fun startProfile(name: String) {
        mc.mcProfiler.startSection("Maxpenis: $name")
    }

    /**
     * Ends the current minecraft profiler section.
     */
    fun endProfile() {
        mc.mcProfiler.endSection()
    }





    val mc: Minecraft = Minecraft.getMinecraft()


}



