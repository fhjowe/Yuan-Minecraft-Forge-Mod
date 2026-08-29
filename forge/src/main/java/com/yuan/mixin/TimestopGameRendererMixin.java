package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timestop.YuanTimeStopConfig;
import com.yuan.timestop.YuanTimeStopShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class TimestopGameRendererMixin {
    @ModifyVariable(method = "loadEffect", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private ResourceLocation timestopLoadEffect(ResourceLocation location) {
        return YuanTimeStop.get() && YuanTimeStopConfig.specialShader
                ? YuanTimeStopShaders.effectLocation() : location;
    }

    @Inject(method = "shutdownEffect", at = @At("HEAD"), cancellable = true)
    private void timestopShutdownEffect(CallbackInfo ci) {
        GameRenderer self = (GameRenderer) (Object) this;
        if (YuanTimeStop.get() && YuanTimeStopConfig.specialShader && self.minecraft.level != null
                && self.postEffect != null
                && (self.postEffect.getName().endsWith("the_world.json")
                || self.postEffect.getName().endsWith("yuan_world_style.json"))) {
            ci.cancel();
        }
    }

    @Inject(method = "tickFov", at = @At("HEAD"), cancellable = true)
    private void timestopTickFov(CallbackInfo ci) {
        if (YuanTimeStop.shouldCancelTickFov()) {
            ci.cancel();
        }
    }

    @Inject(method = "checkEntityPostEffect", at = @At("HEAD"), cancellable = true)
    private void timestopCheckEntityPostEffect(Entity entity, CallbackInfo ci) {
        GameRenderer self = (GameRenderer) (Object) this;
        if (YuanTimeStop.get() && YuanTimeStopConfig.specialShader && self.minecraft.level != null
                && self.postEffect != null
                && (self.postEffect.getName().endsWith("the_world.json")
                || self.postEffect.getName().endsWith("yuan_world_style.json"))) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "renderItemInHand", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float timestopPartialHand(float value) {
        return YuanTimeStop.get() && !YuanTimeStop.shouldCancelItemInHandTick()
                ? YuanTimeStop.livePartialTick() : value;
    }

    @ModifyVariable(method = "getFov", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float timestopPartialFov(float value) {
        return YuanTimeStop.get() && !YuanTimeStop.shouldCancelTickFov()
                ? YuanTimeStop.livePartialTick() : value;
    }

    @ModifyVariable(method = "bobHurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float timestopPartialBobHurt(float value) {
        return YuanTimeStop.get() && !YuanTimeStop.shouldCancelTickFov()
                ? YuanTimeStop.livePartialTick() : value;
    }

    @ModifyVariable(method = "bobView", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float timestopPartialBobView(float value) {
        return YuanTimeStop.get() && !YuanTimeStop.shouldCancelTickFov()
                ? YuanTimeStop.livePartialTick() : value;
    }

    @ModifyVariable(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;F)V"),
            ordinal = 0, argsOnly = true)
    private float timestopPartialGui(float value) {
        return value;
    }
}
