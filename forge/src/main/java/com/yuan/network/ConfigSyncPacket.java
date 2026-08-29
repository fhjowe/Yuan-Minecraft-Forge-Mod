package com.yuan.network;

import com.yuan.item.YuanSwordItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import com.yuan.Yuan;

import java.util.function.Supplier;

public class ConfigSyncPacket {
    private final CompoundTag config;
    private final int selectedSlot;
    private final long requestId;
    private final int stackIdentity;

    public ConfigSyncPacket(CompoundTag config) { this(config, -1, 0, 0); }
    public ConfigSyncPacket(CompoundTag config, int selectedSlot) { this(config, selectedSlot, 0, 0); }
    public ConfigSyncPacket(CompoundTag config, int selectedSlot, long requestId) { this(config, selectedSlot, requestId, 0); }
    public ConfigSyncPacket(CompoundTag config, int selectedSlot, long requestId, int stackIdentity) {
        this.config = config; this.selectedSlot = selectedSlot; this.requestId = requestId; this.stackIdentity = stackIdentity;
    }
    public ConfigSyncPacket(FriendlyByteBuf buf) {
        this.config = buf.readNbt(); this.selectedSlot = buf.readVarInt(); this.requestId = buf.readVarLong();
        this.stackIdentity = buf.readInt();
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(config); buf.writeVarInt(selectedSlot); buf.writeVarLong(requestId);
        buf.writeInt(stackIdentity);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (player != null) {
                if (selectedSlot >= 0 && player.getInventory().selected != selectedSlot) {
                    acknowledge(player, false, new CompoundTag(), 0, "主手栏位已改变"); return;
                }
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof YuanSwordItem) {
                    CompoundTag currentConfig = stack.getTagElement("YuanConfig");
                    int currentIdentity = currentConfig == null ? 0 : currentConfig.toString().hashCode();
                    if (stackIdentity != currentIdentity) {
                        acknowledge(player, false, new CompoundTag(), 0, "虚渊物品已改变"); return;
                    }
                    CompoundTag clean = com.yuan.item.YuanConfig.sanitize(config);
                    int corrections = 0;
                    for (String key : clean.getAllKeys())
                        if (!config.contains(key) || !clean.get(key).equals(config.get(key))) corrections++;
                    stack.getOrCreateTag().put("YuanConfig", clean.copy());
                    acknowledge(player, true, clean, corrections, "");
                } else acknowledge(player, false, new CompoundTag(), 0, "主手未持有虚渊");
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void acknowledge(Player player, boolean success, CompoundTag applied, int corrections, String message) {
        if (requestId <= 0 || !(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;
        Yuan.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                new ConfigSyncAckPacket(requestId, success, applied, corrections, message));
    }
}
