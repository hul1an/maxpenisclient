package com.github.hul1an.maxpenisclient.commands

import com.github.hul1an.maxpenisclient.utils.RouteWalker
import com.github.hul1an.maxpenisclient.utils.Rotations
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.event.ClickEvent
import net.minecraft.init.Blocks
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.LogManager
import kotlin.reflect.KParameter

import com.github.hul1an.maxpenisclient.utils.Waypoint
import net.minecraft.command.CommandXP


class CrashCommand: CommandBase() {
    override fun getCommandName(): String {
        return "crashme"
    }

    override fun getCommandUsage(sender: ICommandSender?): String  { //instead of @override public string or public void or whatever it is in java, you just do override fun then declare what kind of variable after the colon
        return ""
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (args != null && args.size == 1 && args[0] == "confirm") {
            LogManager.getLogger("CrashCommand").info("Crashing the game")
            FMLCommonHandler.instance().exitJava(1, false)
        } else {
            Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("§aAre you sure you want to crash the game? Click to confirm.")
                .setChatStyle(ChatStyle()
                    .setChatClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/crashme confirm"))
                )
            )
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }

    override fun getCommandAliases(): List<String> {
        return listOf("dontcrashme")
    }

}

class RotationTest: CommandBase() {
    private val rotation = Rotations()
    override fun getCommandName(): String {
        return "rotateme"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return ""
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        val origin = Vec3(0.0, 0.0, 0.0)
        rotation.rotateTo(origin)
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }

}


class AddWaypointCommand(private val routeWalker: RouteWalker) : CommandBase() {
    override fun getCommandName(): String {
        return "addwaypoint"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/addwaypoint <area> <name> <x> <y> <z>"
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (args != null && args.size == 5) {
            val areaName = args[0]
            val waypointName = args[1]
            val x = args[2].toDoubleOrNull()
            val y = args[3].toDoubleOrNull()
            val z = args[4].toDoubleOrNull()

            if (x != null && y != null && z != null) {
                val waypoint = Waypoint(waypointName, x, y, z)
                routeWalker.addWaypoint(areaName, waypoint)
                sender?.addChatMessage(ChatComponentText("Waypoint added: $waypointName at ($x, $y, $z) in area $areaName"))
            } else {
                sender?.addChatMessage(ChatComponentText("Invalid coordinates"))
            }
        } else {
            sender?.addChatMessage(ChatComponentText("Usage: ${getCommandUsage(sender)}"))
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }
}
class RemoveWaypointCommand(private val routeWalker: RouteWalker) : CommandBase() {
    override fun getCommandName(): String {
        return "removewaypoint"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/removewaypoint <area> <name>"
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (args != null && args.size == 2) {
            val areaName = args[0]
            val waypointName = args[1]

            if (routeWalker.removeWaypoint(areaName, waypointName)) {
                sender?.addChatMessage(ChatComponentText("Waypoint removed: $waypointName from area $areaName"))
            } else {
                sender?.addChatMessage(ChatComponentText("Waypoint not found: $waypointName in area $areaName"))
            }
        } else {
            sender?.addChatMessage(ChatComponentText("Usage: ${getCommandUsage(sender)}"))
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }
}

class TestRouteWalkerRoute(private val routeWalker: RouteWalker) : CommandBase() {
    override fun getCommandName(): String {
        return "testroute"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/testroute <routeName>"
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (args != null && args.size == 1) {
            val routeName = args[0]
            val path = routeWalker.loadPathFromJson(routeName)
            routeWalker.setPath(path)
            routeWalker.toggle()

        }else {
            sender?.addChatMessage(ChatComponentText("Usage: ${getCommandUsage(sender)}"))
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }

}



