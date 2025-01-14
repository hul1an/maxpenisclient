package com.github.hul1an.maxpenisclient


import net.minecraft.client.Minecraft
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import com.github.hul1an.maxpenisclient.MyConfig.highliteMinerMainToggle
import com.github.hul1an.maxpenisclient.commands.*
import com.github.hul1an.maxpenisclient.features.HighliteMacro
import com.github.hul1an.maxpenisclient.utils.LocationUtils
import com.github.hul1an.maxpenisclient.utils.RouteWalker
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard


@Mod(
    modid = ModCore.MOD_ID,
    name = ModCore.NAME,
    version = ModCore.VERSION,
    clientSideOnly = true
)

class ModCore {

    val config = MyConfig
    val highliteMacro = HighliteMacro()
    val routeWalker = RouteWalker()


    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {

        MinecraftForge.EVENT_BUS.register(MyEventHandlerClass())
       // MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(KeyBindHandler(config.minerKeyBind, highliteMacro))
        MinecraftForge.EVENT_BUS.register(highliteMacro)
        MinecraftForge.EVENT_BUS.register(routeWalker)
        MinecraftForge.EVENT_BUS.register(LocationUtils)
        ClientCommandHandler.instance.registerCommand(CrashCommand())
        ClientCommandHandler.instance.registerCommand(RotationTest())
        ClientCommandHandler.instance.registerCommand(LocationTest())
        //ClientCommandHandler.instance.registerCommand(AstarWalkTest())
        ClientCommandHandler.instance.registerCommand(TestRouteWalkerRoute(routeWalker))
        ClientCommandHandler.instance.registerCommand(AddWaypointCommand(routeWalker))
        ClientCommandHandler.instance.registerCommand(RemoveWaypointCommand(routeWalker))


        try {
            val resource: net.minecraft.client.resources.IResource = Minecraft.getMinecraft().resourceManager
                .getResource(net.minecraft.util.ResourceLocation("test:test.txt"))
            org.apache.commons.io.IOUtils.copy(resource.inputStream, System.out)
        } catch (e: java.io.IOException) {
            throw java.lang.RuntimeException(e)
        }


    }


    companion object {
        const val MOD_ID = "maxpenisaddons"
        const val NAME = "MaxPenisAddons"
        const val VERSION = "1.1.0"
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

class KeyBindHandler(private val minerKeyBind: OneKeyBind, private val highliteMacro: HighliteMacro) {

    @SubscribeEvent
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        if (Keyboard.getEventKeyState() && minerKeyBind.isActive && highliteMinerMainToggle) {
            // Handle the key press event
            println("Miner key bind pressed")
            highliteMacro.toggle()
        }



    }
}






