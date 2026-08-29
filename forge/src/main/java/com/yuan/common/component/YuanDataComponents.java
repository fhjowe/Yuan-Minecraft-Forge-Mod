package com.yuan.common.component;

import com.yuan.Yuan;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class YuanDataComponents {
    public static final String AWAKENED_STATE_KEY = "AwakenedState";

    public static boolean isAwakened(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(AWAKENED_STATE_KEY);
    }

    public static void setAwakened(ItemStack stack, boolean awakened) {
        if (awakened) {
            stack.getOrCreateTag().putBoolean(AWAKENED_STATE_KEY, true);
        } else {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove(AWAKENED_STATE_KEY);
            }
        }
    }
}
