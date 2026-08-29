package com.yuan.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class YuanBowItem extends BowItem {
    private static final float FIRE_CHANCE = 1.0F;
    private static final float PIERCE_CHANCE = 0.10F;
    private static final float DRAW_SPEED_MULTIPLIER = 1.25F;
    
    public YuanBowItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int chargeTicks = getUseDuration(stack) - timeLeft;
        int fasterChargeTicks = Math.round(chargeTicks * DRAW_SPEED_MULTIPLIER);
        super.releaseUsing(stack, level, entity, getUseDuration(stack) - fasterChargeTicks);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        if (arrow.level().random.nextFloat() < FIRE_CHANCE) {
            arrow.setSecondsOnFire(100);
        }
        if (arrow.level().random.nextFloat() < PIERCE_CHANCE) {
            arrow.setPierceLevel((byte) Math.max(arrow.getPierceLevel(), 3));
        }
        return arrow;
    }
    
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(net.minecraft.world.item.Items.NETHER_STAR);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, java.util.List<net.minecraft.network.chat.Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.yuan.bow.1").withStyle(net.minecraft.ChatFormatting.RED));
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.yuan.bow.2").withStyle(net.minecraft.ChatFormatting.AQUA));
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.yuan.bow.3").withStyle(net.minecraft.ChatFormatting.GREEN));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
