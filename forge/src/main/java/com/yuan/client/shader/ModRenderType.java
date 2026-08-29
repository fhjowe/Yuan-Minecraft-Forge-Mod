package com.yuan.client.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import com.yuan.client.cosmic.YuanCosmicShaders;

public class ModRenderType extends RenderType {
    public ModRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static final RenderStateShard.TextureStateShard COSMIC_TEXTURE_ISOLATED = new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false) {
        @Override
        public void setupRenderState() {
            super.setupRenderState();
            RenderSystem.activeTexture(33984);
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
            RenderSystem.bindTexture(atlas.getId());
        }
    };

    private static final RenderStateShard.TextureStateShard VORONOI_NOISE_TEXTURE = new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false) {
        @Override
        public void setupRenderState() {
            YuanCosmicShaders.bindVoronoiNoise();
        }
    };

    public static final RenderType rainbow_slime_block = RenderType.create(
        "yuan:rainbow_slime_block",
        DefaultVertexFormat.BLOCK,
        VertexFormat.Mode.TRIANGLES,
        256,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(ModShaders::getRainbowSlimeShader))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setDepthTestState(NO_DEPTH_TEST)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .createCompositeState(false)
    );

    public static final RenderType liquidGlass = RenderType.create(
        "yuan:liquid_glass",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        256, false, true,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(ModShaders::getLiquidGlassShader))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setDepthTestState(NO_DEPTH_TEST)
            .createCompositeState(false)
    );

    public static final RenderType cosmicRenderType = RenderType.create(
        "yuan:cosmic_neo",
        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
        VertexFormat.Mode.QUADS,
        0x200000, true, false,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(() -> YuanCosmicShaders.cosmicShader))
            .setTextureState(COSMIC_TEXTURE_ISOLATED)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setLightmapState(LIGHTMAP)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    public static final RenderType silkRenderType = RenderType.create(
        "yuan:silk_item",
        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
        VertexFormat.Mode.QUADS,
        0x200000, true, false,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(() -> YuanCosmicShaders.silkShader))
            .setTextureState(COSMIC_TEXTURE_ISOLATED)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setLightmapState(LIGHTMAP)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    public static final RenderType tunnelRenderType = RenderType.create(
        "yuan:tunnel_item",
        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
        VertexFormat.Mode.QUADS,
        0x200000, true, false,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(() -> YuanCosmicShaders.tunnelShader))
            .setCullState(NO_CULL)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setLightmapState(LIGHTMAP)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    public static final RenderType voronoiRenderType = RenderType.create(
        "yuan:voronoi_item",
        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
        VertexFormat.Mode.QUADS,
        0x200000, true, false,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(() -> YuanCosmicShaders.voronoiShader))
            .setTextureState(VORONOI_NOISE_TEXTURE)
            .setCullState(NO_CULL)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setLightmapState(LIGHTMAP)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    public static RenderType foxBladeBackground(ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
