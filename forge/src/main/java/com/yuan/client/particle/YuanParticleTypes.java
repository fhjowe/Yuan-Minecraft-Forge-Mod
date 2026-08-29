package com.yuan.client.particle;

import com.yuan.Yuan;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class YuanParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Yuan.MOD_ID);

    public static final RegistryObject<SimpleParticleType> GILDED_RING = PARTICLE_TYPES.register("gilded_ring",
        () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> GILDED_SPARK = PARTICLE_TYPES.register("gilded_spark",
        () -> new SimpleParticleType(true));
}
