package com.yuan.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class YuanWeaponBinding {
    public static final String WEAPON_UUID_TAG = "YuanWeaponUUID";
    public static final String OWNER_UUID_TAG = "YuanOwnerUUID";
    private static final String PLAYER_TAG = "YuanWeaponBinding";
    private static final String STACK_TAG = "Stack";
    private static final String MISSING_SINCE_TAG = "MissingSince";
    private static final String LIFECYCLE_UNTIL_TAG = "LifecycleUntil";
    private static final String LEGAL_TRANSFER_TAG = "YuanLegalTransfer";
    private static final String DISABLED_TAG = "YuanWeaponDisabled";
    private static final String OFFLINE_TRANSFER_TAG = "YuanOfflineTransfer";
    private static final String TRANSFER_TICK_TAG = "TransferTick";
    private static final double CANDIDATE_SCAN_RADIUS = 32.0;
    private static final Map<UUID, Record> RECORDS = new java.util.HashMap<>();

    private YuanWeaponBinding() {}

    public static boolean configured(int mode) { return mode >= 1 && mode <= 3; }
    public static boolean blocksHostileDisarm(int mode) { return configured(mode); }
    public static boolean recalls(int mode) { return mode >= 2 && mode <= 3; }
    public static boolean recoversHostileDisarm(int mode) { return configured(mode); }
    public static boolean blocksManualDrop(int mode, boolean allowed) { return mode == 3 || mode == 2 && !allowed; }
    public static boolean blocksContainerTransfer(int mode, boolean allowed) { return mode == 3 || mode == 2 && !allowed; }
    public static boolean blocksPlayerTransfer(int mode, boolean allowed) { return mode == 3 || mode == 2 && !allowed; }
    public static boolean keepsOnDeath(int mode, boolean enabled) { return mode == 3 || mode == 2 && enabled; }

    public static boolean graceExpired(long missingSince, long now, int graceTicks) {
        return missingSince >= 0 && now - missingSince >= Math.max(0, graceTicks);
    }

    public static boolean restoreEligible(int mode, boolean autoRecall, boolean authorityExists,
                                          boolean lifecycleGrace, long missingSince, long now, int graceTicks) {
        return recoversHostileDisarm(mode) && autoRecall && !authorityExists && !lifecycleGrace
                && graceExpired(missingSince, now, graceTicks);
    }

    public static boolean restoreEligible(int mode, boolean autoRecall, boolean authorityExists,
                                          boolean lifecycleGrace, long missingSince, long now, int graceTicks,
                                          boolean legalCustody) {
        return !noRestoreWhileLegalCustody(legalCustody)
                && restoreEligible(mode, autoRecall, authorityExists, lifecycleGrace,
                missingSince, now, graceTicks);
    }

    public static boolean boundedCandidateScan(boolean currentLevelOnly, double radius) {
        return currentLevelOnly && radius >= 0 && radius <= CANDIDATE_SCAN_RADIUS;
    }

    public static boolean canInitializeCandidate(boolean localHolder, UUID owner, UUID player) {
        return localHolder && (owner == null || owner.equals(player));
    }

    public static boolean canInitializeCandidate(String holderKind, UUID owner, UUID player) {
        return "inventory".equals(holderKind) && (owner == null || owner.equals(player));
    }

    public static boolean legalPickup(boolean marked, UUID owner, UUID player) {
        return marked && player != null && (owner == null || !player.equals(owner));
    }

    public static UUID transferOwnerId(boolean marked, UUID currentOwner, UUID recipient) {
        return marked && recipient != null ? recipient : currentOwner;
    }

    public static UUID transferOwnerId(UUID currentOwner, UUID recipient) {
        return currentOwner == null ? recipient : currentOwner;
    }

    public static boolean claimUnownedPickup(CompoundTag stack, UUID recipient, int mode) {
        if (stack == null || recipient == null || !configured(mode) || isDisabled(stack)) return false;
        if (weaponId(stack) == null) stack.putUUID(WEAPON_UUID_TAG, UUID.randomUUID());
        stack.putUUID(OWNER_UUID_TAG, recipient);
        stack.remove(LEGAL_TRANSFER_TAG);
        return true;
    }

    public static boolean logoutRuntimeCleared(boolean persisted) {
        return persisted;
    }

    public static boolean loginReloadsPersisted(boolean runtimeCleared) {
        return runtimeCleared;
    }

    public static boolean containerCustodyPolicy(boolean allowed) {
        return allowed;
    }

    public static boolean markedCandidateIgnored(boolean marked, boolean localReturn) {
        return marked && !localReturn;
    }

    public static boolean localReturnClearsMarker(boolean marked, boolean localHolder, UUID owner, UUID player) {
        return marked && localHolder && owner != null && owner.equals(player);
    }

    public static boolean noRestoreWhileLegalCustody(boolean marked) {
        return marked;
    }

    public static boolean localReturnEligible(boolean playerInventorySlot, boolean remoteSlot) {
        return playerInventorySlot && !remoteSlot;
    }

    public static boolean applyContainerCustody(CompoundTag stack, boolean allowed) {
        if (!allowed || stack == null) return false;
        stack.putBoolean(LEGAL_TRANSFER_TAG, true);
        return true;
    }

    public static boolean containerCustodyClearsAuthority(boolean mutated, boolean hasAuthority) {
        return mutated && hasAuthority;
    }

    public record CustodyState(CompoundTag stack, boolean marked, boolean clearAuthority) {}

    public record LogoutState(CompoundTag persisted, boolean clearRuntime) {}

    public static boolean shouldNotifyContainer(CustodyState state, boolean mutated) {
        return mutated && state != null && state.marked() && state.clearAuthority();
    }

    public static CustodyState applyContainerCustodyState(CompoundTag stack, boolean allowed) {
        CompoundTag result = stack == null ? new CompoundTag() : stack.copy();
        boolean marked = applyContainerCustody(result, allowed);
        return new CustodyState(result, marked, marked);
    }

    public static CompoundTag serializeBindingRecord(CompoundTag stack, long lastSeen,
                                                     long missingSince, long lifecycleUntil) {
        return serializeBindingRecord(stack, lastSeen, missingSince, lifecycleUntil, "", Vec3.ZERO);
    }

    public static CompoundTag serializeBindingRecord(CompoundTag stack, long lastSeen,
                                                     long missingSince, long lifecycleUntil,
                                                     String dimension, Vec3 location) {
        CompoundTag record = new CompoundTag();
        if (stack == null || !stack.hasUUID(WEAPON_UUID_TAG) || !stack.hasUUID(OWNER_UUID_TAG)) return record;
        record.put("Stack", stack.copy());
        record.putLong("LastSeen", lastSeen);
        record.putLong(MISSING_SINCE_TAG, missingSince);
        record.putLong(LIFECYCLE_UNTIL_TAG, lifecycleUntil);
        record.putString("Dimension", dimension == null ? "" : dimension);
        Vec3 position = location == null ? Vec3.ZERO : location;
        record.putDouble("X", position.x);
        record.putDouble("Y", position.y);
        record.putDouble("Z", position.z);
        return record;
    }

    public static CompoundTag loadBindingRecord(CompoundTag record, UUID owner) {
        if (record == null || owner == null || !record.contains("Stack")) return new CompoundTag();
        CompoundTag stack = record.getCompound("Stack");
        if (!owner.equals(ownerId(stack)) || weaponId(stack) == null) return new CompoundTag();
        return record.copy();
    }

    public static boolean shouldRestoreCloneSnapshot(CompoundTag snapshot, int mode, boolean keepOnDeath,
                                                     long tombstoneTick, long snapshotTick) {
        return !isLegalCustody(snapshot) && cloneRestoreAllowed(false, keepOnDeath)
                && recalls(mode) && tombstoneAllowsSnapshot(tombstoneTick, snapshotTick);
    }

    public static boolean tombstoneAllowsSnapshot(long tombstoneTick, long snapshotTick) {
        return tombstoneTick == Long.MIN_VALUE || !tombstoneRejectsSnapshot(tombstoneTick, snapshotTick);
    }

    public static boolean shouldRestoreCloneSnapshot(ItemStack snapshot, int mode, boolean keepOnDeath,
                                                     long tombstoneTick, long snapshotTick) {
        return shouldRestoreCloneSnapshot(snapshot == null ? null : snapshot.getTag(), mode, keepOnDeath,
                tombstoneTick, snapshotTick);
    }

    public static LogoutState logoutRecordState(CompoundTag record) {
        CompoundTag persisted = loadBindingRecord(record, ownerId(record == null ? null : record.getCompound("Stack")));
        if (persisted.isEmpty()) return new LogoutState(new CompoundTag(), true);
        persisted.putLong(LIFECYCLE_UNTIL_TAG, Long.MAX_VALUE);
        return new LogoutState(persisted, true);
    }


    public static boolean applyContainerCustody(ServerPlayer player, ItemStack stack, boolean allowed) {
        CustodyState state = applyContainerCustodyState(stack.getTag(), allowed);
        if (!state.marked()) return false;
        stack.setTag(state.stack());
        if (containerCustodyClearsAuthority(state.marked(), hasPersistedRecord(player.getPersistentData()))) forget(player);
        return true;
    }

    public static boolean isLegalCustody(CompoundTag stack) {
        return stack != null && stack.getBoolean(LEGAL_TRANSFER_TAG);
    }

    public static boolean isLegalCustody(ItemStack stack) {
        return !stack.isEmpty() && isLegalCustody(stack.getTag());
    }

    public static CompoundTag trustedSnapshot(CompoundTag stack) {
        return stack == null ? new CompoundTag() : stack.copy();
    }

    public static boolean bindingAuthorityAvailable(boolean marked, boolean ownerMatches) {
        return !marked && ownerMatches;
    }

    public static boolean cloneRestoreAllowed(boolean snapshotMarked, boolean keepsOnDeath) {
        return !snapshotMarked && keepsOnDeath;
    }

    public static int authority(int firstRank, int secondRank) {
        if (firstRank < 0) return secondRank;
        if (secondRank < 0) return firstRank;
        return Math.min(firstRank, secondRank);
    }

    public static boolean authorityOwnerMatches(UUID owner, UUID player) {
        return owner != null && owner.equals(player);
    }

    public static boolean hasPersistedRecord(CompoundTag playerData) {
        return playerData != null && playerData.getCompound(Player.PERSISTED_NBT_TAG).contains(PLAYER_TAG);
    }

    public static void clearPersistedRecord(CompoundTag playerData) {
        if (playerData == null) return;
        CompoundTag persisted = playerData.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.remove(PLAYER_TAG);
        playerData.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static UUID persistedOwner(CompoundTag playerData) {
        CompoundTag saved = playerData.getCompound(Player.PERSISTED_NBT_TAG).getCompound(PLAYER_TAG);
        return ownerId(saved.getCompound(STACK_TAG));
    }

    public static void transferPersistedRecord(CompoundTag oldPlayerData, CompoundTag newPlayerData, UUID newOwner) {
        CompoundTag saved = oldPlayerData.getCompound(Player.PERSISTED_NBT_TAG).getCompound(PLAYER_TAG).copy();
        CompoundTag stack = saved.getCompound(STACK_TAG);
        stack.putUUID(OWNER_UUID_TAG, newOwner);
        saved.put(STACK_TAG, stack);
        clearPersistedRecord(oldPlayerData);
        CompoundTag persisted = newPlayerData.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(PLAYER_TAG, saved);
        newPlayerData.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static long persistedMissingSince(CompoundTag playerData) {
        return playerData.getCompound(Player.PERSISTED_NBT_TAG).getCompound(PLAYER_TAG).getLong(MISSING_SINCE_TAG);
    }

    public static long persistedLifecycleUntil(CompoundTag playerData) {
        return playerData.getCompound(Player.PERSISTED_NBT_TAG).getCompound(PLAYER_TAG).getLong(LIFECYCLE_UNTIL_TAG);
    }

    public static long loginMissingSince(long persistedMissingSince, long now) {
        return persistedMissingSince >= 0 ? persistedMissingSince : -1;
    }

    public static boolean shouldEnforceContainer(boolean recordLoaded, boolean weaponInInventory) {
        return recordLoaded && !weaponInInventory;
    }

    public static boolean legalTransferAllowed(int mode, boolean allowed) {
        return configured(mode) && allowed;
    }

    public static boolean ignoreLegalTransfer(boolean marked, boolean pickedUp) {
        return marked && !pickedUp;
    }

    public static void markOfflineTransfer(CompoundTag tombstones, UUID owner, UUID weapon) {
        markOfflineTransfer(tombstones, owner, weapon, 0);
    }

    public static void markOfflineTransfer(CompoundTag tombstones, UUID owner, UUID weapon, long transferTick) {
        ListTag list = tombstones.getList(OFFLINE_TRANSFER_TAG, Tag.TAG_COMPOUND);
        CompoundTag entry = new CompoundTag();
        entry.putUUID(OWNER_UUID_TAG, owner);
        entry.putUUID(WEAPON_UUID_TAG, weapon);
        entry.putLong(TRANSFER_TICK_TAG, transferTick);
        list.add(entry);
        tombstones.put(OFFLINE_TRANSFER_TAG, list);
    }

    public static boolean hasOfflineTransfer(CompoundTag tombstones, UUID owner, UUID weapon) {
        if (owner == null || weapon == null) return false;
        for (Tag tag : tombstones.getList(OFFLINE_TRANSFER_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) tag;
            if (owner.equals(ownerId(entry)) && weapon.equals(weaponId(entry))) return true;
        }
        return false;
    }

    public static boolean clearOfflineTransfer(CompoundTag tombstones, UUID owner, UUID weapon) {
        boolean removed = false;
        ListTag list = tombstones.getList(OFFLINE_TRANSFER_TAG, Tag.TAG_COMPOUND);
        for (int i = list.size() - 1; i >= 0; i--) {
            CompoundTag entry = list.getCompound(i);
            if (owner.equals(ownerId(entry)) && weapon.equals(weaponId(entry))) {
                list.remove(i);
                removed = true;
            }
        }
        if (removed) tombstones.put(OFFLINE_TRANSFER_TAG, list);
        return removed;
    }

    public static long offlineTransferTick(CompoundTag tombstones, UUID owner, UUID weapon) {
        for (Tag tag : tombstones.getList(OFFLINE_TRANSFER_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) tag;
            if (owner.equals(ownerId(entry)) && weapon.equals(weaponId(entry)))
                return entry.getLong(TRANSFER_TICK_TAG);
        }
        return Long.MIN_VALUE;
    }

    public static boolean shouldWriteOfflineTombstone(boolean ownerOffline) {
        return ownerOffline;
    }

    public static boolean tombstoneRejectsSnapshot(long tombstoneTick, long snapshotTick) {
        return tombstoneTick >= snapshotTick;
    }

    public static boolean shouldClearOfflineRecord(boolean tombstoneExists) {
        return tombstoneExists;
    }

    public static boolean ensureIdentity(CompoundTag root, UUID owner, int mode) {
        if (!configured(mode) || root == null || owner == null || isDisabled(root)) return false;
        boolean changed = false;
        if (!root.hasUUID(WEAPON_UUID_TAG)) {
            root.putUUID(WEAPON_UUID_TAG, UUID.randomUUID());
            changed = true;
        }
        if (!root.hasUUID(OWNER_UUID_TAG)) {
            root.putUUID(OWNER_UUID_TAG, owner);
            changed = true;
        }
        return changed;
    }

    public static boolean ensureIdentity(ServerPlayer player, ItemStack stack) {
        if (player == null || player.level().isClientSide || !(stack.getItem() instanceof YuanSwordItem)) return false;
        return ensureIdentity(stack.getOrCreateTag(), player.getUUID(), mode(stack));
    }

    public static UUID weaponId(CompoundTag root) {
        return root != null && root.hasUUID(WEAPON_UUID_TAG) ? root.getUUID(WEAPON_UUID_TAG) : null;
    }

    public static UUID ownerId(CompoundTag root) {
        return root != null && root.hasUUID(OWNER_UUID_TAG) ? root.getUUID(OWNER_UUID_TAG) : null;
    }

    public static UUID weaponId(ItemStack stack) { return stack.isEmpty() ? null : weaponId(stack.getTag()); }
    public static UUID ownerId(ItemStack stack) { return stack.isEmpty() ? null : ownerId(stack.getTag()); }

    public static boolean canUseWeaponPolicy(int mode, boolean disabled, boolean owned,
                                             boolean authoritativeInstance) {
        return !disabled && (!configured(mode) || owned && authoritativeInstance);
    }

    public static boolean canUseWeapon(ServerPlayer player, ItemStack stack) {
        if (player == null || stack.isEmpty() || !(stack.getItem() instanceof YuanSwordItem)) return false;
        int mode = mode(stack);
        if (!configured(mode)) return canUseWeaponPolicy(mode, isDisabled(stack.getTag()), false, false);
        tick(player);
        Record record = record(player);
        Candidate authority = record == null ? null : candidates(player, record.weaponId).stream()
                .filter(candidate -> record.weaponId != null
                        && record.weaponId.equals(weaponId(candidate.stack))
                        && bindingAuthorityAvailable(isLegalCustody(candidate.stack),
                        authorityOwnerMatches(ownerId(candidate.stack), player.getUUID())))
                .min(Comparator.comparingInt(candidate -> candidate.rank)).orElse(null);
        return canUseWeaponPolicy(mode, isDisabled(stack.getTag()), ownedBy(player, stack),
                authority != null && authority.stack == stack);
    }

    public static void repairOrUnbind(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        tag.remove(DISABLED_TAG);
        unbind(stack);
    }

    public static void tick(ServerPlayer player) {
        long now = player.serverLevel().getGameTime();
        Record record = record(player);
        List<Candidate> candidates = candidates(player, record == null ? null : record.weaponId);

        for (Candidate candidate : candidates) {
            if (localReturnClearsMarker(candidate.stack.getOrCreateTag().getBoolean(LEGAL_TRANSFER_TAG),
                    localHolder(player, candidate.holder), ownerId(candidate.stack), player.getUUID())) {
                candidate.stack.getOrCreateTag().remove(LEGAL_TRANSFER_TAG);
                if (record != null && record.weaponId != null
                        && record.weaponId.equals(weaponId(candidate.stack))) {
                    record = trust(player, candidate.stack, now);
                    candidates = candidates(player, record.weaponId);
                }
            }
        }

        if (record == null) {
            Candidate first = candidates.stream()
                    .filter(candidate -> canInitializeCandidate(localHolder(player, candidate.holder),
                            ownerId(candidate.stack), player.getUUID())
                            && !isDisabled(candidate.stack.getTag())
                            && !candidate.stack.getOrCreateTag().getBoolean(LEGAL_TRANSFER_TAG)
                            && configured(mode(candidate.stack)))
                    .min(Comparator.comparingInt(candidate -> candidate.rank)).orElse(null);
            if (first == null) return;
            ensureIdentity(player, first.stack);
            record = trust(player, first.stack, now);
            candidates = candidates(player, record.weaponId);
        }

        if (isLegalCustody(record.snapshot)) return;

        Record current = record;
        Candidate authority = candidates.stream()
                .filter(candidate -> current.weaponId != null && current.weaponId.equals(weaponId(candidate.stack))
                        && bindingAuthorityAvailable(isLegalCustody(candidate.stack),
                        authorityOwnerMatches(ownerId(candidate.stack), player.getUUID())))
                .min(Comparator.comparingInt(candidate -> candidate.rank)).orElse(null);
        if (authority != null) {
            record.suppressRestore = false;
            if (YuanConfig.get(authority.stack, YuanConfig.K_UNIQUE_WEAPON, true) && now >= record.lifecycleUntil) {
                for (Candidate candidate : candidates) {
                    if (candidate != authority && record.weaponId != null
                            && record.weaponId.equals(weaponId(candidate.stack))) candidate.invalidate();
                }
            }
            trust(player, authority.stack, now);
            return;
        }

        if (record.missingSince < 0) {
            record.missingSince = now;
            save(player, record);
        }
        if (record.suppressRestore) return;
        ItemStack snapshot = record.snapshot;
        boolean lifecycle = now < record.lifecycleUntil;
        if (!restoreEligible(mode(snapshot), YuanConfig.get(snapshot, YuanConfig.K_AUTO_RECALL, true), false,
                lifecycle, record.missingSince, now,
                YuanConfig.getInt(snapshot, YuanConfig.K_RECALL_GRACE_TICKS, 40), isLegalCustody(snapshot))) return;
        ItemStack restored = trustedCopy(snapshot);
        if (!player.getInventory().add(restored)) player.drop(restored, false);
        trust(player, restored, now);
    }

    public static ItemStack defenseStack(ServerPlayer player) {
        Record record = record(player);
        if (record == null || record.missingSince < 0) return ItemStack.EMPTY;
        if (isLegalCustody(record.snapshot)) return ItemStack.EMPTY;
        long now = player.serverLevel().getGameTime();
        int grace = YuanConfig.getInt(record.snapshot, YuanConfig.K_BINDING_DEFENSE_GRACE_TICKS, 20);
        return now - record.missingSince <= grace ? record.snapshot : ItemStack.EMPTY;
    }

    public static boolean blockToss(ServerPlayer player, ItemStack stack, boolean adminBypass) {
        if (!ownedBy(player, stack) || adminBypass && YuanConfig.get(stack, YuanConfig.K_BINDING_ADMIN_BYPASS, true))
            return false;
        boolean blocked = blocksManualDrop(mode(stack), YuanConfig.get(stack, YuanConfig.K_ALLOW_MANUAL_DROP, true));
        if (!blocked) {
            unbind(stack);
            forget(player);
        }
        return blocked;
    }

    public static boolean blockPickup(ServerPlayer player, ItemStack stack, boolean adminBypass) {
        UUID owner = ownerId(stack);
        if (owner == null || owner.equals(player.getUUID())) return false;
        if (legalPickup(stack.getOrCreateTag().getBoolean(LEGAL_TRANSFER_TAG), owner, player.getUUID())) return false;
        if (adminBypass && YuanConfig.get(stack, YuanConfig.K_BINDING_ADMIN_BYPASS, true)) return false;
        return blocksPlayerTransfer(mode(stack), YuanConfig.get(stack, YuanConfig.K_ALLOW_PLAYER_TRANSFER, true));
    }

    public static void transferOwner(ServerPlayer player, ItemStack stack) {
        UUID oldOwner = ownerId(stack);
        if (oldOwner == null) {
            if (!claimUnownedPickup(stack.getOrCreateTag(), player.getUUID(), mode(stack))) return;
            Tombstones tombstones = player.getServer() == null ? null : player.getServer().overworld().getDataStorage()
                    .computeIfAbsent(Tombstones::load, Tombstones::new, OFFLINE_TRANSFER_TAG);
            UUID id = weaponId(stack);
            if (tombstones != null) {
                clearOfflineTransfer(tombstones.data, player.getUUID(), id);
                tombstones.setDirty();
            }
            trust(player, stack, player.serverLevel().getGameTime());
            return;
        }
        if (oldOwner.equals(player.getUUID())) {
            stack.getOrCreateTag().remove(LEGAL_TRANSFER_TAG);
            trust(player, stack, player.serverLevel().getGameTime());
            return;
        }
        stack.getOrCreateTag().putUUID(OWNER_UUID_TAG, player.getUUID());
        stack.getOrCreateTag().remove(LEGAL_TRANSFER_TAG);
        ServerPlayer oldPlayer = player.getServer() == null ? null : player.getServer().getPlayerList().getPlayer(oldOwner);
        if (oldPlayer != null) forget(oldPlayer);
        if (player.getServer() != null) {
            Tombstones tombstones = player.getServer().overworld().getDataStorage()
                    .computeIfAbsent(Tombstones::load, Tombstones::new, OFFLINE_TRANSFER_TAG);
            UUID id = weaponId(stack);
            clearOfflineTransfer(tombstones.data, player.getUUID(), id);
            if (shouldWriteOfflineTombstone(oldPlayer == null))
                markOfflineTransfer(tombstones.data, oldOwner, id, player.serverLevel().getGameTime());
            else
                clearOfflineTransfer(tombstones.data, oldOwner, id);
            tombstones.setDirty();
        }
        trust(player, stack, player.serverLevel().getGameTime());
    }

    public static void keepDeathDrops(ServerPlayer player, Collection<ItemEntity> drops) {
        drops.removeIf(drop -> {
            ItemStack stack = drop.getItem();
            if (!ownedBy(player, stack) || !keepsOnDeath(mode(stack), YuanConfig.get(stack, YuanConfig.K_KEEP_ON_DEATH, true)))
                return false;
            Record record = trust(player, stack, player.serverLevel().getGameTime());
            record.lifecycleUntil = Long.MAX_VALUE;
            save(player, record);
            return true;
        });
    }

    public static void clonePlayer(ServerPlayer original, ServerPlayer replacement, boolean died) {
        CompoundTag saved = original.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(PLAYER_TAG);
        if (!saved.isEmpty()) {
            CompoundTag persisted = replacement.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            persisted.put(PLAYER_TAG, saved.copy());
            replacement.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        }
        Record record = RECORDS.remove(original.getUUID());
        if (record == null) record = load(replacement);
        if (record == null) return;
        RECORDS.put(replacement.getUUID(), record);
        record.lifecycleUntil = replacement.serverLevel().getGameTime()
                + YuanConfig.getInt(record.snapshot, YuanConfig.K_RECALL_GRACE_TICKS, 40);
        save(replacement, record);
        if (died && shouldRestoreCloneSnapshot(record.snapshot, mode(record.snapshot),
                YuanConfig.get(record.snapshot, YuanConfig.K_KEEP_ON_DEATH, true), Long.MIN_VALUE, record.lastSeenTick)
                && !contains(replacement, record.weaponId)) {
            ItemStack restored = trustedCopy(record.snapshot);
            replacement.getInventory().add(restored);
            trust(replacement, restored, replacement.serverLevel().getGameTime());
        }
    }

    public static void login(ServerPlayer player) {
        RECORDS.remove(player.getUUID());
        if (!loginReloadsPersisted(true)) return;
        Record record = record(player);
        if (record == null) return;
        record.suppressRestore = !YuanConfig.get(record.snapshot, YuanConfig.K_RESTORE_ON_LOGIN, true);
        if (record.suppressRestore) return;
        if (record.lifecycleUntil == Long.MAX_VALUE) {
            record.lifecycleUntil = player.serverLevel().getGameTime()
                    + YuanConfig.getInt(record.snapshot, YuanConfig.K_RECALL_GRACE_TICKS, 40);
            save(player, record);
        }
    }

    public static void logout(ServerPlayer player) {
        Record record = RECORDS.get(player.getUUID());
        if (record != null) {
            LogoutState state = logoutRecordState(serializeBindingRecord(record.snapshot.save(new CompoundTag()), record.lastSeenTick,
                    record.missingSince, record.lifecycleUntil));
            player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).put(PLAYER_TAG, state.persisted());
            if (state.clearRuntime()) RECORDS.remove(player.getUUID());
        }
    }

    public static void containerClosed(ServerPlayer player, AbstractContainerMenu menu) {
        Record record = record(player);
        if (!shouldEnforceContainer(record != null, record != null && contains(player, record.weaponId))) return;
        for (Slot slot : menu.slots) {
            if (record.weaponId != null && record.weaponId.equals(weaponId(slot.getItem()))) {
                if (blocksContainerTransfer(mode(record.snapshot),
                        YuanConfig.get(record.snapshot, YuanConfig.K_ALLOW_CONTAINER, true))) {
                    ItemStack stack = slot.getItem().copy();
                    slot.set(ItemStack.EMPTY);
                    if (!player.getInventory().add(stack)) player.drop(stack, false);
                    trust(player, stack, player.serverLevel().getGameTime());
                } else {
                    CustodyState custody = applyContainerCustodyState(slot.getItem().getTag(), true);
                    slot.getItem().setTag(custody.stack());
                    if (shouldNotifyContainer(custody, true)) {
                        slot.setChanged();
                        menu.broadcastChanges();
                    }
                    if (custody.clearAuthority()) forget(player);
                }
                return;
            }
        }
    }

    public static void clearAll() { RECORDS.clear(); }

    private static Record record(ServerPlayer player) {
        Record record = RECORDS.get(player.getUUID());
        if (record != null) return record;
        record = load(player);
        if (record != null) RECORDS.put(player.getUUID(), record);
        return record;
    }

    private static Record trust(ServerPlayer player, ItemStack stack, long now) {
        ItemStack snapshot = trustedCopy(stack);
        UUID id = weaponId(snapshot);
        Record record = RECORDS.computeIfAbsent(player.getUUID(), ignored -> new Record());
        record.weaponId = id;
        record.snapshot = snapshot;
        record.lastSeenTick = now;
        record.lastDimension = player.level().dimension().location().toString();
        record.lastLocation = player.position();
        record.missingSince = -1;
        record.lifecycleUntil = Math.min(record.lifecycleUntil, now);
        save(player, record);
        return record;
    }

    private static ItemStack trustedCopy(ItemStack stack) {
        ItemStack copy = stack.copy();
        CompoundTag config = copy.getTagElement("YuanConfig");
        if (config != null) copy.getOrCreateTag().put("YuanConfig", YuanConfig.sanitize(config));
        return copy;
    }

    private static List<Candidate> candidates(ServerPlayer player, UUID wanted) {
        List<Candidate> result = new ArrayList<>();
        IdentityHashMap<ItemStack, Boolean> seen = new IdentityHashMap<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            add(result, seen, stack, i, player.getInventory());
        }
        for (int i = 0; i < player.containerMenu.slots.size(); i++) {
            Slot slot = player.containerMenu.slots.get(i);
            add(result, seen, slot.getItem(), 1000 + i, slot);
        }
        Record record = RECORDS.get(player.getUUID());
        Vec3 center = record != null && record.lastDimension.equals(player.level().dimension().location().toString())
                ? record.lastLocation : player.position();
        List<ItemEntity> drops = player.serverLevel().getEntitiesOfClass(ItemEntity.class,
                AABB.ofSize(center, CANDIDATE_SCAN_RADIUS * 2, CANDIDATE_SCAN_RADIUS * 2,
                        CANDIDATE_SCAN_RADIUS * 2));
        drops.sort(Comparator.comparing(ItemEntity::getUUID));
        for (int i = 0; i < drops.size(); i++) add(result, seen, drops.get(i).getItem(), 100000 + i, drops.get(i));
        if (wanted == null) return result;
        return result.stream().filter(candidate -> wanted.equals(weaponId(candidate.stack))).toList();
    }

    private static void add(List<Candidate> result, IdentityHashMap<ItemStack, Boolean> seen,
                            ItemStack stack, int rank, Object holder) {
        if (stack.isEmpty() || !(stack.getItem() instanceof YuanSwordItem) || seen.put(stack, true) != null) return;
        result.add(new Candidate(stack, rank, holder));
    }

    private static boolean contains(ServerPlayer player, UUID id) {
        if (id == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            if (id.equals(weaponId(player.getInventory().getItem(i)))) return true;
        return false;
    }

    private static boolean ownedBy(ServerPlayer player, ItemStack stack) {
        return player.getUUID().equals(ownerId(stack));
    }

    private static int mode(ItemStack stack) {
        return YuanConfig.getInt(stack, YuanConfig.K_BINDING_MODE, 1);
    }

    private static void save(ServerPlayer player, Record record) {
        CompoundTag saved = serializeBindingRecord(record.snapshot.save(new CompoundTag()), record.lastSeenTick,
                record.missingSince, record.lifecycleUntil, record.lastDimension, record.lastLocation);
        if (saved.isEmpty()) return;
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(PLAYER_TAG, saved);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static Record load(ServerPlayer player) {
        CompoundTag saved = loadBindingRecord(player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG)
                .getCompound(PLAYER_TAG), player.getUUID());
        if (!saved.contains(STACK_TAG)) return null;
        ItemStack stack = ItemStack.of(saved.getCompound(STACK_TAG));
        UUID id = weaponId(stack);
        if (player.getServer() != null && id != null) {
            Tombstones tombstones = player.getServer().overworld().getDataStorage()
                    .computeIfAbsent(Tombstones::load, Tombstones::new, OFFLINE_TRANSFER_TAG);
            long tombstoneTick = offlineTransferTick(tombstones.data, player.getUUID(), id);
            if (!tombstoneAllowsSnapshot(tombstoneTick, saved.getLong("LastSeen"))) {
                clearPersistedRecord(player.getPersistentData());
                clearOfflineTransfer(tombstones.data, player.getUUID(), id);
                tombstones.setDirty();
                return null;
            }
            if (tombstoneTick != Long.MIN_VALUE) {
                clearOfflineTransfer(tombstones.data, player.getUUID(), id);
                tombstones.setDirty();
            }
        }
        if (stack.isEmpty() || id == null || !player.getUUID().equals(ownerId(stack))) {
            clearPersistedRecord(player.getPersistentData());
            return null;
        }
        Record record = new Record();
        record.weaponId = id;
        record.snapshot = trustedCopy(stack);
        record.lastSeenTick = saved.getLong("LastSeen");
        record.lastDimension = saved.getString("Dimension");
        record.lastLocation = new Vec3(saved.getDouble("X"), saved.getDouble("Y"), saved.getDouble("Z"));
        record.missingSince = saved.contains(MISSING_SINCE_TAG) ? saved.getLong(MISSING_SINCE_TAG) : -1;
        record.lifecycleUntil = saved.getLong(LIFECYCLE_UNTIL_TAG);
        return record;
    }

    private static void forget(ServerPlayer player) {
        RECORDS.remove(player.getUUID());
        clearPersistedRecord(player.getPersistentData());
    }

    private static void unbind(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        tag.remove(WEAPON_UUID_TAG);
        tag.remove(OWNER_UUID_TAG);
        tag.remove(LEGAL_TRANSFER_TAG);
    }

    private static boolean isDisabled(CompoundTag tag) {
        return tag != null && tag.getBoolean(DISABLED_TAG);
    }

    private static boolean localHolder(ServerPlayer player, Object holder) {
        return holder == player.getInventory()
                || holder instanceof Slot slot && slot.container == player.getInventory();
    }

    private static final class Tombstones extends SavedData {
        private final CompoundTag data = new CompoundTag();

        private static Tombstones load(CompoundTag tag) {
            Tombstones result = new Tombstones();
            result.data.merge(tag);
            return result;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.merge(data);
            return tag;
        }
    }

    private static final class Record {
        private UUID weaponId;
        private ItemStack snapshot = ItemStack.EMPTY;
        private long lastSeenTick;
        private String lastDimension = "";
        private Vec3 lastLocation = Vec3.ZERO;
        private long missingSince = -1;
        private long lifecycleUntil;
        private boolean suppressRestore;
    }

    private static final class Candidate {
        private final ItemStack stack;
        private final int rank;
        private final Object holder;

        private Candidate(ItemStack stack, int rank, Object holder) {
            this.stack = stack;
            this.rank = rank;
            this.holder = holder;
        }

        private void invalidate() {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean(DISABLED_TAG, true);
            tag.remove(WEAPON_UUID_TAG);
            tag.remove(OWNER_UUID_TAG);
            if (holder instanceof Slot slot) slot.setChanged();
            if (holder instanceof ItemEntity item) item.setItem(stack);
        }
    }
}
