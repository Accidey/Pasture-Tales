package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;

public final class SizeHealthSystem {

    public static final String NBT_BASE_HEALTH = "af_base_health";

    private static final double COW_REFERENCE_VOLUME = 0.9 * 1.4;
    private static final float MIN_HEALTH = 4.0F;
    private static final float CAPACITY_EPSILON = 0.01F;
    private static final double HEALTH_EPSILON = 0.01;

    private SizeHealthSystem() {
    }

    public static void apply(Animal animal) {
        GenderLogic.initGender(animal);
        applyMaxHealth(animal);
        applyCapacity(animal);
    }

    public static void applyMaxHealth(Animal animal) {
        AttributeInstance attribute = animal.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;

        CompoundTag data = animal.getPersistentData();
        if (!data.contains(NBT_BASE_HEALTH)) {
            data.putFloat(NBT_BASE_HEALTH, (float) attribute.getBaseValue());
        }
        double original = data.getFloat(NBT_BASE_HEALTH);

        double adultVolume = adultVolumeOf(animal);
        double formula = (double) Config.COW_MAX_HEALTH.get() * adultVolume / COW_REFERENCE_VOLUME;
        double adultTarget = Math.max(original, formula);

        double target;
        if (animal.isBaby()) {
            double ratio = adultVolume > 0.0 ? volumeOf(animal) / adultVolume : 1.0;
            target = Math.max(MIN_HEALTH, adultTarget * ratio);
        } else {
            target = Math.max(MIN_HEALTH, adultTarget);
        }

        if (Math.abs(target - attribute.getBaseValue()) < HEALTH_EPSILON) return;

        float oldMax = animal.getMaxHealth();
        float oldHealth = animal.getHealth();
        attribute.setBaseValue(target);

        float newMax = animal.getMaxHealth();
        if (oldMax <= 0.0F || newMax <= 0.0F) return;
        float ratio = Math.min(1.0F, oldHealth / oldMax);
        animal.setHealth(Math.max(1.0F, ratio * newMax));
    }

    public static void applyCapacity(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        float target = HungerSystem.calculateCapacity(animal);
        if (!data.contains(HungerSystem.NBT_MAX_HUNGER) || !data.contains(HungerSystem.NBT_HUNGER)) {
            data.putFloat(HungerSystem.NBT_MAX_HUNGER, target);
            return;
        }

        float oldMax = data.getFloat(HungerSystem.NBT_MAX_HUNGER);
        if (Math.abs(target - oldMax) < CAPACITY_EPSILON) return;

        float oldHunger = data.getFloat(HungerSystem.NBT_HUNGER);
        float ratio = oldMax <= 0.0F ? 0.0F : oldHunger / oldMax;
        data.putFloat(HungerSystem.NBT_MAX_HUNGER, target);
        HungerSystem.setHunger(animal, ratio * target);
    }

    private static double volumeOf(Animal animal) {
        EntityDimensions dims = animal.getDimensions(animal.getPose());
        double volume = (double) dims.width() * dims.height();
        return volume > 0.0 ? volume : COW_REFERENCE_VOLUME;
    }

    private static double adultVolumeOf(Animal animal) {
        EntityDimensions dims = animal.getType().getDimensions().scale((float) GenderLogic.getBabyScale(animal));
        double volume = (double) dims.width() * dims.height();
        return volume > 0.0 ? volume : COW_REFERENCE_VOLUME;
    }
}
