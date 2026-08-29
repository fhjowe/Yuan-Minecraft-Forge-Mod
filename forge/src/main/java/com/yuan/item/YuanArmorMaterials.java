package com.yuan.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public class YuanArmorMaterials implements ArmorMaterial {
    public static final YuanArmorMaterials YUAN = new YuanArmorMaterials();
    
    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 1000;
            case CHESTPLATE -> 1500;
            case LEGGINGS -> 1200;
            case BOOTS -> 800;
            default -> 0;
        };
    }
    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 3;
            case CHESTPLATE -> 6;
            case LEGGINGS -> 5;
            case BOOTS -> 2;
            default -> 0;
        };
    }
    @Override
    public int getEnchantmentValue() {
        return 30;
    }
    @Override
    public net.minecraft.sounds.SoundEvent getEquipSound() {
        return net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_DIAMOND;
    }
    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(net.minecraft.world.item.Items.NETHER_STAR);
    }
    @Override
    public String getName() {
        return "yuan";
    }
    @Override
    public float getToughness() {
        return 4.0F;
    }
    @Override
    public float getKnockbackResistance() {
        return 0.3F;
    }
}
