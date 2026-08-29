package com.yuan.timestop;

import com.mojang.blaze3d.shaders.Uniform;
import com.yuan.item.YuanGodSwordConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class YuanTimeStopShaders {
    public static float timeTheWorld = 0.0f;
    private static long lastMillis = Util.getMillis();

    private YuanTimeStopShaders() {
    }

    public static void setup() {
        if (!YuanTimeStopConfig.specialShader) {
            return;
        }
        timeTheWorld = YuanTimeStop.get() ? timeTheWorld + 0.001f : 0.0f;
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = Util.getMillis();
        long delta = now - lastMillis;
        lastMillis = now;
        YuanTimeStop.realMillis += delta;
        setup();
        if (!YuanTimeStop.get()) {
            YuanTimeStop.millis += delta;
            return;
        }
        updateUniform("time", timeTheWorld);
        YuanGodSwordConfig config = YuanTimeStop.getActiveConfig();
        if (config.grayScreen) {
            float base = 1.0f - config.grayStrength;
            float value;
            if (config.grayAnimate) {
                float phase = (System.currentTimeMillis() % 2400L) / 2400.0f;
                float wave = 0.5f + 0.5f * (float) Math.sin(phase * Math.PI * 2.0);
                value = base + (1.0f - base) * wave;
            } else {
                value = base;
            }
            updateUniform("Saturation", Math.max(0.0f, Math.min(1.0f, value)));
        } else {
            updateUniform("Saturation", 1.0f);
        }
        updateUniform("Style", config.grayScreen ? config.grayStyle : 0f);
    }

    public static void post() {
        if (YuanTimeStop.get() && YuanTimeStopConfig.specialShader) {
            Minecraft.getInstance().gameRenderer.loadEffect(effectLocation());
        } else {
            Minecraft.getInstance().gameRenderer.shutdownEffect();
        }
    }

    public static ResourceLocation effectLocation() {
        return YuanTimeStop.getActiveConfig().grayStyle > 0
                ? new ResourceLocation("shaders/post/yuan_world_style.json")
                : new ResourceLocation("shaders/post/the_world.json");
    }

    private static void updateUniform(String name, float value) {
        var effect = Minecraft.getInstance().gameRenderer.currentEffect();
        if (effect == null) {
            return;
        }
        for (PostPass pass : effect.passes) {
            Uniform uniform = pass.getEffect().getUniform(name);
            if (uniform != null) {
                uniform.set(value);
            }
        }
    }
}
