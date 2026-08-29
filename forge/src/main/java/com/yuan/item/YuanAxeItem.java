package com.yuan.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class YuanAxeItem extends AxeItem {
    private static final float STUN_CHANCE = 0.20F;
    private static final int STUN_DURATION = 40;
    private static final int MAX_TREE_BLOCKS = 48;
    
    public YuanAxeItem(Tier tier, float attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL)) return 15.0F;
        return super.getDestroySpeed(stack, state);
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result && attacker instanceof Player player) {
            if (player.getRandom().nextFloat() < STUN_CHANCE) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, STUN_DURATION, 3));
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, STUN_DURATION, 1));
            }
        }
        return result;
    }
    
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        
        if (!level.isClientSide && !miner.isShiftKeyDown() && miner instanceof Player player) {
            if (state.is(net.minecraft.tags.BlockTags.LOGS)) {
                List<BlockPos> treeBlocks = findConnectedLogs(level, pos, state.getBlock(), MAX_TREE_BLOCKS);
                for (BlockPos treePos : treeBlocks) {
                    if (!treePos.equals(pos)) {
                        level.destroyBlock(treePos, true, miner);
                        stack.hurtAndBreak(1, miner, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                    }
                }
            }
        }
        return result;
    }
    
    private List<BlockPos> findConnectedLogs(Level level, BlockPos start, Block block, int maxBlocks) {
        List<BlockPos> found = new ArrayList<>();
        Queue<BlockPos> queue = new java.util.ArrayDeque<>();
        queue.add(start);
        found.add(start);
        
        while (!queue.isEmpty() && found.size() < maxBlocks) {
            BlockPos current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) <= 1) {
                            BlockPos neighbor = current.offset(dx, dy, dz);
                            BlockState neighborState = level.getBlockState(neighbor);
                            if (neighborState.is(block) && !found.contains(neighbor)) {
                                found.add(neighbor);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
        return found;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, java.util.List<net.minecraft.network.chat.Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.yuan.axe.1").withStyle(net.minecraft.ChatFormatting.GREEN));
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.yuan.axe.2").withStyle(net.minecraft.ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
