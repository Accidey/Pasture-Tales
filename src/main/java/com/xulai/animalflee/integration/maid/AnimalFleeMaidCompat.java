package com.xulai.animalflee.integration.maid;

import com.xulai.animalflee.logic.AnimalFoods;
import com.xulai.animalflee.logic.HungerSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

public final class AnimalFleeMaidCompat {

    private static final float FEED_TRIGGER_RATIO = 0.5F;

    private AnimalFleeMaidCompat() {
    }

    public static boolean canBeBreedTarget(Animal animal) {
        if (!HungerSystem.isDomesticated(animal)) return false;
        float maxHunger = HungerSystem.getMaxHunger(animal);
        if (maxHunger <= 0.0F) return false;
        if (animal.isBaby()) {
            return HungerSystem.getHunger(animal) < maxHunger * FEED_TRIGGER_RATIO;
        }
        if (animal.getAge() != 0) return false;
        return HungerSystem.getHunger(animal) < maxHunger * FEED_TRIGGER_RATIO;
    }

    public static boolean canBeFedMore(Animal animal) {
        float maxHunger = HungerSystem.getMaxHunger(animal);
        return maxHunger > 0.0F && HungerSystem.getHunger(animal) < maxHunger;
    }

    public static boolean isValidFood(Animal animal, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (AnimalFoods.getSaturation(animal, stack) <= 0.0F) return false;
        return AnimalFoods.canEatByDiet(animal, stack);
    }

    public static void feedByMaid(Animal animal, ItemStack food, ServerPlayer owner) {
        if (animal.level().isClientSide) return;
        HungerSystem.feed(animal, food, owner);
    }
}