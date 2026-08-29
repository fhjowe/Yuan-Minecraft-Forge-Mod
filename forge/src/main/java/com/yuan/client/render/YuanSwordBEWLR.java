package com.yuan.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yuan.client.shader.ModRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class YuanSwordBEWLR extends BlockEntityWithoutLevelRenderer {
    public static final YuanSwordBEWLR INSTANCE = new YuanSwordBEWLR();
    public static final ResourceLocation BASE_MODEL = new ResourceLocation("yuan", "item/yuan_sword_base");
    private static final ResourceLocation SWORD_TEXTURE =
            new ResourceLocation("yuan", "textures/item/yuan_sword.png");

    private YuanSwordBEWLR() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
              Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getModelManager().getModel(BASE_MODEL);

        // The Yuan sword is fullbright in every view, matching its bright icon.
        light = 0xF000F0;

        int quadCount = countQuads(model);

        if (isModelTransformContext(context)) {
            poseStack.translate(0.5, 0.5, 0.5);
            boolean leftHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            model.getTransforms().getTransform(context).apply(leftHand, poseStack);
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        if (quadCount <= 2 && model != mc.getModelManager().getMissingModel()) {
            // Flat baked model: render through the standard item path.
            mc.getItemRenderer().renderModelLists(model, stack, light, overlay, poseStack,
                buffer.getBuffer(RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS)));
        } else {
            // Unexpected or missing geometry: force one flat sprite quad.
            renderFlatQuad(poseStack, buffer, light, overlay);
        }

        YuanSwordBGRender.render(poseStack, buffer, context, light);
    }

    private static int countQuads(BakedModel model) {
        int count = 0;
        RandomSource random = RandomSource.create();
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            count += model.getQuads(null, direction, random).size();
        }
        return count;
    }

    private static void renderFlatQuad(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        VertexConsumer vc = buffer.getBuffer(ModRenderType.foxBladeBackground(SWORD_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        // Same depth as vanilla item/generated so handheld transforms land correctly.
        float z = 8.0f;
        vc.vertex(matrix, 0, 0, z).color(255, 255, 255, 255).uv(0.0f, 1.0f).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, 16, 0, z).color(255, 255, 255, 255).uv(1.0f, 1.0f).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, 16, 16, z).color(255, 255, 255, 255).uv(1.0f, 0.0f).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, 0, 16, z).color(255, 255, 255, 255).uv(0.0f, 0.0f).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
    }

    private static boolean isModelTransformContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.GROUND
            || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
            || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
            || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }
}
