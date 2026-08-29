/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferBuilder$RenderedBuffer
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.RenderType
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Vector4i
 */
package com.yuan.client.render;


import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4i;

public class YuanRoundedRectRenderer {
    public static void renderRoundedRect(GuiGraphics graphics, float x, float y, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, Vector4i color, int segments) {
        YuanRoundedRectRenderer.renderRoundedRectGradient(graphics, x, y, width, height, topLeft, topRight, bottomRight, bottomLeft, null, color, segments, RenderType.entityTranslucent(new ResourceLocation("minecraft", "textures/misc/white.png")));
    }

    public static void renderRoundedRectGradient(GuiGraphics graphics, float x, float y, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, YuanGradientConfig gradient, Vector4i fallbackColor, int segments, RenderType renderType) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        VertexConsumer consumer = graphics.bufferSource().getBuffer(renderType);
        float maxRadius = Math.min(width, height) / 2.0f;
        topLeft = Math.min(topLeft, maxRadius);
        topRight = Math.min(topRight, maxRadius);
        bottomRight = Math.min(bottomRight, maxRadius);
        bottomLeft = Math.min(bottomLeft, maxRadius);
        float innerLeft = x + topLeft;
        float innerRight = x + width - topRight;
        float innerTop = y + topLeft;
        float innerBottom = y + height - bottomLeft;
        long time = System.currentTimeMillis();
        float nx = 0.0f;
        float ny = 0.0f;
        float nz = 1.0f;
        YuanRoundedRectRenderer.addQuadGradientWithConsumer(consumer, matrix, normal, innerLeft, innerTop, innerRight, innerBottom, x, y, width, height, gradient, fallbackColor, time, nx, ny, nz);
        YuanRoundedRectRenderer.addQuadGradientWithConsumer(consumer, matrix, normal, x + topLeft, y, x + width - topRight, y + Math.max(topLeft, topRight), x, y, width, height, gradient, fallbackColor, time, nx, ny, nz);
        YuanRoundedRectRenderer.addQuadGradientWithConsumer(consumer, matrix, normal, x + bottomLeft, y + height - Math.max(bottomLeft, bottomRight), x + width - bottomRight, y + height, x, y, width, height, gradient, fallbackColor, time, nx, ny, nz);
        YuanRoundedRectRenderer.addQuadGradientWithConsumer(consumer, matrix, normal, x, y + topLeft, x + Math.max(topLeft, bottomLeft), y + height - bottomLeft, x, y, width, height, gradient, fallbackColor, time, nx, ny, nz);
        YuanRoundedRectRenderer.addQuadGradientWithConsumer(consumer, matrix, normal, x + width - Math.max(topRight, bottomRight), y + topRight, x + width, y + height - bottomRight, x, y, width, height, gradient, fallbackColor, time, nx, ny, nz);
        if (topLeft > 0.0f) {
            YuanRoundedRectRenderer.addRoundedCornerGradientWithConsumer(consumer, matrix, normal, x + topLeft, y + topLeft, topLeft, 0.0f, 180.0f, 270.0f, segments, x, y, width, height, gradient, fallbackColor, time, nx, ny, nz);
        }
        if (topRight > 0.0f) {
            YuanRoundedRectRenderer.addRoundedCornerGradientWithConsumer(consumer, matrix, normal, x + width - topRight, y + topRight, topRight, 0.0f, 270.0f, 360.0f, segments, x, y, width, height, gradient, fallbackColor, time, nx, ny, nz);
        }
        if (bottomRight > 0.0f) {
            YuanRoundedRectRenderer.addRoundedCornerGradientWithConsumer(consumer, matrix, normal, x + width - bottomRight, y + height - bottomRight, bottomRight, 0.0f, 0.0f, 90.0f, segments, x, y, width, height, gradient, fallbackColor, time, nx, ny, nz);
        }
        if (bottomLeft > 0.0f) {
            YuanRoundedRectRenderer.addRoundedCornerGradientWithConsumer(consumer, matrix, normal, x + bottomLeft, y + height - bottomLeft, bottomLeft, 0.0f, 90.0f, 180.0f, segments, x, y, width, height, gradient, fallbackColor, time, nx, ny, nz);
        }
        graphics.bufferSource().endBatch(renderType);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void addQuadGradientWithConsumer(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x1, float y1, float x2, float y2, float rectX, float rectY, float rectWidth, float rectHeight, YuanGradientConfig gradient, Vector4i fallbackColor, long time, float nx, float ny, float nz) {
        int color1 = YuanRoundedRectRenderer.getColorAtPosition(x1, y1, rectX, rectY, rectWidth, rectHeight, gradient, fallbackColor, time);
        int color2 = YuanRoundedRectRenderer.getColorAtPosition(x1, y2, rectX, rectY, rectWidth, rectHeight, gradient, fallbackColor, time);
        int color3 = YuanRoundedRectRenderer.getColorAtPosition(x2, y2, rectX, rectY, rectWidth, rectHeight, gradient, fallbackColor, time);
        int color4 = YuanRoundedRectRenderer.getColorAtPosition(x2, y1, rectX, rectY, rectWidth, rectHeight, gradient, fallbackColor, time);
        YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x1, y1, 0.0f, color1, nx, ny, nz);
        YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x1, y2, 0.0f, color2, nx, ny, nz);
        YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x2, y2, 0.0f, color3, nx, ny, nz);
        YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x1, y1, 0.0f, color1, nx, ny, nz);
        YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x2, y2, 0.0f, color3, nx, ny, nz);
        YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x2, y1, 0.0f, color4, nx, ny, nz);
    }

    private static void addRoundedCornerGradientWithConsumer(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float centerX, float centerY, float outerRadius, float innerRadius, float startAngle, float endAngle, int segments, float rectX, float rectY, float rectWidth, float rectHeight, YuanGradientConfig gradient, Vector4i fallbackColor, long time, float nx, float ny, float nz) {
        float angleStep = (endAngle - startAngle) / (float)segments;
        for (int i = 0; i < segments; ++i) {
            float angle1 = (float)Math.toRadians(startAngle + angleStep * (float)i);
            float angle2 = (float)Math.toRadians(startAngle + angleStep * (float)(i + 1));
            float x1Outer = centerX + (float)Math.cos(angle1) * outerRadius;
            float y1Outer = centerY + (float)Math.sin(angle1) * outerRadius;
            float x2Outer = centerX + (float)Math.cos(angle2) * outerRadius;
            float y2Outer = centerY + (float)Math.sin(angle2) * outerRadius;
            float x1Inner = centerX + (float)Math.cos(angle1) * innerRadius;
            float y1Inner = centerY + (float)Math.sin(angle1) * innerRadius;
            float x2Inner = centerX + (float)Math.cos(angle2) * innerRadius;
            float y2Inner = centerY + (float)Math.sin(angle2) * innerRadius;
            int color1Outer = YuanRoundedRectRenderer.getColorAtPosition(x1Outer, y1Outer, rectX, rectY, rectWidth, rectHeight, gradient, fallbackColor, time);
            int color1Inner = YuanRoundedRectRenderer.getColorAtPosition(x1Inner, y1Inner, rectX, rectY, rectWidth, rectHeight, gradient, fallbackColor, time);
            int color2Inner = YuanRoundedRectRenderer.getColorAtPosition(x2Inner, y2Inner, rectX, rectY, rectWidth, rectHeight, gradient, fallbackColor, time);
            int color2Outer = YuanRoundedRectRenderer.getColorAtPosition(x2Outer, y2Outer, rectX, rectY, rectWidth, rectHeight, gradient, fallbackColor, time);
            YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x1Outer, y1Outer, 0.0f, color1Outer, nx, ny, nz);
            YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x1Inner, y1Inner, 0.0f, color1Inner, nx, ny, nz);
            YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x2Inner, y2Inner, 0.0f, color2Inner, nx, ny, nz);
            YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x1Outer, y1Outer, 0.0f, color1Outer, nx, ny, nz);
            YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x2Inner, y2Inner, 0.0f, color2Inner, nx, ny, nz);
            YuanRoundedRectRenderer.addColoredVertexWithNormal(consumer, matrix, normal, x2Outer, y2Outer, 0.0f, color2Outer, nx, ny, nz);
        }
    }

    private static void addColoredVertexWithNormal(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z, int color, float nx, float ny, float nz) {
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        consumer.vertex(matrix, x, y, z).color(r, g, b, a).uv(0, 0).overlayCoords(655360).uv2(15728880).normal(normal, nx, ny, nz).endVertex();
    }

    public static void renderRoundedRectBorder(GuiGraphics graphics, float x, float y, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, float borderWidth, Vector4i color, int segments) {
        YuanRoundedRectRenderer.renderRoundedRectBorderGradient(graphics, x, y, width, height, topLeft, topRight, bottomRight, bottomLeft, borderWidth, null, color, segments);
    }

    public static void renderRoundedRectBorderGradient(GuiGraphics graphics, float x, float y, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, float borderWidth, YuanGradientConfig gradient, Vector4i fallbackColor, int segments) {
        PoseStack poseStack = graphics.pose();
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        float maxRadius = Math.min(width, height) / 2.0f;
        topLeft = Math.min(topLeft, maxRadius);
        topRight = Math.min(topRight, maxRadius);
        bottomRight = Math.min(bottomRight, maxRadius);
        bottomLeft = Math.min(bottomLeft, maxRadius);
        long time = System.currentTimeMillis();
        float perimeter = YuanRoundedRectRenderer.calculatePerimeter(width, height, topLeft, topRight, bottomRight, bottomLeft);
        float topLength = width - topLeft - topRight;
        int topSegments = Math.max(2, (int)(topLength / 5.0f));
        float topStart = 0.0f;
        YuanRoundedRectRenderer.addTopBorderSegmented(builder, matrix, x + topLeft, y, x + width - topRight, y, borderWidth, topSegments, topStart, topLength, perimeter, gradient, fallbackColor, time);
        float rightLength = height - topRight - bottomRight;
        int rightSegments = Math.max(2, (int)(rightLength / 5.0f));
        float rightStart = (float)((double)topLength + Math.PI * (double)topRight / 2.0);
        YuanRoundedRectRenderer.addRightBorderSegmented(builder, matrix, x + width, y + topRight, x + width, y + height - bottomRight, borderWidth, rightSegments, rightStart, rightLength, perimeter, gradient, fallbackColor, time);
        float bottomLength = width - bottomLeft - bottomRight;
        int bottomSegments = Math.max(2, (int)(bottomLength / 5.0f));
        float bottomStart = (float)((double)topLength + Math.PI * (double)topRight / 2.0 + (double)rightLength + Math.PI * (double)bottomRight / 2.0);
        YuanRoundedRectRenderer.addBottomBorderSegmented(builder, matrix, x + width - bottomRight, y + height, x + bottomLeft, y + height, borderWidth, bottomSegments, bottomStart, bottomLength, perimeter, gradient, fallbackColor, time);
        float leftLength = height - topLeft - bottomLeft;
        int leftSegments = Math.max(2, (int)(leftLength / 5.0f));
        float leftStart = (float)((double)topLength + Math.PI * (double)topRight / 2.0 + (double)rightLength + Math.PI * (double)bottomRight / 2.0 + (double)bottomLength + Math.PI * (double)bottomLeft / 2.0);
        YuanRoundedRectRenderer.addLeftBorderSegmented(builder, matrix, x, y + height - bottomLeft, x, y + topLeft, borderWidth, leftSegments, leftStart, leftLength, perimeter, gradient, fallbackColor, time);
        if (topLeft > 0.0f) {
            YuanRoundedRectRenderer.addRoundedCornerBorderCircular(builder, matrix, x + topLeft, y + topLeft, topLeft, topLeft - borderWidth, 180.0f, 270.0f, segments, (float)((double)topLength + Math.PI * (double)topRight / 2.0 + (double)rightLength + Math.PI * (double)bottomRight / 2.0 + (double)bottomLength + Math.PI * (double)bottomLeft / 2.0 + (double)leftLength), (float)(Math.PI * (double)topLeft / 2.0), perimeter, gradient, fallbackColor, time);
        }
        if (topRight > 0.0f) {
            YuanRoundedRectRenderer.addRoundedCornerBorderCircular(builder, matrix, x + width - topRight, y + topRight, topRight, topRight - borderWidth, 270.0f, 360.0f, segments, topLength, (float)(Math.PI * (double)topRight / 2.0), perimeter, gradient, fallbackColor, time);
        }
        if (bottomRight > 0.0f) {
            YuanRoundedRectRenderer.addRoundedCornerBorderCircular(builder, matrix, x + width - bottomRight, y + height - bottomRight, bottomRight, bottomRight - borderWidth, 0.0f, 90.0f, segments, (float)((double)topLength + Math.PI * (double)topRight / 2.0 + (double)rightLength), (float)(Math.PI * (double)bottomRight / 2.0), perimeter, gradient, fallbackColor, time);
        }
        if (bottomLeft > 0.0f) {
            YuanRoundedRectRenderer.addRoundedCornerBorderCircular(builder, matrix, x + bottomLeft, y + height - bottomLeft, bottomLeft, bottomLeft - borderWidth, 90.0f, 180.0f, segments, (float)((double)topLength + Math.PI * (double)topRight / 2.0 + (double)rightLength + Math.PI * (double)bottomRight / 2.0 + (double)bottomLength), (float)(Math.PI * (double)bottomLeft / 2.0), perimeter, gradient, fallbackColor, time);
        }
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.disableBlend();
    }

    private static float calculatePerimeter(float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft) {
        float straightEdges = width - topLeft - topRight + (height - topRight - bottomRight) + (width - bottomRight - bottomLeft) + (height - bottomLeft - topLeft);
        float corners = (float)(Math.PI * (double)(topLeft + topRight + bottomRight + bottomLeft) / 2.0);
        return straightEdges + corners;
    }

    private static void addTopBorderSegmented(BufferBuilder builder, Matrix4f matrix, float x1, float y1, float x2, float y2, float borderWidth, int segments, float startDistance, float edgeLength, float totalPerimeter, YuanGradientConfig gradient, Vector4i fallbackColor, long time) {
        for (int i = 0; i < segments; ++i) {
            float t1 = (float)i / (float)segments;
            float t2 = (float)(i + 1) / (float)segments;
            float px1 = x1 + (x2 - x1) * t1;
            float px2 = x1 + (x2 - x1) * t2;
            float dist1 = startDistance + edgeLength * t1;
            float dist2 = startDistance + edgeLength * t2;
            float progress1 = dist1 / totalPerimeter;
            float progress2 = dist2 / totalPerimeter;
            int color1 = YuanRoundedRectRenderer.getBorderColorAtProgress(progress1, gradient, fallbackColor, time);
            int color2 = YuanRoundedRectRenderer.getBorderColorAtProgress(progress2, gradient, fallbackColor, time);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px1, y1, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px1, y1 + borderWidth, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px2, y2 + borderWidth, color2);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px1, y1, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px2, y2 + borderWidth, color2);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px2, y2, color2);
        }
    }

    private static void addRightBorderSegmented(BufferBuilder builder, Matrix4f matrix, float x1, float y1, float x2, float y2, float borderWidth, int segments, float startDistance, float edgeLength, float totalPerimeter, YuanGradientConfig gradient, Vector4i fallbackColor, long time) {
        for (int i = 0; i < segments; ++i) {
            float t1 = (float)i / (float)segments;
            float t2 = (float)(i + 1) / (float)segments;
            float py1 = y1 + (y2 - y1) * t1;
            float py2 = y1 + (y2 - y1) * t2;
            float dist1 = startDistance + edgeLength * t1;
            float dist2 = startDistance + edgeLength * t2;
            float progress1 = dist1 / totalPerimeter;
            float progress2 = dist2 / totalPerimeter;
            int color1 = YuanRoundedRectRenderer.getBorderColorAtProgress(progress1, gradient, fallbackColor, time);
            int color2 = YuanRoundedRectRenderer.getBorderColorAtProgress(progress2, gradient, fallbackColor, time);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x1, py1, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x1 - borderWidth, py1, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x2 - borderWidth, py2, color2);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x1, py1, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x2 - borderWidth, py2, color2);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x2, py2, color2);
        }
    }

    private static void addBottomBorderSegmented(BufferBuilder builder, Matrix4f matrix, float x1, float y1, float x2, float y2, float borderWidth, int segments, float startDistance, float edgeLength, float totalPerimeter, YuanGradientConfig gradient, Vector4i fallbackColor, long time) {
        for (int i = 0; i < segments; ++i) {
            float t1 = (float)i / (float)segments;
            float t2 = (float)(i + 1) / (float)segments;
            float px1 = x1 + (x2 - x1) * t1;
            float px2 = x1 + (x2 - x1) * t2;
            float dist1 = startDistance + edgeLength * t1;
            float dist2 = startDistance + edgeLength * t2;
            float progress1 = dist1 / totalPerimeter;
            float progress2 = dist2 / totalPerimeter;
            int color1 = YuanRoundedRectRenderer.getBorderColorAtProgress(progress1, gradient, fallbackColor, time);
            int color2 = YuanRoundedRectRenderer.getBorderColorAtProgress(progress2, gradient, fallbackColor, time);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px1, y1, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px1, y1 - borderWidth, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px2, y2 - borderWidth, color2);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px1, y1, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px2, y2 - borderWidth, color2);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, px2, y2, color2);
        }
    }

    private static void addLeftBorderSegmented(BufferBuilder builder, Matrix4f matrix, float x1, float y1, float x2, float y2, float borderWidth, int segments, float startDistance, float edgeLength, float totalPerimeter, YuanGradientConfig gradient, Vector4i fallbackColor, long time) {
        for (int i = 0; i < segments; ++i) {
            float t1 = (float)i / (float)segments;
            float t2 = (float)(i + 1) / (float)segments;
            float py1 = y1 + (y2 - y1) * t1;
            float py2 = y1 + (y2 - y1) * t2;
            float dist1 = startDistance + edgeLength * t1;
            float dist2 = startDistance + edgeLength * t2;
            float progress1 = dist1 / totalPerimeter;
            float progress2 = dist2 / totalPerimeter;
            int color1 = YuanRoundedRectRenderer.getBorderColorAtProgress(progress1, gradient, fallbackColor, time);
            int color2 = YuanRoundedRectRenderer.getBorderColorAtProgress(progress2, gradient, fallbackColor, time);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x1, py1, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x1 + borderWidth, py1, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x2 + borderWidth, py2, color2);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x1, py1, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x2 + borderWidth, py2, color2);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x2, py2, color2);
        }
    }

    private static void addRoundedCornerBorderCircular(BufferBuilder builder, Matrix4f matrix, float centerX, float centerY, float outerRadius, float innerRadius, float startAngle, float endAngle, int segments, float startDistance, float arcLength, float totalPerimeter, YuanGradientConfig gradient, Vector4i fallbackColor, long time) {
        float angleStep = (endAngle - startAngle) / (float)segments;
        for (int i = 0; i < segments; ++i) {
            float angle1 = (float)Math.toRadians(startAngle + angleStep * (float)i);
            float angle2 = (float)Math.toRadians(startAngle + angleStep * (float)(i + 1));
            float x1Outer = centerX + (float)Math.cos(angle1) * outerRadius;
            float y1Outer = centerY + (float)Math.sin(angle1) * outerRadius;
            float x2Outer = centerX + (float)Math.cos(angle2) * outerRadius;
            float y2Outer = centerY + (float)Math.sin(angle2) * outerRadius;
            float x1Inner = centerX + (float)Math.cos(angle1) * innerRadius;
            float y1Inner = centerY + (float)Math.sin(angle1) * innerRadius;
            float x2Inner = centerX + (float)Math.cos(angle2) * innerRadius;
            float y2Inner = centerY + (float)Math.sin(angle2) * innerRadius;
            float t1 = (float)i / (float)segments;
            float t2 = (float)(i + 1) / (float)segments;
            float dist1 = startDistance + arcLength * t1;
            float dist2 = startDistance + arcLength * t2;
            float progress1 = dist1 / totalPerimeter;
            float progress2 = dist2 / totalPerimeter;
            int color1 = YuanRoundedRectRenderer.getBorderColorAtProgress(progress1, gradient, fallbackColor, time);
            int color2 = YuanRoundedRectRenderer.getBorderColorAtProgress(progress2, gradient, fallbackColor, time);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x1Outer, y1Outer, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x1Inner, y1Inner, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x2Inner, y2Inner, color2);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x1Outer, y1Outer, color1);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x2Inner, y2Inner, color2);
            YuanRoundedRectRenderer.addColoredVertex(builder, matrix, x2Outer, y2Outer, color2);
        }
    }

    private static int getColorAtPosition(float px, float py, float rectX, float rectY, float rectWidth, float rectHeight, YuanGradientConfig gradient, Vector4i fallbackColor, long time) {
        if (gradient == null) {
            return fallbackColor.w << 24 | fallbackColor.x << 16 | fallbackColor.y << 8 | fallbackColor.z;
        }
        float progress = 0.0f;
        switch (gradient.getType()) {
            case HORIZONTAL: 
            case ANIMATED: {
                progress = (px - rectX) / rectWidth;
                break;
            }
            case VERTICAL: {
                progress = (py - rectY) / rectHeight;
                break;
            }
            case RADIAL: {
                float centerX = rectX + rectWidth / 2.0f;
                float centerY = rectY + rectHeight / 2.0f;
                float dx = px - centerX;
                float dy = py - centerY;
                float maxDist = (float)Math.sqrt(rectWidth * rectWidth + rectHeight * rectHeight) / 2.0f;
                progress = (float)Math.sqrt(dx * dx + dy * dy) / maxDist;
            }
        }
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        return gradient.getColorAt(progress, time);
    }

    private static int getBorderColorAtProgress(float progress, YuanGradientConfig gradient, Vector4i fallbackColor, long time) {
        if (gradient == null || gradient.getType() != YuanGradientConfig.GradientType.BORDER_CIRCULAR) {
            return fallbackColor.w << 24 | fallbackColor.x << 16 | fallbackColor.y << 8 | fallbackColor.z;
        }
        return gradient.getColorAt(progress, time);
    }

    private static void addColoredVertex(BufferBuilder builder, Matrix4f matrix, float x, float y, int color) {
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        builder.vertex(matrix, x, y, 0.0f).color(r, g, b, a).endVertex();
    }
}

