package com.github.hul1an.maxpenisclient.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.Timer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import java.lang.reflect.Field
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class BlockRenderer {
    private var blockVBO: Int? = null
    private var blockIBO: Int? = null
    private var blockVBODataChanged = true
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: IntBuffer? = null
    private val blockIndices = intArrayOf(
        0, 1, 2, 3,
        4, 5, 6, 7,
        0, 4, 5, 1,
        2, 6, 7, 3,
        1, 5, 6, 2,
        3, 7, 4, 0
    )
    private val cubeVerticesCache = mutableMapOf<String, FloatArray>()

    private fun drawNormalCubeVertices(x: Float, y: Float, z: Float, w: Float, h: Float): FloatArray {
        return floatArrayOf(
            x, y, z,
            x + w, y, z,
            x + w, y + h, z,
            x, y + h, z,
            x, y, z + w,
            x + w, y, z + w,
            x + w, y + h, z + w,
            x, y + h, z + w
        )
    }

    private fun getCubeVertices(x: Float, y: Float, z: Float, w: Float, h: Float): FloatArray {
        val cacheKey = "$x-$y-$z"
        return cubeVerticesCache.getOrPut(cacheKey) { drawNormalCubeVertices(x, y, z, w, h) }
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(data).flip()
        return buffer
    }

    private fun createIntBuffer(data: IntArray): IntBuffer {
        val buffer = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
        buffer.put(data).flip()
        return buffer
    }

    private fun updateBlockVBO(positions: List<FloatArray>, w: Float, h: Float) {
        if (blockVBO == null) {
            blockVBO = GL15.glGenBuffers()
        }

        if (!blockVBODataChanged) {
            return
        }

        val vertexData = mutableListOf<Float>()
        positions.forEach { position ->
            val cubeVertices = getCubeVertices(position[0], position[1], position[2], w, h)
            vertexData.addAll(cubeVertices.toList())
        }

        if (vertexBuffer == null || vertexBuffer!!.capacity() < vertexData.size) {
            vertexBuffer = createFloatBuffer(vertexData.toFloatArray())
        } else {
            vertexBuffer!!.clear()
            vertexBuffer!!.put(vertexData.toFloatArray()).flip()
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, blockVBO!!)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer!!, GL15.GL_STATIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }

    private fun updateBlockIBO(positions: List<FloatArray>) {
        if (blockIBO == null) {
            blockIBO = GL15.glGenBuffers()
        }

        if (!blockVBODataChanged) {
            return
        }

        val indices = mutableListOf<Int>()
        for (i in positions.indices) {
            for (j in blockIndices.indices) {
                indices.add(i * 8 + blockIndices[j])
            }
        }

        if (indexBuffer == null || indexBuffer!!.capacity() < indices.size) {
            indexBuffer = createIntBuffer(indices.toIntArray())
        } else {
            indexBuffer!!.clear()
            indexBuffer!!.put(indices.toIntArray()).flip()
        }

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, blockIBO!!)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer!!, GL15.GL_STATIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun drawMultipleBlocksInWorld(
        positions: List<FloatArray>, r: Float, g: Float, b: Float, a: Float = 1f,
        depthTest: Boolean = true, filled: Boolean = false, additiveBlend: Boolean = false,
        w: Float = 1f, h: Float = 1f
    ) {
        if (positions.isEmpty()) return

        updateBlockVBO(positions, w, h)
        updateBlockIBO(positions)

        if (blockVBODataChanged) blockVBODataChanged = false
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT)

        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_BLEND)
        if (additiveBlend) {
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE)
        } else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        }
        GL11.glDepthMask(false)
        GL11.glDisable(GL11.GL_TEXTURE_2D)

        if (!depthTest) GL11.glDisable(GL11.GL_DEPTH_TEST)

        GL11.glPushMatrix()

        // Placeholder for Player.getRenderX(), Player.getRenderY(), Player.getRenderZ()
        val renderX = 0.0
        val renderY = 0.0
        val renderZ = 0.0
        GL11.glTranslated(renderX, renderY, renderZ)

        GL11.glColor4f(r, g, b, a)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, blockVBO!!)
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY)
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, blockIBO!!)

        if (!filled) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
            GL11.glDrawElements(GL11.GL_QUADS, positions.size * 24, GL11.GL_UNSIGNED_INT, 0)
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
        } else {
            GL11.glDrawElements(GL11.GL_QUADS, positions.size * 24, GL11.GL_UNSIGNED_INT, 0)
        }

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)

        GL11.glPopMatrix()

        if (!depthTest) GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_CULL_FACE)

        GL11.glPopAttrib()
    }

    /*fun drawMultipleBlocksInWorldWithNumbers(
        positions: List<FloatArray>, rgb: FloatArray, a: Float = 1f,
        depthTest: Boolean = true, filled: Boolean = false, additiveBlend: Boolean = false,
        w: Float = 1f, h: Float = 1f
    ) {
        drawMultipleBlocksInWorld(positions, rgb[0], rgb[1], rgb[2], a, depthTest, filled, additiveBlend, w, h)
        for (i in positions.indices) {
            drawString((i + 1).toString(), positions[i], floatArrayOf(255f, 255f, 255f), 1f, true)
        }
    }*/

     fun drawString(name: String, location: Array<Double>, color: FloatArray = floatArrayOf(128f, 128f, 128f), size: Float = 0.3f, increase: Boolean = false) {
        val fontRenderer = Minecraft.getMinecraft().fontRendererObj
        val x = (location[0] + 0.5).toFloat()
        val y = (location[1] + 0.5).toFloat()
        val z = (location[2] + 0.5).toFloat()
        val scale = size * 0.016666668f

        GL11.glPushMatrix()
        GL11.glTranslatef(x, y, z)
        GL11.glScalef(scale, scale, scale)
        GL11.glNormal3f(0.0f, 1.0f, 0.0f)
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0f, 1.0f, 0.0f)
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0f, 0.0f, 0.0f)
        GL11.glRotatef(180f, 0.0f, 0.0f, 1.0f) // Flip the text upside down
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDepthMask(false)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        OpenGlHelper.glBlendFunc(770, 771, 1, 0)
        val textWidth = fontRenderer.getStringWidth(name) / 2
        val colorInt = (color[0].toInt() shl 16) or (color[1].toInt() shl 8) or color[2].toInt()
        fontRenderer.drawString(name, -textWidth, 0, colorInt)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
    }

    fun renderCords(cords: Array<Array<Double>>, rgb: FloatArray = floatArrayOf(0f, 255f, 255f), alpha: Float = 1f, full: Boolean = false) {
        for (cord in cords) {
            println("Rendering cord at (${cord.joinToString()}) with color ${rgb.joinToString()} and alpha $alpha")
            if (!full) {
                RenderUtils.drawEspBox(cord[0] + 0.5f, cord[1], cord[2] + 0.5f, 1f, 1f, rgb[0], rgb[1], rgb[2], alpha, true)
            } else {
                RenderUtils.drawInnerEspBox(cord[0] + 0.5f, cord[1], cord[2] + 0.5f, 1f, 1f, rgb[0], rgb[1], rgb[2], alpha, true)
            }
        }
    }

   /* fun renderCubes(cords: List<FloatArray>, h: Float, w: Float, rgb: FloatArray = floatArrayOf(0f, 1f, 0f), alpha: Float = 1f, full: Boolean = false) {
        for (cord in cords) {
            if (!full) {
                RenderUtils.drawEspBox(cord[0] + 0.5f, cord[1], cord[2] + 0.5f, h, w, rgb[0], rgb[1], rgb[2], alpha, true)
            } else {
                RenderUtils.drawInnerEspBox(cord[0] + 0.5f, cord[1], cord[2] + 0.5f, h, w, rgb[0], rgb[1], rgb[2], alpha, true)
            }
        }
    }*/

    fun renderCordsWithNumbers(cords: Array<Array<Double>>, rgb: FloatArray = floatArrayOf(0f, 255f, 255f), alpha: Float = 0.2f, full: Boolean = true) {
        val player = Minecraft.getMinecraft().thePlayer
        val timerField: Field = Minecraft::class.java.getDeclaredField("timer")
        timerField.isAccessible = true
        val timer = timerField.get(Minecraft.getMinecraft()) as Timer
        val partialTicks = timer.renderPartialTicks
        val playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
        val playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
        val playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks

        val transformedCords = cords.map { cord ->
            arrayOf(cord[0] - playerX, cord[1] - playerY, cord[2] - playerZ)
        }.toTypedArray()

        renderCords(transformedCords, rgb, alpha, full)
        for (i in transformedCords.indices) {
            drawString((i + 1).toString(), transformedCords[i], floatArrayOf(0f, 255f, 255f), 2f, true)
        }
    }

 /*   fun renderCube(cord: FloatArray, rgb: FloatArray = floatArrayOf(0f, 0f, 1f), full: Boolean = false, alpha: Float = 1f, w: Float = 1f, h: Float = 1f) {
        if (!full) {
            RenderUtils.drawEspBox(cord[0] + 0.5f, cord[1], cord[2] + 0.5f, w, h, rgb[0], rgb[1], rgb[2], alpha, true)
        } else {
            RenderUtils.drawInnerEspBox(cord[0] + 0.5f, cord[1], cord[2] + 0.5f, w, h, rgb[0], rgb[1], rgb[2], alpha, true)
        }
    }*/

    fun renderLines(cords: List<FloatArray>, color: FloatArray = floatArrayOf(0f, 0f, 0f), m: FloatArray = floatArrayOf(0f, 0f, 0f)) {
        var prev: FloatArray? = null
        cords.forEach { cord ->
            if (prev != null) {
                drawLine(floatArrayOf(prev!![0] + m[0], prev!![1] + m[1], prev!![2] + m[2]), floatArrayOf(cord[0] + m[0], cord[1] + m[1], cord[2] + m[2]), color)
            }
            prev = cord
        }
    }

    fun drawLine(cords1: FloatArray, cords2: FloatArray, color: FloatArray = floatArrayOf(0f, 0f, 0f), alpha: Float = 1f, thickness: Float = 0.6f, phase: Boolean = true) {
        // Placeholder for drawLine3d
        // drawLine3d(cords1[0], cords1[1], cords1[2], cords2[0], cords2[1], cords2[2], color[0], color[1], color[2], alpha, thickness, phase)
    }

    /*fun renderPathfindingLines(cords: List<FloatArray>) {
        renderCords(cords, floatArrayOf(0.2f, 0.47f, 1f), 0.2f, true)
        renderLines(cords, floatArrayOf(0.2f, 0.47f, 1f), floatArrayOf(0.5f, 1f, 0.5f))
    }*/
}