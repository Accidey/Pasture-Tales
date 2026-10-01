package com.xulai.animalflee.logic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.CaveVinesPlantBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;

import java.util.Set;

public final class AnimalFoods {

    private static final Set<Item> CROP_ITEMS = Set.of(
            Items.WHEAT,
            Items.CARROT,
            Items.POTATO,
            Items.BEETROOT,
            Items.BREAD,
            Items.COOKIE,
            Items.CAKE,
            Items.PUMPKIN_PIE,
            Items.BAKED_POTATO,
            Items.POISONOUS_POTATO,
            Items.PUMPKIN,
            Items.MELON_SLICE,
            Items.SWEET_BERRIES,
            Items.GLOW_BERRIES,
            Items.TORCHFLOWER_SEEDS,
            Items.PITCHER_POD
    );

    private AnimalFoods() {
    }

    public static boolean isCrop(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (CROP_ITEMS.contains(item)) return true;

        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof CropBlock
                    || block instanceof NetherWartBlock
                    || block instanceof CocoaBlock
                    || block instanceof StemBlock
                    || block instanceof SweetBerryBushBlock
                    || block instanceof MushroomBlock
                    || block instanceof CaveVinesBlock
                    || block instanceof CaveVinesPlantBlock) {
                return true;
            }
        }

        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        if (key != null && key.getPath().contains("seed")) {
            return true;
        }

        return false;
    }

    public static boolean isMeat(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.MEAT);
    }

    public static boolean isVegetableFood(ItemStack stack) {
        return isCrop(stack) && !stack.is(Items.POISONOUS_POTATO);
    }
}