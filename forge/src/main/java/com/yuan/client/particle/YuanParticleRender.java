package com.yuan.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class YuanParticleRender {

    public static class GildedSpark extends TextureSheetParticle {
        protected GildedSpark(ClientLevel level, double x, double y, double z,
                double vx, double vy, double vz) {
            super(level, x, y, z, vx, vy, vz);
            this.xd = vx;
            this.yd = vy;
            this.zd = vz;
            this.quadSize = 0.15f;
            this.lifetime = 20;
            this.hasPhysics = false;
            this.gravity = 0.0f;
        }

        @Override
        public void tick() {
            super.tick();
            float progress = (float) this.age / this.lifetime;
            this.alpha = 1.0f - progress;
            this.quadSize = 0.15f * (1.0f - progress * 0.5f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    public static class GildedRing extends TextureSheetParticle {
        protected GildedRing(ClientLevel level, double x, double y, double z,
                double vx, double vy, double vz) {
            super(level, x, y, z, vx, vy, vz);
            this.xd = 0;
            this.yd = 0.01;
            this.zd = 0;
            this.quadSize = 0.35f;
            this.lifetime = 15;
            this.hasPhysics = false;
            this.gravity = 0.0f;
        }

        @Override
        public void tick() {
            super.tick();
            float progress = (float) this.age / this.lifetime;
            this.alpha = 1.0f - progress;
            this.quadSize = 0.35f + progress * 0.2f;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    public static class SparkProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SparkProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                double x, double y, double z, double vx, double vy, double vz) {
            GildedSpark particle = new GildedSpark(level, x, y, z, vx, vy, vz);
            particle.pickSprite(sprite);
            return particle;
        }
    }

    public static class RingProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public RingProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                double x, double y, double z, double vx, double vy, double vz) {
            GildedRing particle = new GildedRing(level, x, y, z, vx, vy, vz);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
