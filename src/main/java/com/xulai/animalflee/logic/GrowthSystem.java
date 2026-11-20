package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;

import java.util.concurrent.ThreadLocalRandom;

public final class GrowthSystem {

    public static final String NBT_GROWTH_START = "af_growth_start";
    public static final String NBT_GROWTH_ACCUM = "af_growth_accum";
    public static final String NBT_GROWTH_TARGET = "af_growth_target";
    public static final String NBT_GROWTH_LAST = "af_growth_last";

    private static final long DAY_TICKS = 24000L;
    private static final double COW_REFERENCE_VOLUME = 0.9 * 1.4;
    private static final int SIZE_SYNC_INTERVAL = 600;
    private static final double SCALE_EPSILON = 0.001;

    private GrowthSystem() {
    }

    public static void initGrowth(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        if (!data.contains(NBT_GROWTH_START)) {
            data.putLong(NBT_GROWTH_START, animal.level().getDayTime());
            data.putLong(NBT_GROWTH_ACCUM, 0L);
            data.putLong(NBT_GROWTH_TARGET, calculateGrowthTarget(animal));
            data.putLong(NBT_GROWTH_LAST, animal.level().getDayTime());
        }
    }

    public static void tickGrowth(Animal animal) {
        if (animal.level().isClientSide) return;
        if (!animal.isBaby()) return;

        initGrowth(animal);
        updateScale(animal);
        if (animal.tickCount % SIZE_SYNC_INTERVAL == 0) {
            SizeHealthSystem.apply(animal);
        }

        CompoundTag data = animal.getPersistentData();
        long dayTime = animal.level().getDayTime();
        long last = data.getLong(NBT_GROWTH_LAST);
        long delta = dayTime - last;
        if (delta < 0) delta = 0;

        if (hasEnoughHunger(animal)) {
            long accum = data.getLong(NBT_GROWTH_ACCUM) + delta;
            data.putLong(NBT_GROWTH_ACCUM, accum);
            if (accum >= data.getLong(NBT_GROWTH_TARGET)) {
                growUp(animal);
                return;
            }
        }

        data.putLong(NBT_GROWTH_LAST, dayTime);
    }

    public static float getProgress(Animal animal) {
        if (!animal.isBaby()) return 0.0F;
        CompoundTag data = animal.getPersistentData();
        long target = data.getLong(NBT_GROWTH_TARGET);
        if (target <= 0L) return 0.0F;
        long accum = data.getLong(NBT_GROWTH_ACCUM);
        return Math.min(1.0F, Math.max(0.0F, (float) accum / (float) target));
    }

    public static boolean hasEnoughHunger(Animal animal) {
        float maxHunger = HungerSystem.getMaxHunger(animal);
        if (maxHunger <= 0.0F) return false;
        return HungerSystem.getHunger(animal) >= maxHunger * growthThreshold();
    }

    private static void updateScale(Animal animal) {
        AttributeInstance attribute = animal.getAttribute(Attributes.SCALE);
        if (attribute == null) return;
        double expected = GenderLogic.getExpectedScale(animal);
        if (Math.abs(attribute.getBaseValue() - expected) < SCALE_EPSILON) return;
        GenderLogic.applyScale(animal, expected);
    }

    private static void growUp(Animal animal) {
        animal.setAge(0);
        CompoundTag data = animal.getPersistentData();
        data.remove(NBT_GROWTH_START);
        data.remove(NBT_GROWTH_ACCUM);
        data.remove(NBT_GROWTH_TARGET);
        data.remove(NBT_GROWTH_LAST);
        GenderLogic.applyScale(animal, GenderLogic.getExpectedScale(animal));
        SizeHealthSystem.apply(animal);
    }

    private static long calculateGrowthTarget(Animal animal) {
        int baseDays = Config.COW_GROWTH_DAYS.get();
        int floatDays = Config.GROWTH_FLOAT_DAYS.get();
        double scaled = baseDays * sizeFactor(animal);
        int offset = ThreadLocalRandom.current().nextInt(-floatDays, floatDays + 1);
        double days = Math.max(1.0, scaled + offset);
        return (long) (days * DAY_TICKS);
    }

    private static double sizeFactor(Animal animal) {
        EntityDimensions dims = animal.getType().getDimensions();
        double volume = dims.width() * dims.height();
        if (volume <= 0.0) return 1.0;
        return Math.max(0.25, Math.sqrt(volume / COW_REFERENCE_VOLUME));
    }

    private static float growthThreshold() {
        return Config.GROWTH_MIN_HUNGER_PERCENT.get() / 100.0F;
    }
}