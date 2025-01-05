package com.github.hul1an.maxpenisclient.commands

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

