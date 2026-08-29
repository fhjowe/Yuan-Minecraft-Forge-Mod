package com.yuan.client;

import com.yuan.Yuan;
import com.yuan.client.gui.YuanGuiLogic;
import com.yuan.item.YuanSwordItem;
import com.yuan.network.ModeSwitchMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class YuanSwordModeHandler {

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!YuanGuiLogic.shouldHandleModeScroll(mc.screen != null, event.getScrollDelta())) return;
        if (!mc.player.isShiftKeyDown()) return;
        ItemStack stack = mc.player.getMainHandItem();
        if (!(stack.getItem() instanceof YuanSwordItem)) return;

        event.setCanceled(true);

        int direction = event.getScrollDelta() > 0 ? 1 : -1;

        // Apply locally for instant tooltip feedback
        if (direction > 0) YuanSwordItem.nextMode(stack);
        else YuanSwordItem.prevMode(stack);

        YuanSwordItem.AttackMode mode = YuanSwordItem.getMode(stack);
        mc.player.displayClientMessage(
            Component.literal("§6✦ 模式切换: " + mode.getDisplayName()), true);

        // Sync to server
        Yuan.CHANNEL.sendToServer(new ModeSwitchMessage(direction));
    }
}
