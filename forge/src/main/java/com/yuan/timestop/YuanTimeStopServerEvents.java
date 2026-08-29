package com.yuan.timestop;

import com.yuan.Yuan;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class YuanTimeStopServerEvents {
    private YuanTimeStopServerEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Yuan.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new YuanTimeStopPacket(
                            YuanTimeStopServerState.isStopped(), -1,
                            YuanTimeStopServerState.isFreezeSelf(),
                            YuanTimeStopServerState.isFreezeEntities(),
                            YuanTimeStopServerState.isFreezeBlocks(),
                            YuanTimeStopServerState.isFreezeFluids(),
                            YuanTimeStopServerState.isFreezeBossAI(),
                            YuanTimeStopServerState.getStopRadius(),
                            YuanTimeStopServerState.getWielderX(),
                            YuanTimeStopServerState.getWielderY(),
                            YuanTimeStopServerState.getWielderZ()));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long until = YuanTimeStopServerState.getStopUntilMillis();
        if (until > 0L && YuanTimeStopServerState.isStopped()
                && System.currentTimeMillis() >= until) {
            YuanTimeStopServerState.setStopped(false);
            YuanTimeStopServerState.setInvulnerable(YuanTimeStopConfig.invulnerable);
            YuanTimeStopServerState.setStopUntilMillis(0L);
            YuanTimeStopServerState.resetFreezeDefaults();
            Yuan.CHANNEL.send(PacketDistributor.ALL.noArg(), new YuanTimeStopPacket(
                    false, -1,
                    YuanTimeStopServerState.isFreezeSelf(),
                    YuanTimeStopServerState.isFreezeEntities(),
                    YuanTimeStopServerState.isFreezeBlocks(),
                    YuanTimeStopServerState.isFreezeFluids(),
                    YuanTimeStopServerState.isFreezeBossAI(),
                    YuanTimeStopServerState.getStopRadius(),
                    YuanTimeStopServerState.getWielderX(),
                    YuanTimeStopServerState.getWielderY(),
                    YuanTimeStopServerState.getWielderZ()));
        }
    }
}
