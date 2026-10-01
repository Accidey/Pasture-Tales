package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

public final class HungerSystem {

    public static final String NBT_DOMESTICATED = "af_domesticated";
    public static final String NBT_HUNGER = "af_hunger";
    public static final String NBT_HUNGER_LAST_DAY_TIME = "af_hunger_last_daytime";
    public static final String NBT_STARVE_DAY_TIME = "af_starve_start_daytime";

    public static final float MAX_HUNGER = 1.0F;
    public static final float MIN_HUNGER = 0.0F;

    private static final float HUNGER_SAFE_THRESHOLD = 0.05F;
    private static final float PREGNANT_HUNGER_MULTIPLIER = 1.5F;
    private static final int TICKS_PER_MINUTE = 1200;
    private static final int STARVE_DAMAGE_INTERVAL = 20;
    private static final int TICK_INTERVAL = 20;
    private static final long MAX_ELAPSED = 24000L;

    private HungerSystem() {
    }

    public static boolean isDomesticated(Animal animal) {
        return animal.getPersistentData().getBoolean(NBT_DOMESTICATED);
    }

    public static void setDomesticated(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        if (!data.getBoolean(NBT_DOMESTICATED)) {
            data.putBoolean(NBT_DOMESTICATED, true);
        }
    }

    public static float getHunger(Animal animal) {
        return animal.getPersistentData().getFloat(NBT_HUNGER);
    }

    public static void setHunger(Animal animal, float hunger) {
        animal.getPersistentData().putFloat(NBT_HUNGER, Math.max(MIN_HUNGER, Math.min(MAX_HUNGER, hunger)));
    }

    public static void initHunger(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        if (!data.contains(NBT_HUNGER)) {
            data.putFloat(NBT_HUNGER, MAX_HUNGER);
        }
        if (!data.contains(NBT_HUNGER_LAST_DAY_TIME)) {
            data.putLong(NBT_HUNGER_LAST_DAY_TIME, animal.level().getDayTime());
        }
    }

    public static boolean isStarving(Animal animal) {
        return getHunger(animal) <= MIN_HUNGER;
    }

    public static boolean canEat(Animal animal, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (AnimalFoods.isVegetableFood(stack)) return true;
        return animal.isFood(stack);
    }

    public static float feed(Animal animal, ItemStack stack) {
        setDomesticated(animal);
        float current = getHunger(animal);
        float restored = restoreAmount(stack);
        float newHunger = Math.min(MAX_HUNGER, current + restored);
        setHunger(animal, newHunger);
        CompoundTag data = animal.getPersistentData();
        data.remove(NBT_STARVE_DAY_TIME);
        data.putLong(NBT_HUNGER_LAST_DAY_TIME, animal.level().getDayTime());
        return newHunger;
    }

    private static float restoreAmount(ItemStack stack) {
        if (AnimalFoods.isMeat(stack)) {
            return 0.5F;
        }
        return 0.25F;
    }

    public static void tick(Animal animal) {
        if (animal.level().isClientSide) return;
        if (!Config.HUNGER_ENABLED.get()) return;
        if (!isDomesticated(animal)) return;
        if (animal.tickCount % TICK_INTERVAL != 0) return;

        initHunger(animal);
        CompoundTag data = animal.getPersistentData();

        long dayTime = animal.level().getDayTime();
        long lastDayTime = data.getLong(NBT_HUNGER_LAST_DAY_TIME);
        long elapsed = dayTime - lastDayTime;
        elapsed = Math.min(elapsed, MAX_ELAPSED);

        float hunger = getHunger(animal);
        if (hunger > MIN_HUNGER) {
            int hungerMinutes = Config.HUNGER_DURATION_MINUTES.get();
            float drainPerTick = 1.0F / Math.max(1, hungerMinutes * TICKS_PER_MINUTE);
            float drain = drainPerTick * elapsed;
            if (PregnancySystem.isPregnant(animal)) {
                drain *= PREGNANT_HUNGER_MULTIPLIER;
            }
            hunger = Math.max(MIN_HUNGER, hunger - drain);
            setHunger(animal, hunger);
            data.putLong(NBT_HUNGER_LAST_DAY_TIME, dayTime);
            if (hunger <= MIN_HUNGER) {
                data.putLong(NBT_STARVE_DAY_TIME, dayTime);
            }
        } else {
            long starveStart = data.getLong(NBT_STARVE_DAY_TIME);
            int starveDays = Config.HUNGER_STARVE_DAYS.get();
            long starveDuration = starveDays * 24000L;
            long starveElapsed = dayTime - starveStart;
            if (starveElapsed < 0) {
                starveElapsed += 24000L;
            }
            if (starveElapsed < starveDuration) {
                float damage = animal.getMaxHealth() / (starveDuration / (float) STARVE_DAMAGE_INTERVAL);
                animal.hurt(animal.damageSources().starve(), damage);
            }
        }
    }

    public static float getDisplayHunger(Animal animal) {
        return getHunger(animal);
    }

    public static boolean isHungry(Animal animal) {
        return getHunger(animal) < HUNGER_SAFE_THRESHOLD;
    }
}