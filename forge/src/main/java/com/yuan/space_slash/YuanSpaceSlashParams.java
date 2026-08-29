package com.yuan.space_slash;

import com.yuan.item.YuanGodSwordConfig;

public final class YuanSpaceSlashParams {
    public boolean enabled = true;
    public boolean depthTest = true;
    public boolean randomAngle = true;
    public boolean glow = true;
    public float durationSeconds = 0.26f;
    public float lengthMult = 2.8f;
    public float widthRatio = 0.09f;
    public float thicknessRatio = 0.02f;
    public float tipFade = 0.08f;
    public float startScale = 1.0f;
    public float endScale = 1.0f;
    public float coreWidth = 0.62f;
    public float coreShade = 1.0f;
    public float edgeWidth = 0.80f;
    public float edgeBrightness = 1.0f;
    public float glowWidth = 0.90f;
    public float glowStrength = 1.0f;
    public float noiseStrength = 1.0f;
    public float sweepSpeed = 0.35f;
    public float sweepSoftness = 0.15f;
    public float holdFraction = 0.0f;
    public float fadeStart = 0.72f;
    public float fadeDuration = 0.28f;
    public float surfaceOffset = 0.06f;
    public float rollRange = 180f;
    public int coreColor = 0xFF161616;
    public int edgeColor = 0xFFFFFFFF;
    public int glowColor = 0xFFFFFFFF;
    public boolean starfield = false;
    public float starDensity = 0.6f;
    public float starBrightness = 0.8f;
    public float starSize = 1.0f;
    public int starColorMode = 0;

    public static YuanSpaceSlashParams from(YuanGodSwordConfig config) {
        YuanSpaceSlashParams p = new YuanSpaceSlashParams();
        if (config == null) {
            return p;
        }
        p.enabled = config.slashEnabled;
        p.depthTest = config.slashDepthTest;
        p.randomAngle = config.slashRandomAngle;
        p.glow = config.slashGlow;
        p.durationSeconds = config.slashDuration;
        p.lengthMult = config.slashLengthMult;
        p.widthRatio = config.slashWidthRatio;
        p.thicknessRatio = config.slashThicknessRatio;
        p.tipFade = config.slashTipFade;
        p.startScale = config.slashStartScale;
        p.endScale = config.slashEndScale;
        p.coreWidth = config.slashCoreWidth;
        p.coreShade = config.slashCoreShade;
        p.edgeWidth = config.slashEdgeWidth;
        p.edgeBrightness = config.slashEdgeBrightness;
        p.glowWidth = config.slashGlowWidth;
        p.glowStrength = config.slashGlowStrength;
        p.noiseStrength = config.slashNoiseStrength;
        p.sweepSpeed = config.slashSweepSpeed;
        p.sweepSoftness = config.slashSweepSoftness;
        p.holdFraction = config.slashHoldFraction;
        p.fadeStart = config.slashFadeStart;
        p.fadeDuration = config.slashFadeDuration;
        p.surfaceOffset = config.slashSurfaceOffset;
        p.rollRange = config.slashRollRange;
        p.coreColor = config.slashCoreColor;
        p.edgeColor = config.slashEdgeColor;
        p.glowColor = config.slashGlowColor;
        p.starfield = config.slashStarfield;
        p.starDensity = config.slashStarDensity;
        p.starBrightness = config.slashStarBrightness;
        p.starSize = config.slashStarSize;
        p.starColorMode = config.slashStarColorMode;
        return p;
    }
}
