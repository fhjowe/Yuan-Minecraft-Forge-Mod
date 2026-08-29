package com.yuan.timestop.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;

public class CullWrappedRenderLayer extends RenderType {
    private final RenderType delegate;

    public CullWrappedRenderLayer(RenderType delegate) {
        super("magic" + delegate + "_with_cull", delegate.format(), delegate.mode(),
                delegate.bufferSize(), true, delegate.isOutline(),
                () -> {
                    delegate.setupRenderState();
                    RenderSystem.disableBlend();
                },
                () -> {
                    RenderSystem.enableCull();
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
        return other instanceof CullWrappedRenderLayer
                && this.delegate.equals(((CullWrappedRenderLayer) other).delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.delegate);
    }
}
