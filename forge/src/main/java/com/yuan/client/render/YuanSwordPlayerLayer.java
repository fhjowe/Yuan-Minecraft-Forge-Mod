/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  org.joml.Matrix4f
 */
package com.yuan.client.render;

import net.minecraft.resources.ResourceLocation;
import com.yuan.registry.YuanItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class YuanSwordPlayerLayer
extends net.minecraft.client.renderer.entity.layers.RenderLayer<net.minecraft.client.player.AbstractClientPlayer, net.minecraft.client.model.PlayerModel<net.minecraft.client.player.AbstractClientPlayer>> {
    private static final ResourceLocation FOX_TEXTURE = new ResourceLocation("yuan", "textures/images/yuan_blade_bg.png");
    private static final float TEXTURE_SIZE = 2.0f;
    private static final float DISTANCE_BEHIND = 0.5f;

    public YuanSwordPlayerLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.getMainHandItem().getItem() != YuanItems.YUAN_SWORD) {
            return;
        }
        poseStack.pushPose();
        ModelPart body = ((PlayerModel)this.getParentModel()).body;
        poseStack.translate(body.x / 16.0f, body.y / 16.0f, body.z / 16.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.translate(0.0f, 1.2f, -1.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        poseStack.scale(3.0f, 3.0f, 3.0f);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent((ResourceLocation)FOX_TEXTURE));
        this.renderTexture(vertexConsumer, poseStack);
        poseStack.popPose();
    }

    private void renderTexture(VertexConsumer consumer, PoseStack poseStack) {
        Matrix4f pose = poseStack.last().pose();
        float halfW = 1.0f;
        float texAspect = 2784.0f / 1536.0f;
        float halfH = halfW / texAspect;
        int alpha = 205;
        int fullBright = 0xF000F0;
        consumer.vertex(pose, -halfW, 0.0f, 0.0f).color(255, 255, 255, alpha).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullBright).normal(0.0f, 0.0f, 1.0f).endVertex();
        consumer.vertex(pose, halfW, 0.0f, 0.0f).color(255, 255, 255, alpha).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullBright).normal(0.0f, 0.0f, 1.0f).endVertex();
        consumer.vertex(pose, halfW, halfH * 2, 0.0f).color(255, 255, 255, alpha).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullBright).normal(0.0f, 0.0f, 1.0f).endVertex();
        consumer.vertex(pose, -halfW, halfH * 2, 0.0f).color(255, 255, 255, alpha).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullBright).normal(0.0f, 0.0f, 1.0f).endVertex();
    }
}

