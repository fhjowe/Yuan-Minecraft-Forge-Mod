package com.yuan.timestop.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class YuanTimeStopRender {
    public static final ResourceLocation beam =
            new ResourceLocation("yuan:textures/item/white.png");
    private static Map<ParticleRenderType, Queue<Particle>> particleQueues;
    private static final List<YuanTimeStopEffect> effects = new ArrayList<>();

    private YuanTimeStopRender() {
    }

    public static void renderSphere(PoseStack matrix, MultiBufferSource buffer, float radius,
                                    int gradation, int lightX, int lightY,
                                    float r, float g, float b, float a,
                                    RenderType type, float percentage) {
        float pi = (float) Math.PI;
        VertexConsumer consumer = buffer.getBuffer(type);
        Matrix4f pose = matrix.last().pose();
        for (float alpha = 0.0f; alpha < pi; alpha += pi / gradation) {
            for (float beta = 0.0f; beta < pi * 2.0f * percentage; beta += pi / gradation) {
                float x = (float) (radius * Math.cos(beta) * Math.sin(alpha));
                float y = (float) (radius * Math.sin(beta) * Math.sin(alpha));
                float z = (float) (radius * Math.cos(alpha));
                consumer.vertex(pose, x, y, z).color(r, g, b, a).uv(0.0f, 1.0f)
                        .uv2(lightX, lightY).endVertex();
                double nextAlpha = alpha + pi / gradation;
                x = (float) (radius * Math.cos(beta) * Math.sin(nextAlpha));
                y = (float) (radius * Math.sin(beta) * Math.sin(nextAlpha));
                z = (float) (radius * Math.cos(nextAlpha));
                consumer.vertex(pose, x, y, z).color(r, g, b, a).uv(0.0f, 1.0f)
                        .uv2(lightX, lightY).endVertex();
            }
        }
    }

    public static void renderSphere(PoseStack matrix, MultiBufferSource buffer, float radius,
                                    int gradation, int lightX, int lightY,
                                    float r, float g, float b, float a, RenderType type) {
        renderSphere(matrix, buffer, radius, gradation, lightX, lightY,
                r, g, b, a, type, 1.0f);
    }

    public static void renderRing(PoseStack matrix, MultiBufferSource buffer, float radius,
                                  int gradation, int lightX, int lightY,
                                  float r, float g, float b, float a, RenderType type) {
        VertexConsumer consumer = buffer.getBuffer(type);
        Matrix4f pose = matrix.last().pose();
        float inner = radius * 0.78f;
        float step = (float) (Math.PI * 2.0 / gradation);
        for (float beta = 0.0f; beta < Math.PI * 2.0f - step * 0.5f; beta += step) {
            float cos0 = (float) Math.cos(beta);
            float sin0 = (float) Math.sin(beta);
            float cos1 = (float) Math.cos(beta + step);
            float sin1 = (float) Math.sin(beta + step);
            consumer.vertex(pose, radius * cos0, 0.0f, radius * sin0)
                    .color(r, g, b, a).uv(0.0f, 0.0f).uv2(lightX, lightY).endVertex();
            consumer.vertex(pose, inner * cos0, 0.0f, inner * sin0)
                    .color(r, g, b, a).uv(0.0f, 0.0f).uv2(lightX, lightY).endVertex();
            consumer.vertex(pose, radius * cos1, 0.0f, radius * sin1)
                    .color(r, g, b, a).uv(0.0f, 0.0f).uv2(lightX, lightY).endVertex();
            consumer.vertex(pose, inner * cos1, 0.0f, inner * sin1)
                    .color(r, g, b, a).uv(0.0f, 0.0f).uv2(lightX, lightY).endVertex();
        }
    }

    public static void spawnEffect(YuanTimeStopEffect effect) {
        if (effect != null) {
            effects.add(effect);
        }
    }

    public static void particleRenders(Entity entity, PoseStack matrix, float partialTicks) {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        HashSet<Particle> particles = getNoRenderParticles(Minecraft.getInstance().particleEngine);
        if (particles.isEmpty()) {
            return;
        }
        for (Particle particle : particles) {
            if (!(particle instanceof YuanTimeStopParticle timeStopParticle)
                    || !timeStopParticle.isAlive()) {
                continue;
            }
            timeStopParticle.tick();
            YuanTimeStopParticle.render(timeStopParticle,
                    camera.x, camera.y, camera.z, matrix, partialTicks);
        }
    }

    public static HashSet<Particle> getNoRenderParticles(ParticleEngine manager) {
        if (particleQueues == null) {
            particleQueues = manager.particles;
        }
        Queue<Particle> queue = particleQueues.get(ParticleRenderType.NO_RENDER);
        return queue == null ? new HashSet<>() : new HashSet<>(queue);
    }

    public static void renderEffects(Entity entity, PoseStack matrix, float partialTicks) {
        if (effects.isEmpty()) {
            return;
        }
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffer =
                Minecraft.getInstance().renderBuffers().bufferSource();
        Iterator<YuanTimeStopEffect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            YuanTimeStopEffect effect = iterator.next();
            if (effect.isFinished()) {
                iterator.remove();
                continue;
            }
            effect.render(matrix, buffer, camera.x, camera.y, camera.z);
        }
    }

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity != null) {
            particleRenders(cameraEntity, event.getPoseStack(), event.getPartialTick());
            renderEffects(cameraEntity, event.getPoseStack(), event.getPartialTick());
        }
    }
}
