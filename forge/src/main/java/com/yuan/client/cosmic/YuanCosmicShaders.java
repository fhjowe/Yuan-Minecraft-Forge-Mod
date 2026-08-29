package com.yuan.client.cosmic;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.yuan.item.YuanGodSwordConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.joml.Vector4f;

import java.io.IOException;
import java.io.InputStream;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class YuanCosmicShaders {
    public static CCShaderInstance cosmicShader;
    public static CCUniform cosmicTime;
    public static CCUniform cosmicYaw;
    public static CCUniform cosmicPitch;
    public static CCUniform cosmicExternalScale;
    public static CCUniform cosmicOpacity;
    public static CCUniform cosmicUseType;
    public static CCUniform cosmicColor;
    public static CCUniform cosmicUVs;

    public static final float[] COSMIC_UVS = new float[40];
    public static ShaderInstance silkShader;
    private static Uniform silkTime;
    private static Uniform silkColor0;
    private static Uniform silkColor1;
    private static Uniform silkColor2;
    private static Uniform silkColor3;
    private static Uniform silkColor4;
    private static Uniform silkColor5;
    private static Uniform silkColor6;
    private static Uniform silkColor7;
    private static Uniform silkBrightness;
    private static Uniform silkContrast;
    private static Uniform silkSaturation;
    private static Uniform silkScale;
    private static Uniform silkIntensity;
    private static Uniform silkWarp;
    private static Uniform silkDetail;
    private static Uniform silkHue;
    private static Uniform silkSeed;
    private static Uniform silkRotation;
    private static Uniform silkDrift;
    private static Uniform silkVignette;
    private static Uniform silkBlur;
    private static Uniform silkGrain;
    private static Uniform silkSpriteBounds;
    private static Uniform silkViewYaw;
    private static Uniform silkViewPitch;
    public static ShaderInstance tunnelShader;
    private static Uniform tunnelTime;
    private static Uniform tunnelSpeed;
    private static Uniform tunnelBrightness;
    private static Uniform tunnelDensity;
    private static Uniform tunnelFov;
    private static Uniform tunnelSpriteBounds;
    private static Uniform tunnelViewYaw;
    private static Uniform tunnelViewPitch;
    public static ShaderInstance voronoiShader;
    private static Uniform voronoiTime;
    private static Uniform voronoiColorCount;
    private static Uniform voronoiStepsPerColor;
    private static Uniform voronoiColor0;
    private static Uniform voronoiColor1;
    private static Uniform voronoiColor2;
    private static Uniform voronoiColor3;
    private static Uniform voronoiColor4;
    private static Uniform voronoiColorGlow;
    private static Uniform voronoiColorGap;
    private static Uniform voronoiDistortion;
    private static Uniform voronoiGap;
    private static Uniform voronoiGlow;
    private static Uniform voronoiScale;
    private static Uniform voronoiFov;
    private static Uniform voronoiSpeed;
    private static Uniform voronoiRotation;
    private static Uniform voronoiOffsetX;
    private static Uniform voronoiOffsetY;
    private static Uniform voronoiSpriteBounds;
    private static Uniform voronoiViewYaw;
    private static Uniform voronoiViewPitch;
    private static DynamicTexture voronoiNoise;
    private static boolean voronoiNoiseFailed = false;
    public static int renderTime = 0;
    public static float renderFrame = 0f;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("YuanCosmic");


    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("yuan", "cosmic_neo"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader -> {
            cosmicShader = (CCShaderInstance) shader;
            cosmicTime = cosmicShader.getUniform("time");
            cosmicYaw = cosmicShader.getUniform("yaw");
            cosmicPitch = cosmicShader.getUniform("pitch");
            cosmicExternalScale = cosmicShader.getUniform("externalScale");
            cosmicOpacity = cosmicShader.getUniform("opacity");
            cosmicUseType = cosmicShader.getUniform("useCosmicType");
            cosmicColor = cosmicShader.getUniform("cosmicColor0");
            cosmicUVs = cosmicShader.getUniform("cosmicuvs");
            cosmicShader.onApply(() -> {
                if (cosmicTime != null) {
                    cosmicTime.set(renderTime + renderFrame);
                }
            });
        });
        event.registerShader(new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath("yuan", "silk_item"),
                        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                shader -> {
                    silkShader = shader;
                    silkTime = shader.getUniform("Time");
                    silkColor0 = shader.getUniform("SilkColor0");
                    silkColor1 = shader.getUniform("SilkColor1");
                    silkColor2 = shader.getUniform("SilkColor2");
                    silkColor3 = shader.getUniform("SilkColor3");
                    silkColor4 = shader.getUniform("SilkColor4");
                    silkColor5 = shader.getUniform("SilkColor5");
                    silkColor6 = shader.getUniform("SilkColor6");
                    silkColor7 = shader.getUniform("SilkColor7");
                    silkBrightness = shader.getUniform("SilkBrightness");
                    silkContrast = shader.getUniform("SilkContrast");
                    silkSaturation = shader.getUniform("SilkSaturation");
                    silkScale = shader.getUniform("SilkScale");
                    silkIntensity = shader.getUniform("SilkIntensity");
                    silkWarp = shader.getUniform("SilkWarp");
                    silkDetail = shader.getUniform("SilkDetail");
                    silkHue = shader.getUniform("SilkHue");
                    silkSeed = shader.getUniform("SilkSeed");
                    silkRotation = shader.getUniform("SilkRotation");
                    silkDrift = shader.getUniform("SilkDrift");
                    silkVignette = shader.getUniform("SilkVignette");
                    silkBlur = shader.getUniform("SilkBlur");
                    silkGrain = shader.getUniform("SilkGrain");
                    silkSpriteBounds = shader.getUniform("SpriteBounds");
                    silkViewYaw = shader.getUniform("ViewYaw");
                    silkViewPitch = shader.getUniform("ViewPitch");
                });
        event.registerShader(new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath("yuan", "tunnel_item"),
                        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                shader -> {
                    tunnelShader = shader;
                    tunnelTime = shader.getUniform("Time");
                    tunnelSpeed = shader.getUniform("TunnelSpeed");
                    tunnelBrightness = shader.getUniform("TunnelBrightness");
                    tunnelDensity = shader.getUniform("TunnelDensity");
                    tunnelFov = shader.getUniform("TunnelFov");
                    tunnelSpriteBounds = shader.getUniform("SpriteBounds");
                    tunnelViewYaw = shader.getUniform("ViewYaw");
                    tunnelViewPitch = shader.getUniform("ViewPitch");
                });
        event.registerShader(new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath("yuan", "voronoi_item"),
                        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                shader -> {
                    voronoiShader = shader;
                    voronoiTime = shader.getUniform("Time");
                    voronoiColorCount = shader.getUniform("VoronoiColorCount");
                    voronoiStepsPerColor = shader.getUniform("VoronoiStepsPerColor");
                    voronoiColor0 = shader.getUniform("VoronoiColor0");
                    voronoiColor1 = shader.getUniform("VoronoiColor1");
                    voronoiColor2 = shader.getUniform("VoronoiColor2");
                    voronoiColor3 = shader.getUniform("VoronoiColor3");
                    voronoiColor4 = shader.getUniform("VoronoiColor4");
                    voronoiColorGlow = shader.getUniform("VoronoiColorGlow");
                    voronoiColorGap = shader.getUniform("VoronoiColorGap");
                    voronoiDistortion = shader.getUniform("VoronoiDistortion");
                    voronoiGap = shader.getUniform("VoronoiGap");
                    voronoiGlow = shader.getUniform("VoronoiGlow");
                    voronoiScale = shader.getUniform("VoronoiScale");
                    voronoiFov = shader.getUniform("VoronoiFov");
                    voronoiSpeed = shader.getUniform("VoronoiSpeed");
                    voronoiRotation = shader.getUniform("VoronoiRotation");
                    voronoiOffsetX = shader.getUniform("VoronoiOffsetX");
                    voronoiOffsetY = shader.getUniform("VoronoiOffsetY");
                    voronoiSpriteBounds = shader.getUniform("SpriteBounds");
                    voronoiViewYaw = shader.getUniform("ViewYaw");
                    voronoiViewPitch = shader.getUniform("ViewPitch");
                });
    }

    public static void setupCosmicUniforms(float scale, float alpha, int type, Vector4f color, boolean gui) {
        CCShaderInstance shader = cosmicShader;
        if (shader == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f;
        float pitch = 0.0f;
        if (!gui && mc.player != null) {
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -((float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0));
        }
        // Avaritia-style tick-driven time; onApply also refreshes it every shader apply.
        float tick = renderTime + renderFrame;
        if (cosmicTime != null) cosmicTime.set(tick);
        if (cosmicYaw != null) cosmicYaw.set(yaw);
        if (cosmicPitch != null) cosmicPitch.set(pitch);
        if (cosmicExternalScale != null) cosmicExternalScale.set(scale);
        if (cosmicOpacity != null) cosmicOpacity.set(alpha);
        if (cosmicUseType != null) cosmicUseType.set(type);
        if (cosmicColor != null) cosmicColor.set(color);
        if (cosmicUVs != null) {
            refreshCosmicUvs();
            cosmicUVs.set(COSMIC_UVS);
        }
    }

    public static void refreshCosmicUvs() {
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < 10; i++) {
            TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.fromNamespaceAndPath("yuan", "item/cosmic_" + i));
            COSMIC_UVS[i * 4] = sprite.getU0();
            COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }
    }

    public static void setupSilkUniforms(YuanGodSwordConfig cfg) {
        if (silkShader == null) {
            return;
        }
        setFloat(silkTime, (float) ((System.nanoTime() / 1_000_000_000.0) % 100_000.0));
        setColor(silkColor0, cfg.silkColor0);
        setColor(silkColor1, cfg.silkColor1);
        setColor(silkColor2, cfg.silkColor2);
        setColor(silkColor3, cfg.silkColor3);
        setColor(silkColor4, cfg.silkColor4);
        setColor(silkColor5, cfg.silkColor5);
        setColor(silkColor6, cfg.silkColor6);
        setColor(silkColor7, cfg.silkColor7);
        setFloat(silkBrightness, cfg.silkBrightness);
        setFloat(silkContrast, cfg.silkContrast);
        setFloat(silkSaturation, cfg.silkSaturation);
        setFloat(silkScale, cfg.silkScale);
        setFloat(silkIntensity, cfg.silkIntensity);
        setFloat(silkWarp, cfg.silkWarp);
        setFloat(silkDetail, cfg.silkDetail);
        setFloat(silkHue, cfg.silkHue);
        setFloat(silkSeed, cfg.silkSeed);
        setFloat(silkRotation, cfg.silkRotation);
        setFloat(silkDrift, cfg.silkDrift);
        setFloat(silkVignette, cfg.silkVignette);
        setFloat(silkBlur, cfg.silkBlur);
        setFloat(silkGrain, cfg.silkGrain);
        if (silkSpriteBounds != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(ResourceLocation.fromNamespaceAndPath("yuan", "item/yuan_god_sword"));
            silkSpriteBounds.set(
                    sprite.getU0(), sprite.getV0(),
                    sprite.getU1() - sprite.getU0(), sprite.getV1() - sprite.getV0());
        }
        Minecraft mc = Minecraft.getInstance();
        if (silkViewYaw != null) {
            silkViewYaw.set(mc.player != null
                    ? (float) (mc.player.getYRot() * 2.0 * Math.PI / 360.0)
                    : 0.0f);
        }
        if (silkViewPitch != null) {
            silkViewPitch.set(mc.player != null
                    ? -((float) (mc.player.getXRot() * 2.0 * Math.PI / 360.0))
                    : 0.0f);
        }
    }

    public static void setupTunnelUniforms(YuanGodSwordConfig cfg) {
        if (tunnelShader == null) {
            return;
        }
        setFloat(tunnelTime, (float) ((System.nanoTime() / 1_000_000_000.0) % 100_000.0));
        setFloat(tunnelSpeed, cfg.tunnelSpeed);
        setFloat(tunnelBrightness, cfg.tunnelBrightness);
        setFloat(tunnelDensity, cfg.tunnelDensity);
        setFloat(tunnelFov, cfg.tunnelFov);
        Minecraft mc = Minecraft.getInstance();
        if (tunnelViewYaw != null) {
            tunnelViewYaw.set(mc.player != null
                    ? (float) (mc.player.getYRot() * 2.0 * Math.PI / 360.0)
                    : 0.0f);
        }
        if (tunnelViewPitch != null) {
            tunnelViewPitch.set(mc.player != null
                    ? -((float) (mc.player.getXRot() * 2.0 * Math.PI / 360.0))
                    : 0.0f);
        }
        if (tunnelSpriteBounds != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(ResourceLocation.fromNamespaceAndPath("yuan", "item/yuan_god_sword"));
            tunnelSpriteBounds.set(
                    sprite.getU0(), sprite.getV0(),
                    sprite.getU1() - sprite.getU0(), sprite.getV1() - sprite.getV0());
        }
    }

    public static void setupVoronoiUniforms(YuanGodSwordConfig cfg) {
        if (voronoiShader == null) {
            return;
        }
        ensureVoronoiNoise();
        setFloat(voronoiTime, (float) ((System.nanoTime() / 1_000_000_000.0) % 100_000.0));
        setFloat(voronoiColorCount, cfg.voronoiColorCount);
        setFloat(voronoiStepsPerColor, cfg.voronoiStepsPerColor);
        setColorWithAlpha(voronoiColor0, cfg.voronoiColor0);
        setColorWithAlpha(voronoiColor1, cfg.voronoiColor1);
        setColorWithAlpha(voronoiColor2, cfg.voronoiColor2);
        setColorWithAlpha(voronoiColor3, cfg.voronoiColor3);
        setColorWithAlpha(voronoiColor4, cfg.voronoiColor4);
        setColorWithAlpha(voronoiColorGlow, cfg.voronoiColorGlow);
        setColorWithAlpha(voronoiColorGap, cfg.voronoiColorGap);
        setFloat(voronoiDistortion, cfg.voronoiDistortion);
        setFloat(voronoiGap, cfg.voronoiGap);
        setFloat(voronoiGlow, cfg.voronoiGlow);
        setFloat(voronoiScale, cfg.voronoiScale);
        setFloat(voronoiFov, cfg.voronoiFov);
        setFloat(voronoiSpeed, cfg.voronoiSpeed);
        setFloat(voronoiRotation, cfg.voronoiRotation);
        setFloat(voronoiOffsetX, cfg.voronoiOffsetX);
        setFloat(voronoiOffsetY, cfg.voronoiOffsetY);
        if (voronoiSpriteBounds != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(ResourceLocation.fromNamespaceAndPath("yuan", "item/yuan_god_sword"));
            voronoiSpriteBounds.set(
                    sprite.getU0(), sprite.getV0(),
                    sprite.getU1() - sprite.getU0(), sprite.getV1() - sprite.getV0());
        }
        Minecraft mc = Minecraft.getInstance();
        if (voronoiViewYaw != null) {
            voronoiViewYaw.set(mc.player != null
                    ? (float) (mc.player.getYRot() * 2.0 * Math.PI / 360.0)
                    : 0.0f);
        }
        if (voronoiViewPitch != null) {
            voronoiViewPitch.set(mc.player != null
                    ? -((float) (mc.player.getXRot() * 2.0 * Math.PI / 360.0))
                    : 0.0f);
        }
        if (voronoiNoise != null && voronoiShader != null) {
            voronoiShader.setSampler("u_noiseTexture", voronoiNoise.getId());
        }
    }

    private static void ensureVoronoiNoise() {
        if (voronoiNoise != null || voronoiNoiseFailed) {
            return;
        }
        try (InputStream stream = Minecraft.getInstance().getResourceManager()
                .open(ResourceLocation.fromNamespaceAndPath("yuan", "textures/effect/voronoi_noise.png"))) {
            NativeImage image = NativeImage.read(stream);
            DynamicTexture texture = new DynamicTexture(image);
            texture.upload();
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            voronoiNoise = texture;
        } catch (Exception e) {
            voronoiNoiseFailed = true;
            LOGGER.error("[YuanCosmic] failed to load voronoi noise texture", e);
        }
    }

    public static void bindVoronoiNoise() {
        ensureVoronoiNoise();
        if (voronoiNoise != null) {
            RenderSystem.activeTexture(33984);
            RenderSystem.bindTexture(voronoiNoise.getId());
        }
    }

    private static void setFloat(Uniform uniform, float value) {
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setColor(Uniform uniform, int argb) {
        if (uniform == null) {
            return;
        }
        uniform.set(
                ((argb >> 16) & 0xFF) / 255.0f,
                ((argb >> 8) & 0xFF) / 255.0f,
                (argb & 0xFF) / 255.0f,
                1.0f);
    }

    private static void setColorWithAlpha(Uniform uniform, int argb) {
        if (uniform == null) {
            return;
        }
        uniform.set(
                ((argb >> 16) & 0xFF) / 255.0f,
                ((argb >> 8) & 0xFF) / 255.0f,
                (argb & 0xFF) / 255.0f,
                ((argb >> 24) & 0xFF) / 255.0f);
    }
}
