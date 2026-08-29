package com.yuan.network;

import com.yuan.item.YuanSwordItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ModeSwitchMessage {
    private final int direction;

    public ModeSwitchMessage(int direction) {
        this.direction = direction;
    }

    public static int direction(int direction) {
        return direction == 1 || direction == -1 ? direction : 0;
    }

    public ModeSwitchMessage(FriendlyByteBuf buf) {
        this.direction = buf.readByte();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(direction);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (ModeSwitchMessage.direction(direction) == 0) return;
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof YuanSwordItem) {
                    if (direction == 1) {
                        YuanSwordItem.nextMode(stack);
                    } else {
                        YuanSwordItem.prevMode(stack);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
