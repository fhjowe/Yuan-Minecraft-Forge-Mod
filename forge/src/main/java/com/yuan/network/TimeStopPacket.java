package com.yuan.network;

import com.yuan.event.YuanSwordEvents;
import com.yuan.item.YuanConfig;
import com.yuan.item.YuanSwordItem;
import com.yuan.item.YuanWeaponBinding;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TimeStopPacket {
    private final boolean enable;

    public TimeStopPacket(boolean enable) { this.enable = enable; }
    public TimeStopPacket(FriendlyByteBuf buf) { this.enable = buf.readBoolean(); }
    public void encode(FriendlyByteBuf buf) { buf.writeBoolean(enable); }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            boolean changed;
            if (enable) {
                ItemStack stack = player.getMainHandItem();
                changed = stack.getItem() instanceof YuanSwordItem
                        && YuanWeaponBinding.canUseWeapon(player, stack)
                        && YuanConfig.get(stack, YuanConfig.K_TIME_STOP, true)
                        && YuanSwordEvents.startTime(player.getUUID(),
                        YuanConfig.get(stack, YuanConfig.K_TIME_FULL, true),
                        player.level().dimension().location().toString(), player.getX(), player.getY(), player.getZ(),
                        YuanConfig.getFloat(stack, YuanConfig.K_TIME_RANGE, 100));
            } else {
                changed = YuanSwordEvents.stopTime(player.getUUID());
            }
            if (!changed) return;
            YuanSwordEvents.syncTimeState(player.getServer());
            player.displayClientMessage(Component.literal(
                enable ? "§b§l⏸ 时间已停止 — 仅你可行动" : "§a§l▶ 时间恢复流动"), true);
        });
        ctx.get().setPacketHandled(true);
    }
}
