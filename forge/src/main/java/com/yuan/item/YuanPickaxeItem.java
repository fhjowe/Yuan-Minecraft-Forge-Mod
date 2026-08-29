package com.yuan.item;

import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class YuanPickaxeItem extends PickaxeItem {
    private static final int RADIUS = 1;
    
    public YuanPickaxeItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL)) return 20.0F;
        return super.getDestroySpeed(stack, state);
    }
    
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        
        if (!level.isClientSide && miner instanceof Player player && !player.isCreative() && !player.isShiftKeyDown()) {
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int y = -RADIUS; y <= RADIUS; y++) {
                    for (int z = -RADIUS; z <= RADIUS; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        BlockPos targetPos = pos.offset(x, y, z);
                        BlockState targetState = level.getBlockState(targetPos);
                        if (targetState.is(state.getBlock())) {
                            level.destroyBlock(targetPos, true, miner);
                            stack.hurtAndBreak(1, miner, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                        }
                    }
                }
            }
        }

        return result;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, java.util.List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.yuan.pickaxe.1").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.yuan.pickaxe.2").withStyle(ChatFormatting.GREEN));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
