package com.yuan.space_slash;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class YuanSpaceSlashShaders {
    public static ShaderInstance bodyShader;
    public static ShaderInstance glowShader;
    private static Uniform bodyTime;
    private static Uniform bodyProgress;
    private static Uniform bodySeed;
    private static Uniform bodyCoreColor;
    private static Uniform bodyEdgeColor;
    private static Uniform bodyGlowColor;
    private static Uniform bodyCoreShade;
    private static Uniform bodyEdgeBrightness;
    private static Uniform bodyGlowStrength;
    private static Uniform bodyNoiseStrength;
    private static Uniform bodyTipFade;
    private static Uniform bodySweepSpeed;
    private static Uniform bodySweepSoftness;
    private static Uniform bodyFadeStart;
    private static Uniform bodyFadeDuration;
    private static Uniform bodyHoldFraction;
    private static Uniform bodyCoreWidth;
    private static Uniform bodyEdgeWidth;
    private static Uniform bodyGlowWidth;
    private static Uniform bodyStarfield;
    private static Uniform bodyStarDensity;
    private static Uniform bodyStarBrightness;
    private static Uniform bodyStarSize;
    private static Uniform bodyStarColorMode;
    private static Uniform glowTime;
    private static Uniform glowProgress;
    private static Uniform glowSeed;
    private static Uniform glowColor;
    private static Uniform glowStrength;
    private static Uniform glowEdgeBrightness;
    private static Uniform glowTipFade;
    private static Uniform glowSweepSpeed;
    private static Uniform glowSweepSoftness;
    private static Uniform glowFadeStart;
    private static Uniform glowFadeDuration;
    private static Uniform glowHoldFraction;
    private static Uniform glowEdgeWidth;
    private static Uniform glowGlowWidth;

    private YuanSpaceSlashShaders() {
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation("yuan", "space_slash_body"),
                        DefaultVertexFormat.NEW_ENTITY),
                shader -> {
                    YuanSpaceSlashShaders.bodyShader = shader;
                    bodyTime = shader.getUniform("Time");
                    bodyProgress = shader.getUniform("Progress");
                    bodySeed = shader.getUniform("Seed");
                    bodyCoreColor = shader.getUniform("CoreColor");
                    bodyEdgeColor = shader.getUniform("EdgeColor");
                    bodyGlowColor = shader.getUniform("GlowColor");
                    bodyCoreShade = shader.getUniform("CoreShade");
                    bodyEdgeBrightness = shader.getUniform("EdgeBrightness");
                    bodyGlowStrength = shader.getUniform("GlowStrength");
                    bodyNoiseStrength = shader.getUniform("NoiseStrength");
                    bodyTipFade = shader.getUniform("TipFade");
                    bodySweepSpeed = shader.getUniform("SweepSpeed");
                    bodySweepSoftness = shader.getUniform("SweepSoftness");
                    bodyFadeStart = shader.getUniform("FadeStart");
                    bodyFadeDuration = shader.getUniform("FadeDuration");
                    bodyHoldFraction = shader.getUniform("HoldFraction");
                    bodyCoreWidth = shader.getUniform("CoreWidth");
                    bodyEdgeWidth = shader.getUniform("EdgeWidth");
                    bodyGlowWidth = shader.getUniform("GlowWidth");
                    bodyStarfield = shader.getUniform("Starfield");
                    bodyStarDensity = shader.getUniform("StarDensity");
                    bodyStarBrightness = shader.getUniform("StarBrightness");
                    bodyStarSize = shader.getUniform("StarSize");
                    bodyStarColorMode = shader.getUniform("StarColorMode");
                });
        event.registerShader(new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation("yuan", "space_slash_glow"),
                        DefaultVertexFormat.NEW_ENTITY),
                shader -> {
                    YuanSpaceSlashShaders.glowShader = shader;
                    glowTime = shader.getUniform("Time");
                    glowProgress = shader.getUniform("Progress");
                    glowSeed = shader.getUniform("Seed");
                    glowColor = shader.getUniform("GlowColor");
                    glowStrength = shader.getUniform("GlowStrength");
                    glowEdgeBrightness = shader.getUniform("EdgeBrightness");
                    glowTipFade = shader.getUniform("TipFade");
                    glowSweepSpeed = shader.getUniform("SweepSpeed");
                    glowSweepSoftness = shader.getUniform("SweepSoftness");
                    glowFadeStart = shader.getUniform("FadeStart");
                    glowFadeDuration = shader.getUniform("FadeDuration");
                    glowHoldFraction = shader.getUniform("HoldFraction");
                    glowEdgeWidth = shader.getUniform("EdgeWidth");
                    glowGlowWidth = shader.getUniform("GlowWidth");
                });
    }

    public static ShaderInstance getBodyShader() {
        return bodyShader;
    }

    public static ShaderInstance getGlowShader() {
        return glowShader;
    }

    public static void configureBody(YuanSpaceSlashParams params, float progress, int seed) {
        if (bodyShader == null) {
            return;
        }
        float time = (float) ((System.nanoTime() / 1_000_000_000.0) % 100_000.0);
        if (bodyTime != null) {
            bodyTime.set(time);
        }
        if (bodyProgress != null) {
            bodyProgress.set(progress);
        }
        if (bodySeed != null) {
            bodySeed.set((float) seed);
        }
        setColor(bodyCoreColor, params.coreColor);
        setColor(bodyEdgeColor, params.edgeColor);
        setColor(bodyGlowColor, params.glowColor);
        setFloat(bodyCoreShade, params.coreShade);
        setFloat(bodyEdgeBrightness, params.edgeBrightness);
        setFloat(bodyGlowStrength, params.glowStrength);
        setFloat(bodyNoiseStrength, params.noiseStrength);
        setFloat(bodyTipFade, params.tipFade);
        setFloat(bodySweepSpeed, params.sweepSpeed);
        setFloat(bodySweepSoftness, params.sweepSoftness);
        setFloat(bodyFadeStart, params.fadeStart);
        setFloat(bodyFadeDuration, params.fadeDuration);
        setFloat(bodyHoldFraction, params.holdFraction);
        setFloat(bodyCoreWidth, params.coreWidth);
        setFloat(bodyEdgeWidth, params.edgeWidth);
        setFloat(bodyGlowWidth, params.glowWidth);
        setFloat(bodyStarfield, params.starfield ? 1.0f : 0.0f);
        setFloat(bodyStarDensity, params.starDensity);
        setFloat(bodyStarBrightness, params.starBrightness);
        setFloat(bodyStarSize, params.starSize);
        setFloat(bodyStarColorMode, params.starColorMode);
    }

    public static void configureGlow(YuanSpaceSlashParams params, float progress, int seed) {
        if (glowShader == null) {
            return;
        }
        float time = (float) ((System.nanoTime() / 1_000_000_000.0) % 100_000.0);
        if (glowTime != null) {
            glowTime.set(time);
        }
        if (glowProgress != null) {
            glowProgress.set(progress);
        }
        if (glowSeed != null) {
            glowSeed.set((float) seed);
        }
        setColor(glowColor, params.glowColor);
        setFloat(glowStrength, params.glowStrength);
        setFloat(glowEdgeBrightness, params.edgeBrightness);
        setFloat(glowTipFade, params.tipFade);
        setFloat(glowSweepSpeed, params.sweepSpeed);
        setFloat(glowSweepSoftness, params.sweepSoftness);
        setFloat(glowFadeStart, params.fadeStart);
        setFloat(glowFadeDuration, params.fadeDuration);
        setFloat(glowHoldFraction, params.holdFraction);
        setFloat(glowEdgeWidth, params.edgeWidth);
        setFloat(glowGlowWidth, params.glowWidth);
    }

    private static void setColor(Uniform uniform, int argb) {
        if (uniform == null) {
            return;
        }
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        uniform.set(r, g, b, 1.0f);
    }

    private static void setFloat(Uniform uniform, float value) {
        if (uniform != null) {
            uniform.set(value);
        }
    }
}
