package com.yuan.space_slash;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class YuanSpaceSlashEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_SEED =
            SynchedEntityData.defineId(YuanSpaceSlashEntity.class, EntityDataSerializers.INT);

    private YuanSpaceSlashParams params = new YuanSpaceSlashParams();
    private int targetId = -1;
    private float yaw;
    private float pitch;
    private float roll;
    private Vec3 offset = Vec3.ZERO;
    private float baseLength = 1.6f;
    private long startMillis;
    private Vector3f longAxis = new Vector3f(1.0f, 0.0f, 0.0f);
    private Vector3f side = new Vector3f(0.0f, 1.0f, 0.0f);
    private Vector3f thick = new Vector3f(0.0f, 0.0f, 1.0f);
    private boolean orientationReady;

    public YuanSpaceSlashEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.startMillis = System.currentTimeMillis();
    }

    public void initSlash(Entity target, int seed, float yaw, float pitch, float roll,
                          double hitX, double hitY, double hitZ, YuanSpaceSlashParams params) {
        this.params = params;
        this.targetId = target.getId();
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.offset = new Vec3(hitX, hitY, hitZ).subtract(target.position());
        this.baseLength = lengthFor(target);
        this.entityData.set(DATA_SEED, seed);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SEED, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_SEED, tag.getInt("Seed"));
        this.yaw = tag.getFloat("Yaw");
        this.pitch = tag.getFloat("Pitch");
        this.roll = tag.getFloat("Roll");
        this.baseLength = tag.getFloat("BaseLength");
        this.targetId = tag.getInt("TargetId");
        this.offset = new Vec3(tag.getDouble("Ox"), tag.getDouble("Oy"), tag.getDouble("Oz"));
        this.startMillis = tag.getLong("StartMillis");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Seed", this.entityData.get(DATA_SEED));
        tag.putFloat("Yaw", this.yaw);
        tag.putFloat("Pitch", this.pitch);
        tag.putFloat("Roll", this.roll);
        tag.putFloat("BaseLength", this.baseLength);
        tag.putInt("TargetId", this.targetId);
        tag.putDouble("Ox", this.offset.x);
        tag.putDouble("Oy", this.offset.y);
        tag.putDouble("Oz", this.offset.z);
        tag.putLong("StartMillis", this.startMillis);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            this.discard();
            return;
        }
        float lifetimeMs = Math.max(0.05f, params.durationSeconds) * 1000.0f;
        if (System.currentTimeMillis() - startMillis >= lifetimeMs) {
            this.discard();
            return;
        }
        if (targetId >= 0) {
            Entity target = level().getEntity(targetId);
            if (target != null && !target.isRemoved()) {
                setPos(target.position().add(offset));
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }

    public float progress() {
        long elapsed = System.currentTimeMillis() - startMillis;
        float lifetimeMs = Math.max(0.05f, params.durationSeconds) * 1000.0f;
        return (float) Mth.clamp((double) elapsed / lifetimeMs, 0.0D, 1.0D);
    }

    public YuanSpaceSlashParams getParams() {
        return this.params;
    }

    public int getSeed() {
        return this.entityData.get(DATA_SEED);
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getRoll() {
        return this.roll;
    }

    public float getBaseLength() {
        return this.baseLength;
    }

    public Vector3f getLongAxis() {
        return this.longAxis;
    }

    public Vector3f getSide() {
        return this.side;
    }

    public Vector3f getThick() {
        return this.thick;
    }

    public boolean isOrientationReady() {
        return this.orientationReady;
    }

    public void setOrientation(Vector3f longAxis, Vector3f side, Vector3f thick) {
        this.longAxis = longAxis;
        this.side = side;
        this.thick = thick;
        this.orientationReady = true;
    }

    private static float lengthFor(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            return Math.max(1.1f, entity.getBbWidth() * 0.9f + entity.getBbHeight() * 0.7f);
        }
        return 1.6f;
    }
}
