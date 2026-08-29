package com.yuan.registry;

import com.yuan.item.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;

public class YuanItems {
    public static final Item YUAN_SWORD = new YuanSwordItem(YuanTiers.YUAN, 999, 100.0F,
            new Item.Properties().fireResistant());
    public static final Item YUAN_GOD_SWORD = new YuanGodSwordItem(YuanTiers.YUAN, 1, -2.4F,
            new Item.Properties().fireResistant());
    public static final Item YUAN_ORIGIN_BLADE = new SwordItem(YuanTiers.YUAN, 1, -2.4F,
            new Item.Properties().fireResistant());
    public static final Item YUAN_AXE = new YuanAxeItem(YuanTiers.YUAN, 5.0F, -3.0F,
            new Item.Properties().fireResistant().durability(5000));
    public static final Item YUAN_PICKAXE = new YuanPickaxeItem(YuanTiers.YUAN, 1, -2.8F,
            new Item.Properties().fireResistant().durability(5000));
    public static final Item YUAN_BOW = new YuanBowItem(
            new Item.Properties().fireResistant().durability(5000));
    public static final Item YUAN_HELMET = new ArmorItem(YuanArmorMaterials.YUAN, ArmorItem.Type.HELMET,
            new Item.Properties().fireResistant());
    public static final Item YUAN_CHESTPLATE = new ArmorItem(YuanArmorMaterials.YUAN, ArmorItem.Type.CHESTPLATE,
            new Item.Properties().fireResistant());
    public static final Item YUAN_LEGGINGS = new ArmorItem(YuanArmorMaterials.YUAN, ArmorItem.Type.LEGGINGS,
            new Item.Properties().fireResistant());
    public static final Item YUAN_BOOTS = new ArmorItem(YuanArmorMaterials.YUAN, ArmorItem.Type.BOOTS,
            new Item.Properties().fireResistant());

    public static void init() {
        // No-op: Forge registers these instances through DeferredRegister in Yuan.
    }
}
