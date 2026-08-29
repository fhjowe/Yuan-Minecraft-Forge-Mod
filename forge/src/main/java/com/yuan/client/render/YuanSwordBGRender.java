package com.yuan.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.yuan.client.shader.ModRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;

public class YuanSwordBGRender {
    private static final ResourceLocation BG_TEXTURE =
        new ResourceLocation("yuan", "textures/images/yuan_blade_bg.png");

    // Image is 2784x1536 (aspect 1.812)
    // Fox blade was 700x840 rendered at 1.5x = 1050x1260
    // We use proportional sizing
    private static final float RENDER_SIZE = 1.0f;

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource,
            ItemDisplayContext context, int packedLight) {
        if (context != ItemDisplayContext.FIXED) return;

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.505f);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-25.0f));

        VertexConsumer consumer = bufferSource.getBuffer(ModRenderType.foxBladeBackground(BG_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float size = RENDER_SIZE;

        consumer.vertex(matrix, -size / 2.0f, -size / 2.0f, 0.0f).color(255, 255, 255, 255).uv(0.0f, 1.0f).overlayCoords(655360).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, size / 2.0f, -size / 2.0f, 0.0f).color(255, 255, 255, 255).uv(1.0f, 1.0f).overlayCoords(655360).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, size / 2.0f, size / 2.0f, 0.0f).color(255, 255, 255, 255).uv(1.0f, 0.0f).overlayCoords(655360).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, -size / 2.0f, size / 2.0f, 0.0f).color(255, 255, 255, 255).uv(0.0f, 0.0f).overlayCoords(655360).uv2(packedLight).normal(0, 0, 1).endVertex();

        poseStack.popPose();
    }
}
