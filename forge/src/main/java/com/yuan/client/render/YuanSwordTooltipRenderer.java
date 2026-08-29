package com.yuan.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.yuan.client.shader.ModRenderType;
import com.yuan.client.shader.ModShaders;
import com.yuan.client.shader.TimeSystem;
import com.yuan.item.YuanSwordItem;
import com.yuan.item.YuanConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4i;
import com.mojang.blaze3d.platform.Window;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class YuanSwordTooltipRenderer {
    private static final ResourceLocation FOX = new ResourceLocation("yuan", "textures/images/yuan_blade_bg.png");
    private static final ResourceLocation TRUE_DEMON_BOW_TEXTURE = new ResourceLocation("yuan", "textures/gui/tooltip/true_demon_bow.png");
    private static final ResourceLocation EXTRA_DECORATION = new ResourceLocation("yuan", "textures/gui/tooltip/true_demon_bow_extra_decoration.png");
    private static final ResourceLocation EXTRA_DECORATION_2 = new ResourceLocation("yuan", "textures/gui/tooltip/true_demon_bow_extra_decoration_2.png");
    private static final float BG_WIDTH = 580.0f;
    private static final float BG_HEIGHT = 320.0f;

    private static final int CORNER = 7;
    private static final int TEX_W = 74;
    private static final int TEX_H = 74;
    private static final int DECOR_W = 32;
    private static final int DECOR_H = 32;
    private static final int DECOR_PAD_R = 4;
    private static final int DECOR_PAD_B = 4;
    private static final int DECOR_W_2 = 16;
    private static final int DECOR_H_2 = 16;
    private static final int DECOR_OVERFLOW_B = 0;

    private static final float NOISE_DENSITY = 0.30f;
    private static final float NOISE_UV_SCALE = 64.0f;
    private static final float NOISE_FLOW_SPEED = 0.25f;
    private static final float ALPHA_BASE = 0.09f;
    private static final float ALPHA_MIN = 0.02f;
    private static final float ALPHA_SHADOW_BOOST = 1.10f;
    private static final float SAT_BASE = 0.70f;
    private static final float SAT_SHADOW_GAIN = 0.30f;
    private static final float VAL_BASE = 0.85f;
    private static final float VAL_LUMA_COMP = 0.25f;
    private static final boolean NOISE_ADDITIVE = true;
    private static final long NOISE_PERIOD_MS = 5560L;
    private static final LiquidGlassRenderer liquidGlassRenderer = new LiquidGlassRenderer();

    private static boolean noiseDataLoaded = false;
    private static boolean[] baseMask;
    private static float[] baseLuma;
    private static float[] baseEdge;
    private static boolean[] decor1Mask;
    private static float[] decor1Luma;
    private static float[] decor1Edge;
    private static int decor1W, decor1H;
    private static boolean[] decor2Mask;
    private static float[] decor2Luma;
    private static float[] decor2Edge;
    private static int decor2W, decor2H;
    private static int cachedMapW = -1, cachedMapH = -1;
    private static int[] suMapX = null;
    private static int[] svMapY = null;
    private static final int NOISE_FP = 256;
    private static final int HASH_MASK = 0x7fffffff;
    private static final int NOISE_CUTOFF = (int) ((1.0f - NOISE_DENSITY) * (float) HASH_MASK);

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderTooltip(RenderTooltipEvent.Pre event) {
        if (!(event.getItemStack().getItem() instanceof YuanSwordItem)) return;
        List<Component> components = new ArrayList<>();
        for (Component c : event.getItemStack().getTooltipLines(Minecraft.getInstance().player,
                Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL)) {
            components.add(c);
        }
        if (components.isEmpty()) return;
        Font font = Minecraft.getInstance().font;
        renderCustomTooltip(event.getGraphics(), event.getItemStack(), components, event.getX(), event.getY(), font);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        liquidGlassRenderer.close();
    }

    public static void renderCustomTooltip(GuiGraphics graphics, ItemStack stack, List<Component> components, int x, int y, Font font) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 1000.0f);

        int maxTextWidth = 0, lineCount = 0;
        for (Component c : components) {
            int w = font.width(c);
            if (w > maxTextWidth) maxTextWidth = w;
            if (!c.getString().isEmpty()) lineCount++;
        }
        int bgWidth = maxTextWidth + 40;
        int bgHeight = lineCount * 10 + 18;

        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();
        if (x + bgWidth > screenW) x = screenW - bgWidth - 10;
        if (y + bgHeight > screenH) y = screenH - bgHeight - 10;

        float centerX = x + bgWidth / 2.0f - 5;
        float centerY = y + bgHeight / 2.0f - 20;

        renderRing(graphics, centerX, centerY, 230.0f, 190.0f, 0.9f, -35.0f, 64,
            ModRenderType.rainbow_slime_block, true, false, false);

        renderFoxBackground(graphics, (int)(centerX - BG_WIDTH / 2 + 5), (int)(centerY - BG_HEIGHT / 2));

        if (lineCount > 1) {
            int glassY = y + 4;
            int glassH = lineCount * 10 + 8;
            int glassX = x + 8;
            int glassW = bgWidth - 16;
            if (YuanConfig.get(stack, YuanConfig.K_GLASS_ENABLED, true)
                    && liquidGlassRenderer.prepare(graphics, glassW, glassH,
                    YuanConfig.getFloat(stack, YuanConfig.K_GLASS_BLUR, 12))) {
                renderLiquidGlassBackground(graphics, stack, glassX, glassY, glassW, glassH);
            }
        }

        renderRing(graphics, centerX, centerY, 230.0f, 190.0f, 0.9f, -35.0f, 64,
            ModRenderType.rainbow_slime_block, true, true, false);

        int currentY = y + 8;
        boolean isFirst = true, separatorAdded = false;
        int totalLines = components.size();
        for (int i = 0; i < totalLines; i++) {
            Component c = components.get(i);
            String text = c.getString();
            if (text.isEmpty()) continue;
            if (isFirst) {
                drawRainbowText(graphics, font, text, x + (bgWidth - font.width(c)) / 2, currentY, 10.0f, 0.1f);
                currentY += 10;
                isFirst = false;
                continue;
            }
            if (!separatorAdded) {
                renderSeparator(graphics, x + 8, currentY, bgWidth - 16);
                currentY += 6;
                separatorAdded = true;
            }
            drawRainbowText(graphics, font, text, x + 12, currentY, 17.0f, 0.03f);
            currentY += 10;
        }
        poseStack.popPose();
    }

    public static void renderGlassPanel(GuiGraphics graphics, ItemStack stack, int x, int y, int w, int h) {
        if (YuanConfig.get(stack, YuanConfig.K_GLASS_ENABLED, true)
                && liquidGlassRenderer.prepare(graphics, w, h, YuanConfig.getFloat(stack, YuanConfig.K_GLASS_BLUR, 12)))
            renderLiquidGlassBackground(graphics, stack, x, y, w, h);
    }

    private static void renderLiquidGlassBackground(GuiGraphics graphics, ItemStack stack, int x, int y, int w, int h) {
        ShaderInstance shader = ModShaders.getLiquidGlassShader();
        if (shader == null) return;
        RenderSystem.setShader(() -> shader);
        Minecraft mc = Minecraft.getInstance();
        shader.safeGetUniform("time").set(TimeSystem.getShaderTimeValue());
        shader.safeGetUniform("guiSize").set((float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
        shader.safeGetUniform("framebufferSize").set((float)mc.getWindow().getWidth(), (float)mc.getWindow().getHeight());
        shader.safeGetUniform("tooltipArea").set((float)x, (float)y, (float)w, (float)h);
        shader.safeGetUniform("cornerRadius").set(YuanConfig.getFloat(stack, YuanConfig.K_GLASS_RADIUS, 12));
        shader.safeGetUniform("tint").set(
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_TINT_R, 0) / 255f,
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_TINT_G, 0) / 255f,
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_TINT_B, 0) / 255f,
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_TINT_ALPHA, 0) / 100f);
        shader.safeGetUniform("shadow").set(
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_SHADOW_EXPAND, 30),
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_SHADOW_FACTOR, 25) / 100f,
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_SHADOW_X, 0),
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_SHADOW_Y, 2));
        shader.safeGetUniform("shadowColor").set(
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_SHADOW_R, 0) / 255f,
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_SHADOW_G, 0) / 255f,
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_SHADOW_B, 0) / 255f,
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_SHADOW_ALPHA, 100) / 100f);
        shader.safeGetUniform("optics0").set(
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_REF_THICKNESS, 20),
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_REF_FACTOR, 1.4f),
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_DISPERSION, 7),
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_FRESNEL_RANGE, 30));
        shader.safeGetUniform("optics1").set(
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_FRESNEL_HARDNESS, 20),
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_FRESNEL_FACTOR, 20),
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_GLARE_RANGE, 30),
                YuanConfig.getFloat(stack, YuanConfig.K_GLASS_GLARE_HARDNESS, 20));
        shader.setSampler("SceneTex", liquidGlassRenderer.getSceneTextureId());
        shader.setSampler("BlurTex", liquidGlassRenderer.getBlurTextureId());

        VertexConsumer c = graphics.bufferSource().getBuffer(ModRenderType.liquidGlass);
        Matrix4f m = graphics.pose().last().pose();
        int pad = 24;
        c.vertex(m, x-pad, y+h+pad, 0).color(255,255,255,255).endVertex();
        c.vertex(m, x+w+pad, y+h+pad, 0).color(255,255,255,255).endVertex();
        c.vertex(m, x+w+pad, y-pad, 0).color(255,255,255,255).endVertex();
        c.vertex(m, x-pad, y-pad, 0).color(255,255,255,255).endVertex();
        graphics.bufferSource().endBatch(ModRenderType.liquidGlass);
    }

    private static void renderRing(GuiGraphics graphics, float centerX, float centerY,
            float outerRadius, float innerRadius, float tiltAngleX, float rotationZ,
            int segments, RenderType renderType, boolean filterHalf,
            boolean isFrontHalf, boolean flatDepth) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        ShaderInstance shader = ModShaders.getRainbowSlimeShader();
        if (shader == null) { poseStack.popPose(); return; }
        RenderSystem.setShader(() -> shader);
        shader.safeGetUniform("time").set(TimeSystem.getShaderTimeValue() * 25.0f);
        shader.safeGetUniform("screenSize").set(ModShaders.getScreenSize().x, ModShaders.getScreenSize().y);
        VertexConsumer consumer = graphics.bufferSource().getBuffer(renderType);
        poseStack.translate(centerX, centerY, 0.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationZ));
        poseStack.translate(-centerX, -centerY, 0.0f);
        Matrix4f pm = poseStack.last().pose();
        Matrix3f nm = poseStack.last().normal();
        float tiltScaleX = 1.0f - tiltAngleX * 0.9f;
        for (int i = 0; i < segments; i++) {
            float a1 = (float)(Math.PI * 2 * i / segments);
            float a2 = (float)(Math.PI * 2 * (i + 1) / segments);
            float x1o = centerX + (float)Math.cos(a1) * outerRadius;
            float y1o = centerY + (float)Math.sin(a1) * outerRadius * tiltScaleX;
            float x2o = centerX + (float)Math.cos(a2) * outerRadius;
            float y2o = centerY + (float)Math.sin(a2) * outerRadius * tiltScaleX;
            float x1i = centerX + (float)Math.cos(a1) * innerRadius;
            float y1i = centerY + (float)Math.sin(a1) * innerRadius * tiltScaleX;
            float x2i = centerX + (float)Math.cos(a2) * innerRadius;
            float y2i = centerY + (float)Math.sin(a2) * innerRadius * tiltScaleX;
            float z1 = ((y1o + y1i) / 2 - centerY) / (outerRadius * tiltScaleX) * 2.5f;
            float z2 = ((y2o + y2i) / 2 - centerY) / (outerRadius * tiltScaleX) * 2.5f;
            if (flatDepth) z1 = z2 = 0.0f;
            if (filterHalf && (isFrontHalf && (z1 + z2) / 2 <= 0.0f
                    || !isFrontHalf && (z1 + z2) / 2 > 0.0f)) continue;
            consumer.vertex(pm, x1o, y1o, z1).color(255,255,255,255).uv(0,0).overlayCoords(655360).uv2(15728880).normal(nm,0,0,1).endVertex();
            consumer.vertex(pm, x1i, y1i, z1).color(255,255,255,255).uv(0,1).overlayCoords(655360).uv2(15728880).normal(nm,0,0,1).endVertex();
            consumer.vertex(pm, x2i, y2i, z2).color(255,255,255,255).uv(1,1).overlayCoords(655360).uv2(15728880).normal(nm,0,0,1).endVertex();
            consumer.vertex(pm, x1o, y1o, z1).color(255,255,255,255).uv(0,0).overlayCoords(655360).uv2(15728880).normal(nm,0,0,1).endVertex();
            consumer.vertex(pm, x2i, y2i, z2).color(255,255,255,255).uv(1,1).overlayCoords(655360).uv2(15728880).normal(nm,0,0,1).endVertex();
            consumer.vertex(pm, x2o, y2o, z2).color(255,255,255,255).uv(1,0).overlayCoords(655360).uv2(15728880).normal(nm,0,0,1).endVertex();
        }
        graphics.bufferSource().endBatch(renderType);
        poseStack.popPose();
    }

    private static void renderFoxBackground(GuiGraphics graphics, int x, int y) {
        RenderSystem.setShaderTexture(0, FOX);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        long time = System.currentTimeMillis();
        float baseAngle = (float)(time % 6000L) / 6000.0f * 360.0f;
        float orbitRadius = 20.0f;
        int w = (int)BG_WIDTH, h = (int)BG_HEIGHT;
        for (int i = 0; i < 3; i++) {
            float angle = baseAngle + i * 120.0f;
            float ox = (float)Math.cos(Math.toRadians(angle)) * orbitRadius;
            float oy = (float)Math.sin(Math.toRadians(angle)) * orbitRadius;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.3f);
            graphics.blit(FOX, x + (int)ox, y + (int)oy, 0.0f, 0.0f, w, h, w, h);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(FOX, x, y, 0.0f, 0.0f, w, h, w, h);
        RenderSystem.disableBlend();
    }

    private static void renderSeparator(GuiGraphics graphics, int x, int y, int width) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f m = graphics.pose().last().pose();
        int cx = x + width / 2;
        float hue = ((System.currentTimeMillis() * 2.0f / 1000.0f) % 360) / 360.0f;
        int rgb = hslToRgb(hue, 0.9f, 0.5f);
        int r = (rgb>>16)&0xFF, g = (rgb>>8)&0xFF, bl = rgb&0xFF;
        b.vertex(m, x, y, 0).color(r,g,bl,120).endVertex();
        b.vertex(m, cx, y, 0).color(r,g,bl,30).endVertex();
        b.vertex(m, cx, y+2, 0).color(r,g,bl,30).endVertex();
        b.vertex(m, x, y+2, 0).color(r,g,bl,120).endVertex();
        b.vertex(m, cx, y, 0).color(r,g,bl,30).endVertex();
        b.vertex(m, x+width, y, 0).color(r,g,bl,120).endVertex();
        b.vertex(m, x+width, y+2, 0).color(r,g,bl,120).endVertex();
        b.vertex(m, cx, y+2, 0).color(r,g,bl,30).endVertex();
        t.end();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void drawRainbowText(GuiGraphics g, Font f, String t, int x, int y, float sp, float cs) {
        long time = System.currentTimeMillis(); int cx = 0;
        for (int i = 0; i < t.length(); i++) {
            float hue = ((time*sp/1000.0f)+i*cs)%1.0f;
            g.drawString(f, String.valueOf(t.charAt(i)), x+cx, y, hslToRgb(hue,0.8f,0.6f));
            cx += f.width(String.valueOf(t.charAt(i)));
        }
    }

    private static int hslToRgb(float h, float s, float l) {
        float r, g, b;
        if (s==0) { r=g=b=l; }
        else {
            float q = l<0.5f ? l*(1+s) : l+s-l*s;
            float p = 2*l-q;
            r = hueToRgb(p,q,h+1.0f/3.0f);
            g = hueToRgb(p,q,h);
            b = hueToRgb(p,q,h-1.0f/3.0f);
        }
        return 0xFF000000|((int)(r*255)<<16)|((int)(g*255)<<8)|(int)(b*255);
    }
    private static float hueToRgb(float p, float q, float t) {
        if (t<0) t+=1; if (t>1) t-=1;
        if (t<1.0f/6.0f) return p+(q-p)*6*t;
        if (t<1.0f/2.0f) return q;
        if (t<2.0f/3.0f) return p+(q-p)*(2.0f/3.0f-t)*6;
        return p;
    }

    private static void blitNineSliceScaled(GuiGraphics graphics, int x, int y, int w, int h, int corner, int texW, int texH) {
        int cX = Math.min(corner, w / 2);
        int cY = Math.min(corner, h / 2);
        int midW = Math.max(0, w - cX * 2);
        int midH = Math.max(0, h - cY * 2);
        int srcMidW = Math.max(0, texW - corner * 2);
        int srcMidH = Math.max(0, texH - corner * 2);
        Matrix4f matrix4f = graphics.pose().last().pose();
        blitPartScaled(graphics, matrix4f, x, y, cX, cY, 0, 0, corner, corner, texW, texH);
        blitPartScaled(graphics, matrix4f, x + w - cX, y, cX, cY, texW - corner, 0, corner, corner, texW, texH);
        blitPartScaled(graphics, matrix4f, x, y + h - cY, cX, cY, 0, texH - corner, corner, corner, texW, texH);
        blitPartScaled(graphics, matrix4f, x + w - cX, y + h - cY, cX, cY, texW - corner, texH - corner, corner, corner, texW, texH);
        if (midW > 0) {
            blitPartScaled(graphics, matrix4f, x + cX, y, midW, cY, corner, 0, srcMidW, corner, texW, texH);
            blitPartScaled(graphics, matrix4f, x + cX, y + h - cY, midW, cY, corner, texH - corner, srcMidW, corner, texW, texH);
        }
        if (midH > 0) {
            blitPartScaled(graphics, matrix4f, x, y + cY, cX, midH, 0, corner, corner, srcMidH, texW, texH);
            blitPartScaled(graphics, matrix4f, x + w - cX, y + cY, cX, midH, texW - corner, corner, corner, srcMidH, texW, texH);
        }
        if (midW > 0 && midH > 0) {
            blitPartScaled(graphics, matrix4f, x + cX, y + cY, midW, midH, corner, corner, srcMidW, srcMidH, texW, texH);
        }
    }

    private static void blitPartScaled(GuiGraphics graphics, Matrix4f matrix4f, int x, int y, int w, int h, int u, int v, int uw, int vh, int texW, int texH) {
        if (w <= 0 || h <= 0 || uw <= 0 || vh <= 0) return;
        float u0 = u / (float) texW;
        float v0 = v / (float) texH;
        float u1 = (u + uw) / (float) texW;
        float v1 = (v + vh) / (float) texH;
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix4f, x, y + h, 0).uv(u0, v1).endVertex();
        builder.vertex(matrix4f, x + w, y + h, 0).uv(u1, v1).endVertex();
        builder.vertex(matrix4f, x + w, y, 0).uv(u1, v0).endVertex();
        builder.vertex(matrix4f, x, y, 0).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }

    private static void renderAnimatedNoiseLayer(GuiGraphics graphics, int bgX, int bgY, int bgW, int bgH, int decor1X, int decor1Y, int decor2X, int decor2Y) {
        ensureNoiseDataLoaded();
        if (baseMask == null) return;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        if (NOISE_ADDITIVE) {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float time = (System.currentTimeMillis() % NOISE_PERIOD_MS) / (float) NOISE_PERIOD_MS;
        ensureNineSliceMaps(bgW, bgH);
        if (suMapX == null || svMapY == null) return;
        final float invW = 1.0f / (float) bgW;
        final float invH = 1.0f / (float) bgH;
        for (int py = 0; py < bgH; py++) {
            int sv = svMapY[py];
            int rowBase = sv * TEX_W;
            float v = py * invH;
            for (int px = 0; px < bgW; px++) {
                int su = suMapX[px];
                int idx = rowBase + su;
                if (!baseMask[idx]) continue;
                float u = px * invW;
                float n = legacyHash(u * NOISE_UV_SCALE + time * NOISE_FLOW_SPEED, v * NOISE_UV_SCALE - time * NOISE_FLOW_SPEED);
                if (n < 1.0f - NOISE_DENSITY) continue;
                float luma = (baseLuma != null) ? baseLuma[idx] : 0.5f;
                float edge = (baseEdge != null) ? baseEdge[idx] : 1.0f;
                float alpha = ALPHA_BASE * (ALPHA_SHADOW_BOOST - luma);
                alpha *= edge * edge;
                if (alpha < ALPHA_MIN) continue;
                float hue = time + u * 0.30f + v * 0.10f;
                if (hue >= 1.0f) hue -= 1.0f;
                float sat = Mth.clamp(SAT_BASE + SAT_SHADOW_GAIN * (1.0f - luma), 0f, 1f);
                float val = Mth.clamp(VAL_BASE - VAL_LUMA_COMP * (luma - 0.5f), 0f, 1f);
                int rgb = Mth.hsvToRgb(hue, sat, val);
                float r = ((rgb >> 16) & 255) / 255f;
                float g = ((rgb >> 8) & 255) / 255f;
                float b = (rgb & 255) / 255f;
                putPixelQuadSized(builder, matrix, bgX + px, bgY + py, 1, r, g, b, alpha);
            }
        }
        appendNoiseForScaledTexture(builder, matrix, decor1X, decor1Y, DECOR_W, DECOR_H, decor1Mask, decor1Luma, decor1Edge, decor1W, decor1H, time, 1);
        appendNoiseForScaledTexture(builder, matrix, decor2X, decor2Y, DECOR_W_2, DECOR_H_2, decor2Mask, decor2Luma, decor2Edge, decor2W, decor2H, time, 1);
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static void ensureNoiseDataLoaded() {
        if (noiseDataLoaded) return;
        baseMask = loadAlphaMask(TRUE_DEMON_BOW_TEXTURE, 0, 0, TEX_W, TEX_H);
        baseLuma = loadLuma(TRUE_DEMON_BOW_TEXTURE, 0, 0, TEX_W, TEX_H);
        baseEdge = (baseMask != null) ? buildEdgeWeights(baseMask, TEX_W, TEX_H) : null;
        decor1Mask = loadAlphaMask(EXTRA_DECORATION, 0, 0, DECOR_W, DECOR_H);
        decor1Luma = loadLuma(EXTRA_DECORATION, 0, 0, DECOR_W, DECOR_H);
        decor1Edge = (decor1Mask != null) ? buildEdgeWeights(decor1Mask, DECOR_W, DECOR_H) : null;
        decor1W = DECOR_W; decor1H = DECOR_H;
        decor2Mask = loadAlphaMask(EXTRA_DECORATION_2, 0, 0, DECOR_W_2, DECOR_H_2);
        decor2Luma = loadLuma(EXTRA_DECORATION_2, 0, 0, DECOR_W_2, DECOR_H_2);
        decor2Edge = (decor2Mask != null) ? buildEdgeWeights(decor2Mask, DECOR_W_2, DECOR_H_2) : null;
        decor2W = DECOR_W_2; decor2H = DECOR_H_2;
        noiseDataLoaded = true;
    }

    private static boolean[] loadAlphaMask(ResourceLocation tex, int u, int v, int w, int h) {
        try {
            var mc = Minecraft.getInstance();
            var opt = mc.getResourceManager().getResource(tex);
            if (opt.isEmpty()) return null;
            try (var in = opt.get().open(); var img = NativeImage.read(in)) {
                int texW = img.getWidth();
                int texH = img.getHeight();
                int rw = Math.min(w, Math.max(0, texW - u));
                int rh = Math.min(h, Math.max(0, texH - v));
                boolean[] mask = new boolean[w * h];
                for (int yy = 0; yy < rh; yy++)
                    for (int xx = 0; xx < rw; xx++)
                        mask[yy * w + xx] = ((img.getPixelRGBA(u + xx, v + yy) >>> 24) & 0xFF) > 8;
                return mask;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static float[] loadLuma(ResourceLocation tex, int u, int v, int w, int h) {
        try {
            var mc = Minecraft.getInstance();
            var opt = mc.getResourceManager().getResource(tex);
            if (opt.isEmpty()) return null;
            try (var in = opt.get().open(); var img = NativeImage.read(in)) {
                int texW = img.getWidth();
                int texH = img.getHeight();
                int rw = Math.min(w, Math.max(0, texW - u));
                int rh = Math.min(h, Math.max(0, texH - v));
                float[] out = new float[w * h];
                for (int yy = 0; yy < rh; yy++) {
                    for (int xx = 0; xx < rw; xx++) {
                        int packed = img.getPixelRGBA(u + xx, v + yy);
                        float b = ((packed >> 16) & 0xFF) / 255f;
                        float g = ((packed >> 8) & 0xFF) / 255f;
                        float r = (packed & 0xFF) / 255f;
                        out[yy * w + xx] = 0.2126f * r + 0.7152f * g + 0.0722f * b;
                    }
                }
                return out;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static float[] buildEdgeWeights(boolean[] mask, int w, int h) {
        float[] ew = new float[w * h];
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int i = y * w + x;
                if (!mask[i]) { ew[i] = 0f; continue; }
                int neighbors = 0;
                for (int oy = -1; oy <= 1; oy++)
                    for (int ox = -1; ox <= 1; ox++)
                        if (mask[(y + oy) * w + (x + ox)]) neighbors++;
                ew[i] = Math.max(0f, (neighbors - 5) / 4f);
            }
        }
        return ew;
    }

    private static void ensureNineSliceMaps(int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (w == cachedMapW && h == cachedMapH && suMapX != null && svMapY != null) return;
        cachedMapW = w; cachedMapH = h;
        suMapX = new int[w]; svMapY = new int[h];
        int srcMidW = Math.max(1, TEX_W - CORNER * 2);
        int srcMidH = Math.max(1, TEX_H - CORNER * 2);
        for (int px = 0; px < w; px++) {
            int su;
            if (px < CORNER) su = px;
            else if (px >= w - CORNER) su = TEX_W - (w - px);
            else su = CORNER + ((px - CORNER) % srcMidW);
            suMapX[px] = Math.max(0, su);
        }
        for (int py = 0; py < h; py++) {
            int sv;
            if (py < CORNER) sv = py;
            else if (py >= h - CORNER) sv = TEX_H - (h - py);
            else sv = CORNER + ((py - CORNER) % srcMidH);
            svMapY[py] = Math.max(0, sv);
        }
    }

    private static float legacyHash(float u, float v) {
        int x = Float.floatToIntBits(u);
        int y = Float.floatToIntBits(v);
        int n = x * 374761393 + y * 668265263;
        n = (n ^ (n >>> 13)) * 1274126177;
        return ((n ^ (n >>> 16)) & 0x7fffffff) / (float) 0x7fffffff;
    }

    private static void appendNoiseForScaledTexture(BufferBuilder buf, Matrix4f matrix4f, int x, int y, int drawW, int drawH, boolean[] mask, float[] lumaArr, float[] edgeArr, int texW, int texH, float time, int step) {
        if (mask == null || texW <= 0 || texH <= 0 || drawW <= 0 || drawH <= 0) return;
        final float invW = 1.0f / (float) drawW;
        final float invH = 1.0f / (float) drawH;
        for (int py = 0; py < drawH; py += step) {
            float v = py * invH;
            int sv = Mth.clamp((int)(v * (float) texH), 0, texH - 1);
            int rowBase = sv * texW;
            for (int px = 0; px < drawW; px += step) {
                float u = px * invW;
                int su = Mth.clamp((int)(u * (float) texW), 0, texW - 1);
                int idx = rowBase + su;
                if (!mask[idx]) continue;
                float n = legacyHash(u * NOISE_UV_SCALE + time * NOISE_FLOW_SPEED, v * NOISE_UV_SCALE - time * NOISE_FLOW_SPEED);
                if (n < 1.0f - NOISE_DENSITY) continue;
                float luma = (lumaArr != null) ? lumaArr[idx] : 0.5f;
                float edge = (edgeArr != null) ? edgeArr[idx] : 1.0f;
                float alpha = ALPHA_BASE * (ALPHA_SHADOW_BOOST - luma);
                alpha *= edge * edge;
                if (alpha < ALPHA_MIN) continue;
                float hue = time + u * 0.30f + v * 0.10f;
                if (hue >= 1.0f) hue -= 1.0f;
                float sat = Mth.clamp(SAT_BASE + SAT_SHADOW_GAIN * (1.0f - luma), 0f, 1f);
                float val = Mth.clamp(VAL_BASE - VAL_LUMA_COMP * (luma - 0.5f), 0f, 1f);
                int rgb = Mth.hsvToRgb(hue, sat, val);
                float r = ((rgb >> 16) & 255) / 255f;
                float g = ((rgb >> 8) & 255) / 255f;
                float b = (rgb & 255) / 255f;
                putPixelQuadSized(buf, matrix4f, x + px, y + py, step, r, g, b, alpha);
            }
        }
    }

    private static void putPixelQuadSized(BufferBuilder builder, Matrix4f matrix4f, float x, float y, int size, float r, float g, float b, float a) {
        float s = (float) size;
        builder.vertex(matrix4f, x, y + s, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix4f, x + s, y + s, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix4f, x + s, y, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix4f, x, y, 0).color(r, g, b, a).endVertex();
    }
}
