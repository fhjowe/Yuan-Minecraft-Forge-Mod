package com.yuan.mixin;

import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class TimerewindCameraMixin {
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0D);

    @Shadow(remap = false) private Vec3 f_90552_;
    @Shadow(remap = false) private BlockPos.MutableBlockPos f_90553_;
    @Shadow(remap = false) private Vector3f f_90554_;
    @Shadow(remap = false) private Vector3f f_90555_;
    @Shadow(remap = false) private Vector3f f_90556_;
    @Shadow(remap = false) private float f_90557_;
    @Shadow(remap = false) private float f_90558_;
    @Shadow(remap = false) private Quaternionf f_90559_;
    @Shadow(remap = false) private boolean f_90549_;
    @Shadow(remap = false) private BlockGetter f_90550_;
    @Shadow(remap = false) private Entity f_90551_;
    @Shadow(remap = false) private boolean f_90560_;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void rewindTick(CallbackInfo ci) {
        if (!YuanTimeRewindClient.isFreeCamera()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        applyFreeCamera(mc.level, mc.player, false);
        ci.cancel();
    }

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void rewindSetup(BlockGetter level, Entity entity, boolean detached,
                             boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (!YuanTimeRewindClient.isFreeCamera()) {
            return;
        }
        applyFreeCamera(level, entity, detached);
        ci.cancel();
    }

    private void applyFreeCamera(BlockGetter level, Entity entity, boolean detached) {
        f_90549_ = true;
        this.f_90550_ = level;
        this.f_90551_ = entity;
        this.f_90560_ = detached;
        if (f_90553_ == null) f_90553_ = new BlockPos.MutableBlockPos();
        if (f_90554_ == null) f_90554_ = new Vector3f();
        if (f_90555_ == null) f_90555_ = new Vector3f();
        if (f_90556_ == null) f_90556_ = new Vector3f();
        if (f_90559_ == null) f_90559_ = new Quaternionf();
        Vec3 pos = YuanTimeRewindClient.cameraPos();
        f_90552_ = pos;
        f_90553_.set(pos.x, pos.y, pos.z);
        f_90557_ = YuanTimeRewindClient.cameraPitch();
        f_90558_ = YuanTimeRewindClient.cameraYaw();
        f_90559_.rotationYXZ(-f_90558_ * DEG_TO_RAD, f_90557_ * DEG_TO_RAD, 0.0f);
        f_90554_.set(0.0f, 0.0f, 1.0f).rotate(f_90559_);
        f_90555_.set(0.0f, 1.0f, 0.0f).rotate(f_90559_);
        f_90556_.set(1.0f, 0.0f, 0.0f).rotate(f_90559_);
    }
}
