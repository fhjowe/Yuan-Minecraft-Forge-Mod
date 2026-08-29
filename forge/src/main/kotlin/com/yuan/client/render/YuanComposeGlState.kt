package com.yuan.client.render

import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL13C
import org.lwjgl.opengl.GL14C
import org.lwjgl.opengl.GL20C
import org.lwjgl.opengl.GL21C
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL33C
import org.lwjgl.system.MemoryStack

/**
 * Captures the MC OpenGL state around a Skiko/Compose flush and restores it
 * afterwards so the rest of the game renders unchanged.
 */
object YuanComposeGlState {
    fun save(): State = State.capture()

    fun restore(state: State) = state.restore()

    fun resetPixelStore() {
        GL11C.glPixelStorei(GL11C.GL_UNPACK_SWAP_BYTES, GL11C.GL_FALSE)
        GL11C.glPixelStorei(GL11C.GL_UNPACK_LSB_FIRST, GL11C.GL_FALSE)
        GL11C.glPixelStorei(GL11C.GL_UNPACK_ROW_LENGTH, 0)
        GL11C.glPixelStorei(GL11C.GL_UNPACK_SKIP_ROWS, 0)
        GL11C.glPixelStorei(GL11C.GL_UNPACK_SKIP_PIXELS, 0)
        GL11C.glPixelStorei(GL11C.GL_UNPACK_ALIGNMENT, 4)
        GL21C.glBindBuffer(GL21C.GL_PIXEL_UNPACK_BUFFER, 0)
    }

    class State private constructor(
        private val blendEnabled: Boolean,
        private val blendSrcRgb: Int,
        private val blendDstRgb: Int,
        private val blendSrcAlpha: Int,
        private val blendDstAlpha: Int,
        private val depthTestEnabled: Boolean,
        private val depthMask: Boolean,
        private val depthFunc: Int,
        private val cullEnabled: Boolean,
        private val cullFace: Int,
        private val activeTexture: Int,
        private val textureBindings: IntArray,
        private val samplerBindings: IntArray,
        private val program: Int,
        private val vaoBinding: Int,
        private val colorMaskR: Boolean,
        private val colorMaskG: Boolean,
        private val colorMaskB: Boolean,
        private val colorMaskA: Boolean,
        private val unpackAlignment: Int,
        private val pixelUnpackBufferBinding: Int,
        private val unpackSwapBytes: Boolean,
        private val unpackLsbFirst: Boolean,
        private val unpackRowLength: Int,
        private val unpackSkipRows: Int,
        private val unpackSkipPixels: Int,
        private val pixelPackBufferBinding: Int,
        private val scissorEnabled: Boolean,
        private val scissorBox: IntArray,
        private val viewport: IntArray,
        private val drawFramebufferBinding: Int,
        private val readFramebufferBinding: Int,
        private val blendEquationRgb: Int,
        private val blendEquationAlpha: Int,
        private val arrayBufferBinding: Int,
        private val elementArrayBufferBinding: Int
    ) {
        fun restore() {
            if (blendEnabled) GL11C.glEnable(GL11C.GL_BLEND) else GL11C.glDisable(GL11C.GL_BLEND)
            GL14C.glBlendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha)
            GL20C.glBlendEquationSeparate(blendEquationRgb, blendEquationAlpha)

            if (depthTestEnabled) GL11C.glEnable(GL11C.GL_DEPTH_TEST) else GL11C.glDisable(GL11C.GL_DEPTH_TEST)
            GL11C.glDepthMask(depthMask)
            GL11C.glDepthFunc(depthFunc)

            if (cullEnabled) GL11C.glEnable(GL11C.GL_CULL_FACE) else GL11C.glDisable(GL11C.GL_CULL_FACE)
            GL11C.glCullFace(cullFace)

            for (i in textureBindings.indices) {
                GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + i)
                GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, textureBindings[i])
                GL33C.glBindSampler(i, samplerBindings[i])
            }
            GL13C.glActiveTexture(activeTexture)

            GL20C.glUseProgram(program)
            GL30C.glBindVertexArray(vaoBinding)

            GL11C.glColorMask(colorMaskR, colorMaskG, colorMaskB, colorMaskA)
            GL11C.glPixelStorei(GL11C.GL_UNPACK_ALIGNMENT, unpackAlignment)
            GL11C.glPixelStorei(GL11C.GL_UNPACK_SWAP_BYTES, if (unpackSwapBytes) GL11C.GL_TRUE else GL11C.GL_FALSE)
            GL11C.glPixelStorei(GL11C.GL_UNPACK_LSB_FIRST, if (unpackLsbFirst) GL11C.GL_TRUE else GL11C.GL_FALSE)
            GL11C.glPixelStorei(GL11C.GL_UNPACK_ROW_LENGTH, unpackRowLength)
            GL11C.glPixelStorei(GL11C.GL_UNPACK_SKIP_ROWS, unpackSkipRows)
            GL11C.glPixelStorei(GL11C.GL_UNPACK_SKIP_PIXELS, unpackSkipPixels)
            GL21C.glBindBuffer(GL21C.GL_PIXEL_UNPACK_BUFFER, pixelUnpackBufferBinding)
            GL21C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, pixelPackBufferBinding)
            GL21C.glBindBuffer(GL21C.GL_ARRAY_BUFFER, arrayBufferBinding)
            GL21C.glBindBuffer(GL21C.GL_ELEMENT_ARRAY_BUFFER, elementArrayBufferBinding)

            if (scissorEnabled) GL11C.glEnable(GL11C.GL_SCISSOR_TEST) else GL11C.glDisable(GL11C.GL_SCISSOR_TEST)
            GL11C.glScissor(scissorBox[0], scissorBox[1], scissorBox[2], scissorBox[3])
            GL11C.glViewport(viewport[0], viewport[1], viewport[2], viewport[3])
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, drawFramebufferBinding)
            GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, readFramebufferBinding)
        }

        companion object {
            fun capture(): State {
                MemoryStack.stackPush().use { stack ->
                    val maxUnits = GL11C.glGetInteger(GL20C.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS)
                    val textureBindings = IntArray(maxUnits)
                    val samplerBindings = IntArray(maxUnits)
                    val activeTexture = GL11C.glGetInteger(GL13C.GL_ACTIVE_TEXTURE)
                    for (i in 0 until maxUnits) {
                        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + i)
                        textureBindings[i] = GL11C.glGetInteger(GL11C.GL_TEXTURE_BINDING_2D)
                        samplerBindings[i] = GL33C.glGetInteger(GL33C.GL_SAMPLER_BINDING)
                    }
                    GL13C.glActiveTexture(activeTexture)

                    val colorMask = stack.malloc(4)
                    GL11C.glGetBooleanv(GL11C.GL_COLOR_WRITEMASK, colorMask)
                    val scissor = stack.mallocInt(4)
                    GL11C.glGetIntegerv(GL11C.GL_SCISSOR_BOX, scissor)
                    val viewport = stack.mallocInt(4)
                    GL11C.glGetIntegerv(GL11C.GL_VIEWPORT, viewport)

                    return State(
                        blendEnabled = GL11C.glIsEnabled(GL11C.GL_BLEND),
                        blendSrcRgb = GL11C.glGetInteger(GL14C.GL_BLEND_SRC_RGB),
                        blendDstRgb = GL11C.glGetInteger(GL14C.GL_BLEND_DST_RGB),
                        blendSrcAlpha = GL11C.glGetInteger(GL14C.GL_BLEND_SRC_ALPHA),
                        blendDstAlpha = GL11C.glGetInteger(GL14C.GL_BLEND_DST_ALPHA),
                        depthTestEnabled = GL11C.glIsEnabled(GL11C.GL_DEPTH_TEST),
                        depthMask = GL11C.glGetBoolean(GL11C.GL_DEPTH_WRITEMASK),
                        depthFunc = GL11C.glGetInteger(GL11C.GL_DEPTH_FUNC),
                        cullEnabled = GL11C.glIsEnabled(GL11C.GL_CULL_FACE),
                        cullFace = GL11C.glGetInteger(GL11C.GL_CULL_FACE_MODE),
                        activeTexture = activeTexture,
                        textureBindings = textureBindings,
                        samplerBindings = samplerBindings,
                        program = GL11C.glGetInteger(GL20C.GL_CURRENT_PROGRAM),
                        vaoBinding = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING),
                        colorMaskR = colorMask.get(0) != 0.toByte(),
                        colorMaskG = colorMask.get(1) != 0.toByte(),
                        colorMaskB = colorMask.get(2) != 0.toByte(),
                        colorMaskA = colorMask.get(3) != 0.toByte(),
                        unpackAlignment = GL11C.glGetInteger(GL11C.GL_UNPACK_ALIGNMENT),
                        pixelUnpackBufferBinding = GL11C.glGetInteger(GL21C.GL_PIXEL_UNPACK_BUFFER_BINDING),
                        unpackSwapBytes = GL11C.glGetBoolean(GL11C.GL_UNPACK_SWAP_BYTES),
                        unpackLsbFirst = GL11C.glGetBoolean(GL11C.GL_UNPACK_LSB_FIRST),
                        unpackRowLength = GL11C.glGetInteger(GL11C.GL_UNPACK_ROW_LENGTH),
                        unpackSkipRows = GL11C.glGetInteger(GL11C.GL_UNPACK_SKIP_ROWS),
                        unpackSkipPixels = GL11C.glGetInteger(GL11C.GL_UNPACK_SKIP_PIXELS),
                        pixelPackBufferBinding = GL11C.glGetInteger(GL21C.GL_PIXEL_PACK_BUFFER_BINDING),
                        scissorEnabled = GL11C.glIsEnabled(GL11C.GL_SCISSOR_TEST),
                        scissorBox = intArrayOf(scissor.get(0), scissor.get(1), scissor.get(2), scissor.get(3)),
                        viewport = intArrayOf(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3)),
                        drawFramebufferBinding = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING),
                        readFramebufferBinding = GL11C.glGetInteger(GL30C.GL_READ_FRAMEBUFFER_BINDING),
                        blendEquationRgb = GL11C.glGetInteger(GL20C.GL_BLEND_EQUATION_RGB),
                        blendEquationAlpha = GL11C.glGetInteger(GL20C.GL_BLEND_EQUATION_ALPHA),
                        arrayBufferBinding = GL11C.glGetInteger(GL21C.GL_ARRAY_BUFFER_BINDING),
                        elementArrayBufferBinding = GL11C.glGetInteger(GL21C.GL_ELEMENT_ARRAY_BUFFER_BINDING)
                    )
                }
            }
        }
    }
}
