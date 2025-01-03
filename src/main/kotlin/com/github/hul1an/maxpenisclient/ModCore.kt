package com.github.hul1an.maxpenisclient

import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraft.client.renderer.GlStateManager

@Mod(
    modid = ModCore.MOD_ID,
    name = ModCore.NAME,
    version = ModCore.VERSION,
    clientSideOnly = true
)

class ModCore {
    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        try {
            val resource: net.minecraft.client.resources.IResource = Minecraft.getMinecraft().getResourceManager()
                .getResource(net.minecraft.util.ResourceLocation("test:test.txt"))
            org.apache.commons.io.IOUtils.copy(resource.getInputStream(), java.lang.System.out)
        } catch (e: java.io.IOException) {
            throw java.lang.RuntimeException(e)
        }

        println("Dirt: ${Blocks.dirt.unlocalizedName}")
	    // Below is a demonstration of an access-transformed class access.
	    println("Color State: " + GlStateManager.Color());
    }


    companion object {
        const val MOD_ID = "maxpenisaddons"
        const val NAME = "MaxPenisAddons"
        const val VERSION = "1.0.0"
    }
}


