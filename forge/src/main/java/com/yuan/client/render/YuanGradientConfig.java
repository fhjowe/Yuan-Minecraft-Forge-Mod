/*
 * Decompiled with CFR 0.152.
 */
package com.yuan.client.render;

import java.util.List;

public class YuanGradientConfig {
    private final List<Integer> colors;
    private final float speed;
    private final GradientType type;
    private final boolean clockwise;

    public YuanGradientConfig(List<Integer> colors, float speed, GradientType type) {
        this(colors, speed, type, true);
    }

    public YuanGradientConfig(List<Integer> colors, float speed, GradientType type, boolean clockwise) {
        this.colors = colors;
        this.speed = speed;
        this.type = type;
        this.clockwise = clockwise;
    }

    public List<Integer> getColors() {
        return this.colors;
    }

    public float getSpeed() {
        return this.speed;
    }

    public GradientType getType() {
        return this.type;
    }

    public boolean isClockwise() {
        return this.clockwise;
    }

    public int getColorAt(float progress, long time) {
        float animatedProgress;
        if (this.colors.isEmpty()) {
            return -1;
        }
        if (this.colors.size() == 1) {
            return this.colors.get(0);
        }
        float offset = 0.0f;
        if (this.type == GradientType.ANIMATED || this.type == GradientType.BORDER_CIRCULAR) {
            offset = (float)(time % (long)(10000.0f / this.speed)) / (10000.0f / this.speed);
            if (!this.clockwise) {
                offset = -offset;
            }
        }
        if ((animatedProgress = (progress + offset) % 1.0f) < 0.0f) {
            animatedProgress += 1.0f;
        }
        float scaledProgress = animatedProgress * (float)this.colors.size();
        int index1 = (int)scaledProgress % this.colors.size();
        int index2 = (index1 + 1) % this.colors.size();
        float blend = scaledProgress - (float)((int)scaledProgress);
        return YuanGradientConfig.interpolateColor(this.colors.get(index1), this.colors.get(index2), blend);
    }

    private static int interpolateColor(int color1, int color2, float factor) {
        int a1 = color1 >> 24 & 0xFF;
        int r1 = color1 >> 16 & 0xFF;
        int g1 = color1 >> 8 & 0xFF;
        int b1 = color1 & 0xFF;
        int a2 = color2 >> 24 & 0xFF;
        int r2 = color2 >> 16 & 0xFF;
        int g2 = color2 >> 8 & 0xFF;
        int b2 = color2 & 0xFF;
        if (a1 == 0) {
            a1 = 255;
        }
        if (a2 == 0) {
            a2 = 255;
        }
        int a = (int)((float)a1 + (float)(a2 - a1) * factor);
        int r = (int)((float)r1 + (float)(r2 - r1) * factor);
        int g = (int)((float)g1 + (float)(g2 - g1) * factor);
        int b = (int)((float)b1 + (float)(b2 - b1) * factor);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static enum GradientType {
        HORIZONTAL,
        VERTICAL,
        RADIAL,
        ANIMATED,
        BORDER_CIRCULAR;

    }
}

