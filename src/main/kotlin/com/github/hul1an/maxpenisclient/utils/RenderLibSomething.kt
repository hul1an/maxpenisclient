package com.github.hul1an.maxpenisclient.utils

import org.lwjgl.opengl.GL11
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats

object RenderUtils {

    fun drawEspBox(x: Double, y: Double, z: Double, w: Float, h: Float, red: Float, green: Float, blue: Float, alpha: Float, phase: Boolean) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.worldRenderer

        GL11.glLineWidth(2.0f)
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()

        if (phase) {
            GlStateManager.disableDepth()
        }

        val locations = arrayOf(
            arrayOf(floatArrayOf(0f, 0f, 0f), floatArrayOf(w, 0f, 0f)),
            arrayOf(floatArrayOf(0f, 0f, 0f), floatArrayOf(0f, 0f, w)),
            arrayOf(floatArrayOf(w, 0f, w), floatArrayOf(w, 0f, 0f)),
            arrayOf(floatArrayOf(w, 0f, w), floatArrayOf(0f, 0f, w)),
            arrayOf(floatArrayOf(0f, h, 0f), floatArrayOf(w, h, 0f)),
            arrayOf(floatArrayOf(0f, h, 0f), floatArrayOf(0f, h, w)),
            arrayOf(floatArrayOf(w, h, w), floatArrayOf(w, h, 0f)),
            arrayOf(floatArrayOf(w, h, w), floatArrayOf(0f, h, w)),
            arrayOf(floatArrayOf(0f, 0f, 0f), floatArrayOf(0f, h, 0f)),
            arrayOf(floatArrayOf(w, 0f, 0f), floatArrayOf(w, h, 0f)),
            arrayOf(floatArrayOf(0f, 0f, w), floatArrayOf(0f, h, w)),
            arrayOf(floatArrayOf(w, 0f, w), floatArrayOf(w, h, w))
        )

        locations.forEach { loc ->
            //println("Drawing line from (${x + loc[0][0] - w / 2}, ${y + loc[0][1]}, ${z + loc[0][2] - w / 2}) to (${x + loc[1][0] - w / 2}, ${y + loc[1][1]}, ${z + loc[1][2] - w / 2})")
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR)
            buffer.pos((x + loc[0][0] - w / 2).toDouble(), (y + loc[0][1]).toDouble(), (z + loc[0][2] - w / 2).toDouble()).color(red, green, blue, alpha).endVertex()
            buffer.pos((x + loc[1][0] - w / 2).toDouble(), (y + loc[1][1]).toDouble(), (z + loc[1][2] - w / 2).toDouble()).color(red, green, blue, alpha).endVertex()
            tessellator.draw()
        }

        GlStateManager.enableCull()
        GlStateManager.disableBlend()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()

        if (phase) {
            GlStateManager.enableDepth()
        }
    }

    fun drawInnerEspBox(x: Double, y: Double, z: Double, w: Float, h: Float, red: Float, green: Float, blue: Float, alpha: Float, phase: Boolean) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.worldRenderer

        GL11.glLineWidth(2.0f)
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()

        if (phase) {
            GlStateManager.disableDepth()
        }

        val halfW = w / 2

        //println("Drawing filled box at ($x, $y, $z) with width $w and height $h")

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        buffer.pos((x + halfW).toDouble(), y.toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x + halfW).toDouble(), y.toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x - halfW).toDouble(), y.toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x - halfW).toDouble(), y.toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()

        buffer.pos((x + halfW).toDouble(), (y + h).toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x + halfW).toDouble(), (y + h).toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x - halfW).toDouble(), (y + h).toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x - halfW).toDouble(), (y + h).toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()

        buffer.pos((x - halfW).toDouble(), (y + h).toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x - halfW).toDouble(), (y + h).toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x - halfW).toDouble(), y.toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x - halfW).toDouble(), y.toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()

        buffer.pos((x + halfW).toDouble(), (y + h).toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x + halfW).toDouble(), (y + h).toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x + halfW).toDouble(), y.toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x + halfW).toDouble(), y.toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()

        buffer.pos((x + halfW).toDouble(), (y + h).toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x - halfW).toDouble(), (y + h).toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x - halfW).toDouble(), y.toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x + halfW).toDouble(), y.toDouble(), (z - halfW).toDouble()).color(red, green, blue, alpha).endVertex()

        buffer.pos((x - halfW).toDouble(), (y + h).toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x + halfW).toDouble(), (y + h).toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x + halfW).toDouble(), y.toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        buffer.pos((x - halfW).toDouble(), y.toDouble(), (z + halfW).toDouble()).color(red, green, blue, alpha).endVertex()
        tessellator.draw()

        GlStateManager.enableCull()
        GlStateManager.disableBlend()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()

        if (phase) {
            GlStateManager.enableDepth()
        }
    }
}