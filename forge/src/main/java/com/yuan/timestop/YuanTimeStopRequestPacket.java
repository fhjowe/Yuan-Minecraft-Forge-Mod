package com.yuan.timestop;

import com.yuan.Yuan;
import com.yuan.item.YuanGodSwordConfig;
import com.yuan.item.YuanGodSwordItem;
import com.yuan.registry.YuanItems;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class YuanTimeStopRequestPacket {
    private final boolean desired;

    public YuanTimeStopRequestPacket(boolean desired) {
        this.desired = desired;
    }

    public static void encode(YuanTimeStopRequestPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.desired);
    }

    public static YuanTimeStopRequestPacket decode(FriendlyByteBuf buffer) {
        return new YuanTimeStopRequestPacket(buffer.readBoolean());
    }

    public static void handle(YuanTimeStopRequestPacket message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Player sender = context.get().getSender();
            if (message == null || sender == null) {
                return;
            }
            boolean holdsGodSword = sender.getMainHandItem().getItem() instanceof YuanGodSwordItem
                    || sender.getOffhandItem().getItem() instanceof YuanGodSwordItem;
            if (!holdsGodSword) {
                return;
            }
            ItemStack godSword = sender.getMainHandItem().getItem() instanceof YuanGodSwordItem
                    ? sender.getMainHandItem() : sender.getOffhandItem();
            YuanGodSwordConfig config = new YuanGodSwordConfig();
            config.read(godSword);
            if (!config.enabled) {
                return;
            }
            if (!YuanTimeStopServerState.cooldownReady(config.cooldown)) {
                return;
            }
            int cooldownTicks = Math.max(0, Math.min(100, config.cooldown));
            YuanTimeStopServerState.startCooldown(cooldownTicks);
            if (message.desired) {
                YuanTimeStopServerState.setInvulnerable(config.invulnerable);
                YuanTimeStopServerState.setFreezeSelf(config.freezeSelf);
                YuanTimeStopServerState.setFreezeEntities(config.freezeEntities);
                YuanTimeStopServerState.setFreezeBlocks(config.freezeBlocks);
                YuanTimeStopServerState.setFreezeFluids(config.freezeFluids);
                YuanTimeStopServerState.setFreezeBossAI(config.freezeBossAI);
                YuanTimeStopServerState.setStopRadius(config.stopRadius);
                YuanTimeStopServerState.setWielderPosition(
                        sender.getX(), sender.getY(), sender.getZ());
                long stopUntilMillis = config.stopDuration > 0f
                        ? System.currentTimeMillis() + (long) (config.stopDuration * 1000L)
                        : 0L;
                YuanTimeStopServerState.setStopUntilMillis(stopUntilMillis);
            } else {
                YuanTimeStopServerState.setInvulnerable(YuanTimeStopConfig.invulnerable);
                YuanTimeStopServerState.setStopUntilMillis(0L);
                YuanTimeStopServerState.resetFreezeDefaults();
            }
            YuanTimeStopServerState.setStopped(message.desired);
            Yuan.CHANNEL.send(PacketDistributor.ALL.noArg(), new YuanTimeStopPacket(
                    message.desired, -1,
                    YuanTimeStopServerState.isFreezeSelf(),
                    YuanTimeStopServerState.isFreezeEntities(),
                    YuanTimeStopServerState.isFreezeBlocks(),
                    YuanTimeStopServerState.isFreezeFluids(),
                    YuanTimeStopServerState.isFreezeBossAI(),
                    YuanTimeStopServerState.getStopRadius(),
                    YuanTimeStopServerState.getWielderX(),
                    YuanTimeStopServerState.getWielderY(),
                    YuanTimeStopServerState.getWielderZ()));
        });
        context.get().setPacketHandled(true);
    }

    public boolean isDesired() {
        return desired;
    }
}
