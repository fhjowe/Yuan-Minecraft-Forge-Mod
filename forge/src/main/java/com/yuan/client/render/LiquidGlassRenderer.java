package com.yuan.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.yuan.client.shader.ModShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class LiquidGlassRenderer {
    private TextureTarget sceneTarget;
    private TextureTarget blurScratchTarget;
    private TextureTarget blurTarget;
    private int targetWidth = -1;
    private int targetHeight = -1;

    public boolean prepare(GuiGraphics graphics, int glassW, int glassH, float blurRadius) {
        ShaderInstance blurShader = ModShaders.getBlurShader();
        if (blurShader == null || ModShaders.getLiquidGlassShader() == null || glassW <= 0 || glassH <= 0) return false;

        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        int readFbo = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int drawFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        int blendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int blendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int blendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int blendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);

        try {
            ensureTargets(main.width, main.height);
            graphics.flush();
            copyScene(main);
            blur(sceneTarget, blurScratchTarget, 1.0f, 0.0f, blurRadius);
            blur(blurScratchTarget, blurTarget, 0.0f, 1.0f, blurRadius);
            return true;
        } catch (RuntimeException error) {
            close();
            return false;
        } finally {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
            RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            RenderSystem.depthMask(depthMask);
            RenderSystem.depthFunc(depthFunc);
            if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            RenderSystem.blendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha);
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
        }
    }

    public int getSceneTextureId() { return sceneTarget == null ? 0 : sceneTarget.getColorTextureId(); }
    public int getBlurTextureId() { return blurTarget == null ? 0 : blurTarget.getColorTextureId(); }

    private void ensureTargets(int width, int height) {
        if (sceneTarget != null && targetWidth == width && targetHeight == height) return;
        close();
        try {
            sceneTarget = target(width, height);
            blurScratchTarget = target(width, height);
            blurTarget = target(width, height);
            targetWidth = width;
            targetHeight = height;
        } catch (RuntimeException error) {
            close();
            throw error;
        }
    }

    private static TextureTarget target(int width, int height) {
        TextureTarget target = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        target.setFilterMode(GL11.GL_LINEAR);
        target.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        return target;
    }

    private void copyScene(RenderTarget main) {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, sceneTarget.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, main.width, main.height,
                0, 0, sceneTarget.width, sceneTarget.height,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        main.bindWrite(true);
    }

    private static void blur(RenderTarget input, RenderTarget output,
                             float directionX, float directionY, float radius) {
        ShaderInstance shader = ModShaders.getBlurShader();
        output.bindWrite(true);
        RenderSystem.viewport(0, 0, output.width, output.height);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShader(() -> shader);
        shader.setSampler("DiffuseSampler", input.getColorTextureId());
        shader.safeGetUniform("Direction").set(directionX, directionY);
        shader.safeGetUniform("Radius").set(radius);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(-1.0, -1.0, 0.0).uv(0.0f, 0.0f).endVertex();
        buffer.vertex(1.0, -1.0, 0.0).uv(1.0f, 0.0f).endVertex();
        buffer.vertex(1.0, 1.0, 0.0).uv(1.0f, 1.0f).endVertex();
        buffer.vertex(-1.0, 1.0, 0.0).uv(0.0f, 1.0f).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    public void close() {
        destroy(sceneTarget);
        destroy(blurScratchTarget);
        destroy(blurTarget);
        sceneTarget = null;
        blurScratchTarget = null;
        blurTarget = null;
        targetWidth = -1;
        targetHeight = -1;
    }

    private static void destroy(TextureTarget target) {
        if (target != null) target.destroyBuffers();
    }
}
