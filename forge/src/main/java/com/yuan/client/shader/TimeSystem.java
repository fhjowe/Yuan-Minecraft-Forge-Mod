package com.yuan.client.shader;

public class TimeSystem {
    private static final long startTime = System.currentTimeMillis();

    public static float getShaderTimeValue() {
        return (float)((System.currentTimeMillis() - startTime) % 3600000L) / 20000.0f;
    }
}
