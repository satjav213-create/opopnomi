package com.opopnomi.mod;

import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {

    public static final ItemGroup TAB = new ItemGroup("opopnomi") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.GOMU_GOMU_NO_MI.get());
        }
    };

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OpOpNoMi.MODID);

    public static final RegistryObject<Item> GOMU_GOMU_NO_MI = ITEMS.register("gomu_gomu_no_mi",
            () -> new GomuGomuNoMiItem(new Item.Properties()
                    .tab(TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .food(new Food.Builder()
                            .nutrition(2)
                            .saturationMod(0.3F)
                            .alwaysEat()
                            .build())));
}
