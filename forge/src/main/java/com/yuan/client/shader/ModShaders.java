package com.yuan.client.shader;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector2f;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModShaders {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("ModShaders");
    public static ShaderInstance rainbow_slime_shader;
    public static ShaderInstance liquid_glass_shader;
    public static ShaderInstance blur_shader;

    public static ShaderInstance getRainbowSlimeShader() { return rainbow_slime_shader; }
    public static ShaderInstance getLiquidGlassShader() { return liquid_glass_shader; }
    public static ShaderInstance getBlurShader() { return blur_shader; }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("yuan", "rainbow_slime_block"), DefaultVertexFormat.BLOCK), shader -> rainbow_slime_shader = shader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("yuan", "liquid_glass"), DefaultVertexFormat.POSITION_COLOR), shader -> liquid_glass_shader = shader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("yuan", "blur"), DefaultVertexFormat.POSITION_TEX), shader -> blur_shader = shader);
    }

    public static Vector2f getScreenSize() {
        Minecraft mc = Minecraft.getInstance();
        try { Window wh = mc.getWindow(); int w = wh.getWidth(), h = wh.getHeight(); if (w > 0 && h > 0) return new Vector2f(w, h); } catch (Exception e) {}
        return new Vector2f(1920.0f, 1080.0f);
    }
}
