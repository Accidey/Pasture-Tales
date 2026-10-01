package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
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
import net.neoforged.neoforge.common.Tags;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class AnimalFoods {

    public enum Diet {
        HERBIVORE,
        CARNIVORE,
        OMNIVORE
    }

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
            Items.MELON,
            Items.MELON_SLICE,
            Items.SWEET_BERRIES,
            Items.GLOW_BERRIES,
            Items.TORCHFLOWER_SEEDS,
            Items.PITCHER_POD
    );

    private static final float SEED_SATURATION = 0.25F;
    private static final float CROP_DEFAULT_SATURATION = 1.0F;
    private static final float WHEAT_SATURATION = 1.5F;
    private static final float BIG_MELON_SATURATION = 8.0F;
    private static final float COCOA_SATURATION = 1.0F;
    private static final float HAY_BLOCK_SATURATION = 2.0F;
    private static final float SUGAR_SATURATION = 1.0F;

    private static Map<String, Float> customFoodCache = null;
    private static Map<String, Diet> dietCache = null;

    private AnimalFoods() {
    }

    public static void resetCustomFoodCache() {
        customFoodCache = null;
    }

    public static void resetDietCache() {
        dietCache = null;
    }

    private static Map<String, Float> getCustomFoodCache() {
        if (customFoodCache == null) {
            Map<String, Float> map = new HashMap<>();
            for (String entry : Config.CUSTOM_ANIMAL_FOODS.get()) {
                String trimmed = entry.trim();
                if (trimmed.isEmpty()) continue;
                int colon = trimmed.lastIndexOf(':');
                if (colon > 0 && colon < trimmed.length() - 1) {
                    String id = trimmed.substring(0, colon);
                    try {
                        float value = Float.parseFloat(trimmed.substring(colon + 1));
                        if (value > 0.0F) {
                            map.put(id, value);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(trimmed));
                    if (item != null) {
                        var food = new ItemStack(item).getFoodProperties(null);
                        if (food != null) {
                            map.put(trimmed, (float) food.nutrition());
                        }
                    }
                }
            }
            customFoodCache = map;
        }
        return customFoodCache;
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
        return key != null && key.getPath().contains("seed");
    }

    public static boolean isMeat(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ItemTags.MEAT)
                || stack.is(Tags.Items.FOODS_RAW_MEAT)
                || stack.is(Tags.Items.FOODS_COOKED_MEAT);
    }

    public static boolean isFish(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ItemTags.FISHES)
                || stack.is(Tags.Items.FOODS_RAW_FISH)
                || stack.is(Tags.Items.FOODS_COOKED_FISH);
    }

    public static boolean isMeatOrFish(ItemStack stack) {
        return isMeat(stack) || isFish(stack);
    }

    public static float getSaturation(ItemStack stack) {
        if (stack.isEmpty()) return 0.0F;

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemKey != null) {
            Float custom = getCustomFoodCache().get(itemKey.toString());
            if (custom != null) return custom;
        }

        var food = stack.getFoodProperties(null);
        if (food != null) return food.nutrition();

        Item item = stack.getItem();
        if (item == Items.WHEAT) return WHEAT_SATURATION;
        if (item == Items.PUMPKIN || item == Items.MELON) return BIG_MELON_SATURATION;
        if (item == Items.COCOA_BEANS) return COCOA_SATURATION;
        if (item == Items.HAY_BLOCK.asItem()) return HAY_BLOCK_SATURATION;
        if (item == Items.SUGAR) return SUGAR_SATURATION;
        if (isSeed(stack)) return SEED_SATURATION;
        if (isCrop(stack)) return CROP_DEFAULT_SATURATION;
        return 0.0F;
    }

    public static Diet getDiet(EntityType<?> type) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) return Diet.OMNIVORE;
        return getDietCache().getOrDefault(key.toString(), Diet.OMNIVORE);
    }

    public static Diet getDiet(Mob mob) {
        return getDiet(mob.getType());
    }

    public static boolean canEatByDiet(Animal animal, ItemStack stack) {
        Diet diet = getDiet(animal);
        boolean meatOrFish = isMeatOrFish(stack);
        return switch (diet) {
            case HERBIVORE -> !meatOrFish;
            case CARNIVORE -> meatOrFish;
            case OMNIVORE -> true;
        };
    }

    private static Map<String, Diet> getDietCache() {
        if (dietCache == null) {
            Map<String, Diet> map = new HashMap<>();
            for (String id : Config.HERBIVORE_MOBS.get()) {
                String trimmed = id.trim();
                if (!trimmed.isEmpty()) {
                    map.put(trimmed, Diet.HERBIVORE);
                }
            }
            for (String id : Config.CARNIVORE_MOBS.get()) {
                String trimmed = id.trim();
                if (!trimmed.isEmpty()) {
                    map.put(trimmed, Diet.CARNIVORE);
                }
            }
            dietCache = map;
        }
        return dietCache;
    }

    private static boolean isSeed(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.TORCHFLOWER_SEEDS || item == Items.PITCHER_POD) return true;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key != null && key.getPath().contains("seed");
    }
}