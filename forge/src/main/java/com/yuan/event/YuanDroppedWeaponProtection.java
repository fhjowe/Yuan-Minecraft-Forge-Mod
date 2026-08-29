package com.yuan.event;

import com.yuan.item.YuanConfig;
import com.yuan.item.YuanSwordItem;
import com.yuan.item.YuanWeaponBinding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class YuanDroppedWeaponProtection {
    enum RescueResult { RESCUED, HELD, RETRY }

    private YuanDroppedWeaponProtection() {}

    public static boolean protectsDamage(boolean server, boolean yuanSword, boolean configured) {
        return server && yuanSword && configured;
    }

    public static boolean preventNaturalExpiry(boolean server, boolean yuanSword, boolean canDespawn) {
        return server && yuanSword && !canDespawn;
    }

    public static boolean shouldRescueBeforeVanilla(boolean server, boolean removed, boolean belowMinimum,
                                                    boolean configured) {
        return server && !removed && belowMinimum && configured;
    }

    static boolean candidateSafetyDecision(boolean loaded, boolean support, boolean collisionFree,
                                           boolean allCoveredBlocksFluidFree, boolean insideBorder,
                                           boolean insideBuildBounds) {
        return loaded && support && collisionFree && allCoveredBlocksFluidFree
                && insideBorder && insideBuildBounds;
    }

    public static int rescueDestination(boolean bound, boolean ownerOnline, boolean sameLevel,
                                        boolean ownerPositionSafe) {
        return bound && ownerOnline && sameLevel && ownerPositionSafe ? 1 : 2;
    }

    public static boolean usesProtectedEntity(boolean yuanSword) {
        return yuanSword;
    }

    public static boolean shouldReplaceJoinedItem(boolean server, boolean itemEntity,
                                                  boolean protectedEntity, boolean yuanSword) {
        return server && itemEntity && !protectedEntity && yuanSword;
    }

    static RescueResult rescueResult(boolean safeDestination, boolean safeHold) {
        return safeDestination ? RescueResult.RESCUED : safeHold ? RescueResult.HELD : RescueResult.RETRY;
    }

    static boolean runVanillaTick(RescueResult result) {
        return result == RescueResult.RESCUED;
    }

    static boolean holdNoGravity(RescueResult result) {
        return result == RescueResult.RETRY;
    }

    public static ItemEntity create(Level level, Entity original, ItemStack stack) {
        ProtectedItemEntity item = new ProtectedItemEntity(level, original.getX(), original.getY(), original.getZ(), stack);
        if (original instanceof ItemEntity old) {
            CompoundTag saved = old.saveWithoutId(new CompoundTag());
            item.load(saved);
            item.setUUID(old.getUUID());
        }
        item.setDeltaMovement(original.getDeltaMovement());
        return item;
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity old)) return;
        ItemStack stack = old.getItem();
        if (!shouldReplaceJoinedItem(!event.getLevel().isClientSide, true,
                old instanceof ProtectedItemEntity, stack.getItem() instanceof YuanSwordItem)) return;
        ItemEntity replacement = create(event.getLevel(), old, stack.copy());
        event.setCanceled(true);
        if (event.getLevel() instanceof ServerLevel level) level.addFreshEntity(replacement);
    }

    @SubscribeEvent
    public static void onExpire(ItemExpireEvent event) {
        ItemEntity item = event.getEntity();
        ItemStack stack = item.getItem();
        if (preventNaturalExpiry(!item.level().isClientSide, stack.getItem() instanceof YuanSwordItem,
                YuanConfig.get(stack, YuanConfig.K_DROP_CAN_DESPAWN, false))) event.setCanceled(true);
    }

    private static final class ProtectedItemEntity extends ItemEntity {
        private ProtectedItemEntity(Level level, double x, double y, double z, ItemStack stack) {
            super(level, x, y, z, stack);
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            ItemStack stack = getItem();
            if (protectsDamage(!level().isClientSide, stack.getItem() instanceof YuanSwordItem,
                    YuanConfig.get(stack, YuanConfig.K_DROP_DAMAGE_PROTECTION, true))) return false;
            return super.hurt(source, amount);
        }

        @Override
        public void tick() {
            if (isRemoved()) return;
            if (level() instanceof ServerLevel serverLevel) {
                ItemStack stack = getItem();
                if (shouldRescueBeforeVanilla(true, false, getY() < serverLevel.getMinBuildHeight(),
                        YuanConfig.get(stack, YuanConfig.K_DROP_VOID_RESCUE, true))
                        && !runVanillaTick(rescue(serverLevel, stack))) return;
            }
            if (!isRemoved()) super.tick();
        }

        private RescueResult rescue(ServerLevel level, ItemStack stack) {
            UUID ownerId = YuanWeaponBinding.ownerId(stack);
            ServerPlayer owner = ownerId == null || level.getServer() == null ? null
                    : level.getServer().getPlayerList().getPlayer(ownerId);
            boolean sameLevel = owner != null && owner.serverLevel() == level;
            Vec3 ownerPosition = sameLevel
                    ? findSafePosition(level, BlockPos.containing(owner.position()), 2, 2) : null;
            Vec3 destination = rescueDestination(ownerId != null, owner != null, sameLevel, ownerPosition != null) == 1
                    ? ownerPosition : findSafePosition(level, level.getSharedSpawnPos(), 4, 4);
            Vec3 hold = destination == null ? currentColumnHold(level) : null;
            RescueResult result = rescueResult(destination != null, hold != null);
            if (result == RescueResult.RESCUED) setPos(destination);
            if (result == RescueResult.HELD) setPos(hold);
            setDeltaMovement(Vec3.ZERO);
            fallDistance = 0;
            clearFire();
            setNoGravity(holdNoGravity(result));
            return result;
        }

        private Vec3 findSafePosition(ServerLevel level, BlockPos center, int radius, int vertical) {
            for (int dy = 0; dy <= vertical; dy++) {
                for (int r = 0; r <= radius; r++) {
                    for (int dx = -r; dx <= r; dx++) {
                        for (int dz = -r; dz <= r; dz++) {
                            if (r > 0 && Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                            BlockPos pos = center.offset(dx, dy, dz);
                            Vec3 candidate = Vec3.atBottomCenterOf(pos);
                            if (safePosition(level, pos, candidate)) return candidate;
                        }
                    }
                }
            }
            return null;
        }

        private boolean safePosition(ServerLevel level, BlockPos pos, Vec3 candidate) {
            AABB bounds = getDimensions(getPose()).makeBoundingBox(candidate);
            boolean buildBounds = bounds.minY >= level.getMinBuildHeight()
                    && bounds.maxY < level.getMaxBuildHeight();
            BlockPos below = pos.below();
            BlockPos min = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
            BlockPos max = BlockPos.containing(bounds.maxX - 1.0E-7, bounds.maxY - 1.0E-7,
                    bounds.maxZ - 1.0E-7);
            boolean loaded = buildBounds && level.hasChunkAt(below);
            boolean fluidFree = loaded;
            for (BlockPos covered : BlockPos.betweenClosed(min, max)) {
                if (!level.hasChunkAt(covered)) {
                    loaded = false;
                    fluidFree = false;
                    break;
                }
                if (!level.getFluidState(covered).isEmpty()) fluidFree = false;
            }
            BlockState support = loaded ? level.getBlockState(below) : null;
            return candidateSafetyDecision(loaded,
                    support != null && support.isFaceSturdy(level, below, Direction.UP),
                    buildBounds && loaded && level.noCollision(this, bounds), fluidFree,
                    buildBounds && level.getWorldBorder().isWithinBounds(bounds), buildBounds);
        }

        private Vec3 currentColumnHold(ServerLevel level) {
            Vec3 candidate = new Vec3(getX(), level.getMinBuildHeight() + 1, getZ());
            return safePosition(level, BlockPos.containing(candidate), candidate) ? candidate : null;
        }
    }
}
