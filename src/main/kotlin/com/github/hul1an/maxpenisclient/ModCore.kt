package com.github.hul1an.maxpenisclient

import com.github.hul1an.maxpenisclient.commands.CrashCommand
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


@Mod(
    modid = ModCore.MOD_ID,
    name = ModCore.NAME,
    version = ModCore.VERSION,
    clientSideOnly = true
)

class ModCore {
    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(MyEventHandlerClass())
        MinecraftForge.EVENT_BUS.register(this)
        ClientCommandHandler.instance.registerCommand(CrashCommand())

        try {
            val resource: net.minecraft.client.resources.IResource = Minecraft.getMinecraft().resourceManager
                .getResource(net.minecraft.util.ResourceLocation("test:test.txt"))
            org.apache.commons.io.IOUtils.copy(resource.inputStream, System.out)
        } catch (e: java.io.IOException) {
            throw java.lang.RuntimeException(e)
        }

        println("Dirt: ${Blocks.dirt.unlocalizedName}")
	    // Below is a demonstration of an access-transformed class access.
	    println("Color State: " + GlStateManager.Color())
    }


    companion object {
        const val MOD_ID = "maxpenisaddons"
        const val NAME = "MaxPenisAddons"
        const val VERSION = "1.0.0"
    }
}

class MyEventHandlerClass {
    var cheeseCount = 0

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.message.unformattedText.contains("cheese")) {
            val cheeseEvent = CheeseEvent(cheeseCount++)
            MinecraftForge.EVENT_BUS.post(cheeseEvent)
            if (cheeseEvent.isCanceled) {
                event.isCanceled = true
            }
        }
        if (event.message.unformattedText.contains("fuck")) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onCheese(event: CheeseEvent) {
        if (event.totalCheeseCount > 10) {
            // more than 10 cheese messages is unacceptable
            event.isCanceled = true
        }
    }
}

@Cancelable
class CheeseEvent(val totalCheeseCount: Int) : Event()








