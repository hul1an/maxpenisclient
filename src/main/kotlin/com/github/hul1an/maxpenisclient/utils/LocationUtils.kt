package com.github.hul1an.maxpenisclient.utils

import com.github.hul1an.maxpenisclient.clock.Executor
import com.github.hul1an.maxpenisclient.clock.Executor.Companion.register
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.minecraftforge.event.world.WorldEvent

@OptIn(ExperimentalStdlibApi::class)
object LocationUtils {

    private var isOnHypixel: Boolean = false
    var isInSkyblock: Boolean = false

    var currentArea: Island = Island.Unknown
    var kuudraTier: Int = 0

    init {
        Executor(500, "LocationUtils") {
            if (!isInSkyblock)
                isInSkyblock = isOnHypixel && mc.theWorld?.scoreboard?.getObjectiveInDisplaySlot(1)?.let { cleanSB(it.displayName).contains("SKYBLOCK") } == true

            if (currentArea.isArea(Island.Kuudra) && kuudraTier == 0)
                sidebarLines.find { cleanLine(it).contains("Kuudra's Hollow (") }?.let {
                    kuudraTier = it.substringBefore(")").lastOrNull()?.digitToIntOrNull() ?: 0 }

            if (currentArea.isArea(Island.Unknown)) {
                currentArea = getArea()
                println("Updated currentArea: $currentArea")
            }

        }.register()
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        isOnHypixel = false
        isInSkyblock = false
        currentArea = Island.Unknown
        kuudraTier = 0
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        isInSkyblock = false
        kuudraTier = 0
        currentArea = Island.Unknown
    }

    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        isOnHypixel = mc.runCatching {
            !event.isLocal && ((thePlayer?.clientBrand?.contains("hypixel", true) ?: currentServerData?.serverIP?.contains("hypixel", true)) == true)
        }.getOrDefault(false)
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        if (isOnHypixel || event.packet !is S3FPacketCustomPayload || event.packet.channelName != "MC|Brand") return
        if (event.packet.bufferData?.readStringFromBuffer(Short.MAX_VALUE.toInt())?.contains("hypixel", true) == true) isOnHypixel = true
    }

    private fun getArea(): Island {
        if (mc.isSingleplayer) return Island.SinglePlayer
        if (!isInSkyblock) return Island.Unknown
        val netHandlerPlayClient: NetHandlerPlayClient = mc.thePlayer?.sendQueue ?: return Island.Unknown
        val list = netHandlerPlayClient.playerInfoMap ?: return Island.Unknown

        val area = list.find {
            it?.displayName?.unformattedText?.startsWith("Area: ") == true ||
                    it?.displayName?.unformattedText?.startsWith("Dungeon: ") == true
        }?.displayName?.formattedText

        println("Detected area: $area")

        return Island.entries.firstOrNull { area?.contains(it.displayName, true) == true } ?: Island.Unknown
    }
}

open class PacketEvent(val packet: Packet<*>) : Event() {

    @Cancelable
    class Receive(packet: Packet<*>) : PacketEvent(packet)

    @Cancelable
    class Send(packet: Packet<*>) : PacketEvent(packet)
}