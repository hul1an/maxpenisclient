package com.github.hul1an.maxpenisclient.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

class LocationUtils {

    private var isOnHypixel: Boolean = false
    var isInSkyblock: Boolean = false
    var currentArea: Island = Island.Unknown

    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        isOnHypixel = Minecraft.getMinecraft().runCatching {
            !event.isLocal && ((Minecraft.getMinecraft().thePlayer?.clientBrand?.contains("hypixel", true) ?: currentServerData?.serverIP?.contains("hypixel", true)) == true)
        }.getOrDefault(false)
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        if (isOnHypixel || event.packet !is S3FPacketCustomPayload || event.packet.channelName != "MC|Brand") return
        if (event.packet.bufferData?.readStringFromBuffer(Short.MAX_VALUE.toInt())?.contains("hypixel", true) == true) isOnHypixel = true
    }

    /**
     * Returns the current area from the tab list info.
     * If no info can be found, return Island.Unknown.
     *
     * @author Aton
     */
    @OptIn(ExperimentalStdlibApi::class)
    private fun getArea(): Island {
        if (Minecraft.getMinecraft().isSingleplayer) return Island.SinglePlayer
        if (!isInSkyblock) return Island.Unknown
        val netHandlerPlayClient: NetHandlerPlayClient = Minecraft.getMinecraft().thePlayer?.sendQueue ?: return Island.Unknown
        val list = netHandlerPlayClient.playerInfoMap ?: return Island.Unknown

        val area = list.find {
            it?.displayName?.unformattedText?.startsWith("Area: ") == true ||
                    it?.displayName?.unformattedText?.startsWith("Dungeon: ") == true
        }?.displayName?.formattedText

        return Island.entries.firstOrNull { area?.contains(it.displayName, true) == true } ?: Island.Unknown
    }

}

open class PacketEvent(val packet: Packet<*>) : Event() {

    @Cancelable
    class Receive(packet: Packet<*>) : PacketEvent(packet)

    @Cancelable
    class Send(packet: Packet<*>) : PacketEvent(packet)
}