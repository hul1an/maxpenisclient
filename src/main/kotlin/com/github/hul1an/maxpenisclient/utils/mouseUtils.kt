package com.github.hul1an.maxpenisclient.utils

import net.minecraft.client.Minecraft
import net.minecraft.util.MouseHelper
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Mouse

object MouseUtils {
    val utils = UtilsClass()
    private val mc = Minecraft.getMinecraft()
    private var isUnGrabbed = false
    private var oldMouseHelper: MouseHelper? = null

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onGameUnload(event: WorldEvent.Unload) {
        reGrabMouse()
    }


    fun unGrabMouse() {
        if (isUnGrabbed) return
        mc.gameSettings.hideGUI = false
        if (oldMouseHelper == null) oldMouseHelper = mc.mouseHelper
        oldMouseHelper?.ungrabMouseCursor()
        mc.inGameHasFocus = true
        mc.mouseHelper = object : MouseHelper() {
            override fun mouseXYChange() {}
            override fun grabMouseCursor() {}
            override fun ungrabMouseCursor() {}
        }
        isUnGrabbed = true
        utils.customChat("ungrabbed")
    }

    fun reGrabMouse() {
        if (!isUnGrabbed) return
        if (oldMouseHelper == null) return
        mc.mouseHelper = oldMouseHelper
        mc.mouseHelper.grabMouseCursor()
        oldMouseHelper = null
        isUnGrabbed = false
        utils.customChat("regrabbed")
    }
}