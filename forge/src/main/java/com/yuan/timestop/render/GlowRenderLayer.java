package com.yuan.timestop.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

public class GlowRenderLayer extends RenderType {
    private final RenderType delegate;

    public GlowRenderLayer(RenderType delegate, float[] rgba, float softness, boolean shaders) {
        super("magic" + delegate + "_with_framebuffer", delegate.format(), delegate.mode(),
                delegate.bufferSize(), true, delegate.isOutline(),
                () -> {
                    delegate.setupRenderState();
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(770, 771);
                    GL11.glDepthFunc(513);
                    GL11.glDepthMask(false);
                },
                () -> {
                    RenderSystem.disableBlend();
                    GL11.glDepthMask(true);
                    delegate.clearRenderState();
                });
        this.delegate = delegate;
    }

    @Override
    public Optional<RenderType> outline() {
        return this.delegate.outline();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof GlowRenderLayer
                && this.delegate.equals(((GlowRenderLayer) other).delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.delegate);
    }
}
