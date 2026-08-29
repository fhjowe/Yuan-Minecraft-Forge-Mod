package com.yuan.timestop.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MegaRenderType extends RenderType {
    public MegaRenderType(String name, VertexFormat format, VertexFormat.Mode mode,
                          int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                          Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload,
                setupState, clearState);
    }

    public static RenderType createSphereRenderType(ResourceLocation texture, int index) {
        return create("magic_sphere3" + index,
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.TRIANGLE_STRIP,
                256, false, false,
                CompositeState.builder()
                        .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)
                        .setShaderState(new RenderStateShard.ShaderStateShard(
                                () -> GameRenderer.getPositionColorTexShader()))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setLightmapState(NO_LIGHTMAP)
                        .setTextureState(new RenderStateShard.TextureStateShard(
                                texture, false, false))
                        .createCompositeState(true));
    }
}
