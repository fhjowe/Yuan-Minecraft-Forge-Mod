package com.yuan.timestop;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class YuanTimeStopSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "yuan");
    public static final RegistryObject<SoundEvent> STOP = SOUNDS.register(
            "stop", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("yuan", "stop")));
    public static final RegistryObject<SoundEvent> START = SOUNDS.register(
            "start", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("yuan", "start")));

    private YuanTimeStopSounds() {
    }
}
