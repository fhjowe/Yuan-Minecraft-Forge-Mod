package com.yuan.network;

import com.yuan.item.YuanGodSwordConfig;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class YuanGodSwordConfigPacket {
    private final CompoundTag tag;

    public YuanGodSwordConfigPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public static void encode(YuanGodSwordConfigPacket message, FriendlyByteBuf buffer) {
        buffer.writeNbt(message.tag);
    }

    public static YuanGodSwordConfigPacket decode(FriendlyByteBuf buffer) {
        return new YuanGodSwordConfigPacket(buffer.readNbt());
    }

    public static void handle(YuanGodSwordConfigPacket message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || message.tag == null) {
                return;
            }
            ItemStack stack = player.getMainHandItem();
            if (!stack.isEmpty()) {
                stack.getOrCreateTag().put(YuanGodSwordConfig.TAG, message.tag.copy());
            }
            com.yuan.timerewind.YuanTimeRewindEvents.invalidateWindowCache();
        });
        context.get().setPacketHandled(true);
    }
}
