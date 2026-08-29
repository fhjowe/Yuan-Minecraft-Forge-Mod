package com.yuan.space_slash;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class YuanSpaceSlashMesh {
    private static final int SEGMENTS = 24;

    private YuanSpaceSlashMesh() {
    }

    public static void drawSlashBlade(PoseStack matrix, MultiBufferSource.BufferSource buffer,
                                      RenderType type, float length, float width, float thickness,
                                      float alpha, Vector3f dir, Vector3f side, Vector3f thick) {
        VertexConsumer consumer = buffer.getBuffer(type);
        Matrix4f pose = matrix.last().pose();
        float halfLength = length * 0.5f;
        float halfThickness = thickness * 0.5f;
        Vector3f z = new Vector3f(thick).mul(halfThickness);

        for (int i = 0; i < SEGMENTS; i++) {
            float t0 = i / (float) SEGMENTS;
            float t1 = (i + 1) / (float) SEGMENTS;
            float along0 = -halfLength + length * t0;
            float along1 = -halfLength + length * t1;
            float hw0 = width * 0.5f * (1.0f - Math.abs(2.0f * t0 - 1.0f));
            float hw1 = width * 0.5f * (1.0f - Math.abs(2.0f * t1 - 1.0f));

            Vector3f p0 = new Vector3f(dir).mul(along0);
            Vector3f p1 = new Vector3f(dir).mul(along1);
            Vector3f s0 = new Vector3f(side).mul(hw0);
            Vector3f s1 = new Vector3f(side).mul(hw1);

            Vector3f a = new Vector3f(p0).add(s0).add(z);
            Vector3f b = new Vector3f(p0).sub(s0).add(z);
            Vector3f c = new Vector3f(p1).sub(s1).add(z);
            Vector3f d = new Vector3f(p1).add(s1).add(z);
            quad(pose, consumer, a, b, c, d, t0, t1, thick, alpha);

            a = new Vector3f(p0).add(s0).sub(z);
            b = new Vector3f(p0).sub(s0).sub(z);
            c = new Vector3f(p1).sub(s1).sub(z);
            d = new Vector3f(p1).add(s1).sub(z);
            quad(pose, consumer, a, b, c, d, t0, t1,
                    new Vector3f(thick).negate(), alpha);

            a = new Vector3f(p0).add(s0).add(z);
            b = new Vector3f(p0).add(s0).sub(z);
            c = new Vector3f(p1).add(s1).sub(z);
            d = new Vector3f(p1).add(s1).add(z);
            sideQuad(pose, consumer, a, b, c, d, t0, t1, side, alpha);

            a = new Vector3f(p0).sub(s0).add(z);
            b = new Vector3f(p0).sub(s0).sub(z);
            c = new Vector3f(p1).sub(s1).sub(z);
            d = new Vector3f(p1).sub(s1).add(z);
            sideQuad(pose, consumer, a, b, c, d, t0, t1,
                    new Vector3f(side).negate(), alpha);
        }
    }

    public static void drawSlashGlow(PoseStack matrix, MultiBufferSource.BufferSource buffer,
                                     RenderType type, float length, float width, float alpha,
                                     Vector3f dir, Vector3f side) {
        VertexConsumer consumer = buffer.getBuffer(type);
        Matrix4f pose = matrix.last().pose();
        float halfLength = length * 0.5f;

        for (int i = 0; i < SEGMENTS; i++) {
            float t0 = i / (float) SEGMENTS;
            float t1 = (i + 1) / (float) SEGMENTS;
            float along0 = -halfLength + length * t0;
            float along1 = -halfLength + length * t1;
            float hw0 = width * 0.5f * (1.0f - Math.abs(2.0f * t0 - 1.0f));
            float hw1 = width * 0.5f * (1.0f - Math.abs(2.0f * t1 - 1.0f));

            Vector3f p0 = new Vector3f(dir).mul(along0);
            Vector3f p1 = new Vector3f(dir).mul(along1);
            Vector3f s0 = new Vector3f(side).mul(hw0);
            Vector3f s1 = new Vector3f(side).mul(hw1);

            Vector3f a = new Vector3f(p0).add(s0);
            Vector3f b = new Vector3f(p0).sub(s0);
            Vector3f c = new Vector3f(p1).sub(s1);
            Vector3f d = new Vector3f(p1).add(s1);
            quad(pose, consumer, a, b, c, d, t0, t1, side, alpha);
        }
    }

    public static void drawSlashBladeFallback(PoseStack matrix, MultiBufferSource buffer,
                                              RenderType type, float length, float width,
                                              float thickness, float alpha, Vector3f dir,
                                              Vector3f side, Vector3f thick, float progress,
                                              YuanSpaceSlashParams params) {
        VertexConsumer consumer = buffer.getBuffer(type);
        Matrix4f pose = matrix.last().pose();
        float halfLength = length * 0.5f;
        float halfThickness = thickness * 0.5f;
        Vector3f z = new Vector3f(thick).mul(halfThickness);

        for (int i = 0; i < SEGMENTS; i++) {
            float t0 = i / (float) SEGMENTS;
            float t1 = (i + 1) / (float) SEGMENTS;
            float along0 = -halfLength + length * t0;
            float along1 = -halfLength + length * t1;
            float hw0 = width * 0.5f * (1.0f - Math.abs(2.0f * t0 - 1.0f));
            float hw1 = width * 0.5f * (1.0f - Math.abs(2.0f * t1 - 1.0f));

            Vector3f p0 = new Vector3f(dir).mul(along0);
            Vector3f p1 = new Vector3f(dir).mul(along1);
            Vector3f s0 = new Vector3f(side).mul(hw0);
            Vector3f s1 = new Vector3f(side).mul(hw1);

            Vector3f a = new Vector3f(p0).add(s0).add(z);
            Vector3f b = new Vector3f(p0).sub(s0).add(z);
            Vector3f c = new Vector3f(p1).sub(s1).add(z);
            Vector3f d = new Vector3f(p1).add(s1).add(z);
            quadColor(pose, consumer, a, b, c, d, t0, t1, alpha, progress, false, thick, params);

            a = new Vector3f(p0).add(s0).sub(z);
            b = new Vector3f(p0).sub(s0).sub(z);
            c = new Vector3f(p1).sub(s1).sub(z);
            d = new Vector3f(p1).add(s1).sub(z);
            quadColor(pose, consumer, a, b, c, d, t0, t1, alpha, progress, false,
                    new Vector3f(thick).negate(), params);

            a = new Vector3f(p0).add(s0).add(z);
            b = new Vector3f(p0).add(s0).sub(z);
            c = new Vector3f(p1).add(s1).sub(z);
            d = new Vector3f(p1).add(s1).add(z);
            sideQuadColor(pose, consumer, a, b, c, d, t0, t1, alpha, progress, false, side, params);

            a = new Vector3f(p0).sub(s0).add(z);
            b = new Vector3f(p0).sub(s0).sub(z);
            c = new Vector3f(p1).sub(s1).sub(z);
            d = new Vector3f(p1).sub(s1).add(z);
            sideQuadColor(pose, consumer, a, b, c, d, t0, t1, alpha, progress, false,
                    new Vector3f(side).negate(), params);
        }
    }

    public static void drawSlashGlowFallback(PoseStack matrix, MultiBufferSource buffer,
                                             RenderType type, float length, float width,
                                             float alpha, Vector3f dir, Vector3f side,
                                             float progress, YuanSpaceSlashParams params) {
        VertexConsumer consumer = buffer.getBuffer(type);
        Matrix4f pose = matrix.last().pose();
        float halfLength = length * 0.5f;

        for (int i = 0; i < SEGMENTS; i++) {
            float t0 = i / (float) SEGMENTS;
            float t1 = (i + 1) / (float) SEGMENTS;
            float along0 = -halfLength + length * t0;
            float along1 = -halfLength + length * t1;
            float hw0 = width * 0.5f * (1.0f - Math.abs(2.0f * t0 - 1.0f));
            float hw1 = width * 0.5f * (1.0f - Math.abs(2.0f * t1 - 1.0f));

            Vector3f p0 = new Vector3f(dir).mul(along0);
            Vector3f p1 = new Vector3f(dir).mul(along1);
            Vector3f s0 = new Vector3f(side).mul(hw0);
            Vector3f s1 = new Vector3f(side).mul(hw1);

            Vector3f a = new Vector3f(p0).add(s0);
            Vector3f b = new Vector3f(p0).sub(s0);
            Vector3f c = new Vector3f(p1).sub(s1);
            Vector3f d = new Vector3f(p1).add(s1);
            quadColor(pose, consumer, a, b, c, d, t0, t1, alpha, progress, true, side, params);
        }
    }

    private static void quadColor(Matrix4f pose, VertexConsumer consumer,
                                  Vector3f a, Vector3f b, Vector3f c, Vector3f d,
                                  float u0, float u1, float alpha, float progress,
                                  boolean glow, Vector3f normal, YuanSpaceSlashParams params) {
        vertexColor(pose, consumer, a, u0, 0.0f, normal, shade(u0, 0.0f, progress, alpha, glow, params));
        vertexColor(pose, consumer, b, u0, 1.0f, normal, shade(u0, 1.0f, progress, alpha, glow, params));
        vertexColor(pose, consumer, c, u1, 1.0f, normal, shade(u1, 1.0f, progress, alpha, glow, params));
        vertexColor(pose, consumer, a, u0, 0.0f, normal, shade(u0, 0.0f, progress, alpha, glow, params));
        vertexColor(pose, consumer, c, u1, 1.0f, normal, shade(u1, 1.0f, progress, alpha, glow, params));
        vertexColor(pose, consumer, d, u1, 0.0f, normal, shade(u1, 0.0f, progress, alpha, glow, params));
    }

    private static void sideQuadColor(Matrix4f pose, VertexConsumer consumer,
                                      Vector3f a, Vector3f b, Vector3f c, Vector3f d,
                                      float u0, float u1, float alpha, float progress,
                                      boolean glow, Vector3f normal, YuanSpaceSlashParams params) {
        vertexColor(pose, consumer, a, u0, 0.5f, normal, shade(u0, 0.5f, progress, alpha, glow, params));
        vertexColor(pose, consumer, b, u0, 0.5f, normal, shade(u0, 0.5f, progress, alpha, glow, params));
        vertexColor(pose, consumer, c, u1, 0.5f, normal, shade(u1, 0.5f, progress, alpha, glow, params));
        vertexColor(pose, consumer, a, u0, 0.5f, normal, shade(u0, 0.5f, progress, alpha, glow, params));
        vertexColor(pose, consumer, c, u1, 0.5f, normal, shade(u1, 0.5f, progress, alpha, glow, params));
        vertexColor(pose, consumer, d, u1, 0.5f, normal, shade(u1, 0.5f, progress, alpha, glow, params));
    }

    private static void vertexColor(Matrix4f pose, VertexConsumer consumer,
                                    Vector3f pos, float u, float v,
                                    Vector3f normal, float[] shade) {
        consumer.vertex(pose, pos.x(), pos.y(), pos.z())
                .color(shade[0], shade[1], shade[2], shade[3])
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal.x(), normal.y(), normal.z())
                .endVertex();
    }

    private static float[] shade(float u, float v, float progress, float alpha,
                                 boolean glow, YuanSpaceSlashParams params) {
        float d = Math.abs(v - 0.5f) * 2.0f;
        float endFade = smoothstep(0.0f, params.tipFade, u) * smoothstep(1.0f, 1.0f - params.tipFade, u);
        float revealEnd = Math.min(progress / params.sweepSpeed, 1.0f);
        float sweep = 1.0f - smoothstep(revealEnd - params.sweepSoftness, revealEnd, u);
        float fadeFrom = Math.max(params.fadeStart, Math.min(1.0f, params.sweepSpeed + params.holdFraction));
        float fadeOut = 1.0f - smoothstep(fadeFrom, fadeFrom + params.fadeDuration, progress);
        float reveal = endFade * sweep * fadeOut;

        if (glow) {
            float edge = smoothstep(params.edgeWidth, Math.min(1.0f, params.edgeWidth + 0.06f), d)
                    * (1.0f - smoothstep(0.94f, 0.98f, d));
            float glowBand = smoothstep(params.glowWidth, 0.98f, d)
                    * (1.0f - smoothstep(0.99f, 1.0f, d));
            float strength = glowBand * params.glowStrength + edge * 0.12f * params.edgeBrightness;
            float[] glowRgb = colorRgb(params.glowColor);
            return new float[]{
                    glowRgb[0] * strength,
                    glowRgb[1] * strength,
                    glowRgb[2] * strength,
                    alpha * reveal * strength
            };
        }

        float core = smoothstep(1.0f, params.coreWidth, d);
        float edge = smoothstep(params.edgeWidth, Math.min(1.0f, params.edgeWidth + 0.06f), d)
                * (1.0f - smoothstep(0.94f, 0.98f, d));
        float glowBand = smoothstep(params.glowWidth, 0.98f, d)
                * (1.0f - smoothstep(0.99f, 1.0f, d));
        float coreShade = (0.09f - 0.05f * d * d) * params.coreShade;
        float[] coreRgb = colorRgb(params.coreColor);
        float[] edgeRgb = colorRgb(params.edgeColor);
        float[] glowRgb = colorRgb(params.glowColor);
        float r = coreRgb[0] * coreShade * core + edgeRgb[0] * params.edgeBrightness * edge
                + glowRgb[0] * params.glowStrength * glowBand;
        float g = coreRgb[1] * coreShade * core + edgeRgb[1] * params.edgeBrightness * edge
                + glowRgb[1] * params.glowStrength * glowBand;
        float b = coreRgb[2] * coreShade * core + edgeRgb[2] * params.edgeBrightness * edge
                + glowRgb[2] * params.glowStrength * glowBand;
        float outAlpha = alpha * reveal;
        return new float[]{
                Math.min(1.0f, Math.max(0.0f, r)),
                Math.min(1.0f, Math.max(0.0f, g)),
                Math.min(1.0f, Math.max(0.0f, b)),
                outAlpha
        };
    }

    private static float[] colorRgb(int argb) {
        return new float[]{
                ((argb >> 16) & 0xFF) / 255.0f,
                ((argb >> 8) & 0xFF) / 255.0f,
                (argb & 0xFF) / 255.0f
        };
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.max(0.0f, Math.min(1.0f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0f - 2.0f * t);
    }

    private static void quad(Matrix4f pose, VertexConsumer consumer,
                             Vector3f a, Vector3f b, Vector3f c, Vector3f d,
                             float u0, float u1, Vector3f normal, float alpha) {
        vertex(pose, consumer, a, u0, 0.0f, normal, alpha);
        vertex(pose, consumer, b, u0, 1.0f, normal, alpha);
        vertex(pose, consumer, c, u1, 1.0f, normal, alpha);
        vertex(pose, consumer, a, u0, 0.0f, normal, alpha);
        vertex(pose, consumer, c, u1, 1.0f, normal, alpha);
        vertex(pose, consumer, d, u1, 0.0f, normal, alpha);
    }

    private static void sideQuad(Matrix4f pose, VertexConsumer consumer,
                                 Vector3f a, Vector3f b, Vector3f c, Vector3f d,
                                 float u0, float u1, Vector3f normal, float alpha) {
        vertex(pose, consumer, a, u0, 0.5f, normal, alpha);
        vertex(pose, consumer, b, u0, 0.5f, normal, alpha);
        vertex(pose, consumer, c, u1, 0.5f, normal, alpha);
        vertex(pose, consumer, a, u0, 0.5f, normal, alpha);
        vertex(pose, consumer, c, u1, 0.5f, normal, alpha);
        vertex(pose, consumer, d, u1, 0.5f, normal, alpha);
    }

    private static void vertex(Matrix4f pose, VertexConsumer consumer,
                               Vector3f pos, float u, float v,
                               Vector3f normal, float alpha) {
        consumer.vertex(pose, pos.x(), pos.y(), pos.z())
                .color(1.0f, 1.0f, 1.0f, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal.x(), normal.y(), normal.z())
                .endVertex();
    }
}
