package com.yuan.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.yuan.data.YuanBanData;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class YuanCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("yuan")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("banlist").executes(ctx -> {
                    Set<UUID> entries = new LinkedHashSet<>(YuanBanData.sessionEntries());
                    entries.addAll(YuanBanData.get(ctx.getSource().getServer()).persistentEntries());
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            entries.isEmpty() ? "Yuan ban list is empty" : "Yuan bans: " + entries), false);
                    return entries.size();
                }))
                .then(Commands.literal("unban")
                        .then(Commands.argument("uuid", StringArgumentType.word()).executes(ctx -> {
                            try {
                                UUID id = UUID.fromString(StringArgumentType.getString(ctx, "uuid"));
                                boolean removed = YuanBanData.remove(ctx.getSource().getServer(), id);
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                        removed ? "Removed Yuan ban: " + id : "UUID was not banned: " + id), true);
                                return removed ? 1 : 0;
                            } catch (IllegalArgumentException e) {
                                ctx.getSource().sendFailure(Component.literal("Invalid UUID"));
                                return 0;
                            }
                        })))
                .then(Commands.literal("unbanall").executes(ctx -> {
                    YuanBanData.clearSession();
                    YuanBanData.get(ctx.getSource().getServer()).clearPersistent();
                    ctx.getSource().sendSuccess(() -> Component.literal("Cleared all Yuan bans"), true);
                    return 1;
                })));
    }
}
