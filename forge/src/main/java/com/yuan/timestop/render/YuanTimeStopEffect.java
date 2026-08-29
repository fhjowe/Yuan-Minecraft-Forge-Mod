package com.yuan.timestop.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Real-time driven start/end effect for the god sword timestop.
 * Burst and collapse render as expanding/shrinking spheres, shockwave as a
 * flat ring on the ground. Progress is computed from wall-clock milliseconds so
 * startDuration/endDuration are actual seconds regardless of frame rate.
 */
public final class YuanTimeStopEffect {
    public enum Kind {
        BURST,
        SHOCKWAVE,
        COLLAPSE
    }

    private final Kind kind;
    private final Vec3 origin;
    private final float durationSeconds;
    private final float maxRadius;
    private final float alpha;
    private final float[] rgba = new float[4];
    private final long startMillis;

    public YuanTimeStopEffect(Kind kind, Vec3 origin, float durationSeconds,
                              float maxRadius, float[] color) {
        this.kind = kind;
        this.origin = origin;
        this.durationSeconds = Math.max(0.1f, durationSeconds);
        this.maxRadius = Math.max(0.1f, maxRadius);
        this.alpha = color.length > 3 ? color[3] : 0.5f;
        this.rgba[0] = color[0];
        this.rgba[1] = color[1];
        this.rgba[2] = color[2];
        this.rgba[3] = this.alpha;
        this.startMillis = System.currentTimeMillis();
    }

    public boolean isFinished() {
        return progress() >= 1.0f;
    }

    public void render(PoseStack matrix, MultiBufferSource.BufferSource buffer,
                       double cameraX, double cameraY, double cameraZ) {
        float t = progress();
        if (t <= 0.0f) {
            return;
        }
        float eased = 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);
        float radius;
        float a;
        switch (kind) {
            case BURST -> {
                radius = maxRadius * eased;
                a = alpha * (1.0f - t);
            }
            case SHOCKWAVE -> {
                radius = maxRadius * eased;
                a = alpha * (1.0f - t * 0.6f);
            }
            default -> {
                radius = maxRadius * (1.0f - eased);
                a = alpha;
            }
        }
        if (radius <= 0.001f || a <= 0.001f) {
            return;
        }
        matrix.pushPose();
        matrix.translate(origin.x - cameraX, origin.y - cameraY, origin.z - cameraZ);
        ResourceLocation texture = YuanTimeStopRender.beam;
        GlowRenderLayer glowLayer = new GlowRenderLayer(
                new CullWrappedRenderLayer(
                        MegaRenderType.createSphereRenderType(texture, 2)),
                rgba, 0.01f, false);
        if (kind == Kind.SHOCKWAVE) {
            YuanTimeStopRender.renderRing(matrix, buffer, radius, 32,
                    240, 240, rgba[0], rgba[1], rgba[2], a, glowLayer);
        } else {
            YuanTimeStopRender.renderSphere(matrix, buffer, radius, 20,
                    240, 240, rgba[0], rgba[1], rgba[2], a, glowLayer);
        }
        buffer.endBatch(glowLayer);
        matrix.popPose();
    }

    private float progress() {
        long now = System.currentTimeMillis();
        long elapsed = now - startMillis;
        return (float) Math.min(1.0, Math.max(0.0,
                (double) elapsed / (durationSeconds * 1000.0)));
    }
}
