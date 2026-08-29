package com.yuan.timestop.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class YuanTimeStopParticle extends Particle {
    public int MAX_LIFE = 100;
    public boolean growing = true;
    public String loc;
    public float[] rgba = new float[4];
    public float softness = 0.01f;
    public float rotation = 0.0f;
    public float rotationSpeed = 0.0f;
    public float sz;
    public double slow;
    public double grow = 0.4;
    public boolean shaders = false;
    public int image;

    public YuanTimeStopParticle(ClientLevel level, double x, double y, double z,
                                float scale, double vx, double vy, double vz,
                                String loc, float size,
                                float r, float g, float b, float a,
                                boolean shaders, double grow) {
        super(level, x, y, z, vx, vy, vz);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.alpha = 0.4f;
        this.MAX_LIFE = this.lifetime = 100;
        this.rgba[0] = r;
        this.rgba[1] = g;
        this.rgba[2] = b;
        this.rgba[3] = a;
        this.loc = loc;
        this.sz = size;
        this.image = this.random.nextInt(6) + 1;
        this.shaders = shaders;
        this.slow = 0.6;
        this.grow = grow;
    }

    public static void render(YuanTimeStopParticle particle, double cameraX, double cameraY,
                              double cameraZ, PoseStack matrix, float partialTicks) {
        double x = particle.xo + (particle.x - particle.xo) * partialTicks;
        double y = particle.yo + (particle.y - particle.yo) * partialTicks;
        double z = particle.zo + (particle.z - particle.zo) * partialTicks;
        matrix.pushPose();
        matrix.translate(x - cameraX, y - cameraY, z - cameraZ);
        if (particle.rotationSpeed != 0.0f) {
            matrix.mulPose(new Quaternionf().rotateZ(particle.roll));
        }
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity != null) {
            MultiBufferSource.BufferSource buffer =
                    Minecraft.getInstance().renderBuffers().bufferSource();
            ResourceLocation texture = new ResourceLocation(particle.loc);
            GlowRenderLayer glowLayer = new GlowRenderLayer(
                    new CullWrappedRenderLayer(
                            MegaRenderType.createSphereRenderType(texture, 0)),
                    particle.rgba, particle.softness, particle.shaders);
            double size = particle.growing
                    ? Math.max(0.0, particle.sz - particle.grow + particle.grow * partialTicks)
                    : Math.max(0.0, particle.sz + particle.grow - particle.grow * partialTicks);
            YuanTimeStopRender.renderSphere(matrix, buffer, (float) size, 20,
                    240, 240, particle.rgba[0], particle.rgba[1],
                    particle.rgba[2], particle.rgba[3], glowLayer);
            buffer.endBatch(glowLayer);
        }
        matrix.popPose();
    }

    @Override
    public void setLifetime(int lifetime) {
        super.setLifetime(lifetime);
        this.MAX_LIFE = lifetime;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        if (this.rgba[3] <= 0.0f) {
            this.remove();
        }
        this.xd *= this.slow;
        this.yd *= this.slow;
        this.zd *= this.slow;
        if (this.age > this.MAX_LIFE / 2 + 10) {
            this.growing = false;
        }
        this.sz = this.growing
                ? (float) (this.sz + this.grow)
                : (float) (this.sz - this.grow);
        this.oRoll = this.roll;
        this.roll += this.rotationSpeed;
        this.setPos(this.getPos().x, this.getPos().y, this.getPos().z);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
    }

    @Override
    public Vec3 getPos() {
        return new Vec3(this.x, this.y, this.z);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.NO_RENDER;
    }
}
