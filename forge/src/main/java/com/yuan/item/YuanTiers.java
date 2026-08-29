package com.yuan.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;

public class YuanTiers {
    public static final Tier YUAN = new Tier() {
        @Override
        public int getUses() {
            return 5000;
        }
        @Override
        public float getSpeed() {
            return 10.0F;
        }
        @Override
        public float getAttackDamageBonus() {
            return 5.0F;
        }
        @Override
        public int getLevel() {
            return 4;
        }
        @Override
        public int getEnchantmentValue() {
            return 30;
        }
        @Override
        public net.minecraft.world.item.crafting.Ingredient getRepairIngredient() {
            return net.minecraft.world.item.crafting.Ingredient.of(Items.NETHER_STAR);
        }
    };
}
