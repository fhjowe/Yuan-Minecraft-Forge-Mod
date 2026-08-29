package com.yuan.timestop;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class YuanTimeStopConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue GRAY_SCREEN =
            BUILDER.comment("Set gray screen during freeze time").define("isGrayScreen", true);
    private static final ForgeConfigSpec.BooleanValue SPECIAL_SHADER =
            BUILDER.comment("Enable Special Shader").define("specialShader", true);
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue INVULNERABLE =
            SERVER_BUILDER.comment("Make you invulnerable during freeze time").define("invulnerable", true);
    public static final ForgeConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    public static boolean grayScreen;
    public static boolean specialShader;
    public static boolean invulnerable;

    private YuanTimeStopConfig() {
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            grayScreen = GRAY_SCREEN.get();
            specialShader = SPECIAL_SHADER.get();
        } else if (event.getConfig().getSpec() == SERVER_SPEC) {
            invulnerable = INVULNERABLE.get();
            YuanTimeStopServerState.setInvulnerable(invulnerable);
        }
    }
}
