package com.yuan.space_slash;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class YuanSpaceSlashRenderType extends RenderType {
    private static final ResourceLocation WHITE_TEXTURE =
            new ResourceLocation("minecraft", "textures/misc/white.png");

    public static final RenderType SLASH_BODY = create(
            "yuan_space_slash_body",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            4096, false, true,
            CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            YuanSpaceSlashShaders::getBodyShader))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true));

    public static final RenderType SLASH_GLOW = create(
            "yuan_space_slash_glow",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            4096, false, true,
            CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            YuanSpaceSlashShaders::getGlowShader))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true));

    public static final RenderType SLASH_BODY_DEPTH = create(
            "yuan_space_slash_body_depth",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            4096, false, true,
            CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            YuanSpaceSlashShaders::getBodyShader))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true));

    public static final RenderType SLASH_GLOW_DEPTH = create(
            "yuan_space_slash_glow_depth",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            4096, false, true,
            CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            YuanSpaceSlashShaders::getGlowShader))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true));

    public static final RenderType SLASH_FALLBACK = create(
            "yuan_space_slash_fallback",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            4096, false, true,
            CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            GameRenderer::getRendertypeEntityTranslucentShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            WHITE_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true));

    public static final RenderType SLASH_GLOW_FALLBACK = create(
            "yuan_space_slash_glow_fallback",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            4096, false, true,
            CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            GameRenderer::getRendertypeEntityTranslucentShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            WHITE_TEXTURE, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true));

    public static final RenderType SLASH_FALLBACK_DEPTH = create(
            "yuan_space_slash_fallback_depth",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            4096, false, true,
            CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            GameRenderer::getRendertypeEntityTranslucentShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            WHITE_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true));

    public static final RenderType SLASH_GLOW_FALLBACK_DEPTH = create(
            "yuan_space_slash_glow_fallback_depth",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            4096, false, true,
            CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            GameRenderer::getRendertypeEntityTranslucentShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            WHITE_TEXTURE, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true));

    public YuanSpaceSlashRenderType(String name, VertexFormat format, VertexFormat.Mode mode,
                                    int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                                    Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload,
                setupState, clearState);
    }
}
