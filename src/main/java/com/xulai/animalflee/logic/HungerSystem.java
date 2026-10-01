package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class HungerSystem {

    public static final String NBT_DOMESTICATED = "af_domesticated";
    public static final String NBT_HUNGER = "af_hunger";
    public static final String NBT_MAX_HUNGER = "af_max_hunger";
    public static final String NBT_METABOLISM = "af_metabolism";
    public static final String NBT_HUNGER_LAST_DAY_TIME = "af_hunger_last_daytime";
    public static final String NBT_STARVE_DAY_TIME = "af_starve_start_daytime";

    public static final float MAX_HUNGER = 20.0F;
    public static final float MIN_HUNGER = 0.0F;
    private static final float PLAYER_REFERENCE_VOLUME = 0.9F * 1.4F;
    private static final float MIN_CAPACITY = 8.0F;
    private static final float PREGNANT_HUNGER_MULTIPLIER = 1.5F;
    private static final float HUNGER_SAFE_THRESHOLD = 0.5F;
    private static final int TICK_INTERVAL = 20;
    private static final int STARVE_DAMAGE_INTERVAL = 20;
    private static final long DAY_TICKS = 24000L;
    private static final float METABOLISM_MIN = 0.8F;
    private static final float METABOLISM_MAX = 1.2F;

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

    public static float getMaxHunger(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        return data.contains(NBT_MAX_HUNGER) ? data.getFloat(NBT_MAX_HUNGER) : MAX_HUNGER;
    }

    public static void setHunger(Animal animal, float hunger) {
        animal.getPersistentData().putFloat(NBT_HUNGER, Math.max(MIN_HUNGER, Math.min(getMaxHunger(animal), hunger)));
    }

    public static void initHunger(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        if (!data.contains(NBT_MAX_HUNGER)) {
            data.putFloat(NBT_MAX_HUNGER, calculateCapacity(animal));
        }
        if (!data.contains(NBT_METABOLISM)) {
            data.putFloat(NBT_METABOLISM, METABOLISM_MIN + ThreadLocalRandom.current().nextFloat() * (METABOLISM_MAX - METABOLISM_MIN));
        }
        if (!data.contains(NBT_HUNGER)) {
            data.putFloat(NBT_HUNGER, getMaxHunger(animal));
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
        return AnimalFoods.getSaturation(stack) > 0.0F;
    }

    public static float feed(Animal animal, ItemStack stack) {
        initHunger(animal);
        setDomesticated(animal);
        float current = getHunger(animal);
        float restored = AnimalFoods.getSaturation(stack);
        float newHunger = Math.min(getMaxHunger(animal), current + restored);
        setHunger(animal, newHunger);
        CompoundTag data = animal.getPersistentData();
        data.remove(NBT_STARVE_DAY_TIME);
        data.putLong(NBT_HUNGER_LAST_DAY_TIME, animal.level().getDayTime());
        return newHunger;
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
        elapsed = Math.min(elapsed, DAY_TICKS);

        float hunger = getHunger(animal);
        if (hunger > MIN_HUNGER) {
            float metabolism = data.getFloat(NBT_METABOLISM);
            float dailyDrainPercent = Config.HUNGER_DAILY_DRAIN_PERCENT.get() / 100.0F;
            float drainPerDay = getMaxHunger(animal) * dailyDrainPercent * metabolism;
            if (PregnancySystem.isPregnant(animal)) {
                drainPerDay *= PREGNANT_HUNGER_MULTIPLIER;
            }
            float drain = drainPerDay * ((float) elapsed / DAY_TICKS);
            hunger = Math.max(MIN_HUNGER, hunger - drain);
            setHunger(animal, hunger);
            data.putLong(NBT_HUNGER_LAST_DAY_TIME, dayTime);
            if (hunger <= MIN_HUNGER) {
                data.putLong(NBT_STARVE_DAY_TIME, dayTime);
            }
        } else {
            long starveStart = data.getLong(NBT_STARVE_DAY_TIME);
            int starveDays = Config.HUNGER_STARVE_DAYS.get();
            long starveDuration = starveDays * DAY_TICKS;
            long starveElapsed = dayTime - starveStart;
            if (starveElapsed < 0) {
                starveElapsed += DAY_TICKS;
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

    private static float calculateCapacity(Animal animal) {
        EntityDimensions dims = animal.getType().getDimensions();
        float volume = dims.width() * dims.height();
        if (volume <= 0.0F) return MAX_HUNGER;
        float capacity = MAX_HUNGER * (volume / PLAYER_REFERENCE_VOLUME);
        return Math.max(MIN_CAPACITY, capacity);
    }
}