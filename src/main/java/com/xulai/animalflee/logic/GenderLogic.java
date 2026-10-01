package com.xulai.animalflee.logic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;

import java.util.concurrent.ThreadLocalRandom;

public class GenderLogic {

    public static final String NBT_GENDER = "af_gender";
    public static final String NBT_BABY_SCALE = "af_baby_scale";
    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";

    private static final double MALE_SCALE_MIN = 1.10;
    private static final double MALE_SCALE_MAX = 1.20;
    private static final double FEMALE_SCALE_MIN = 0.90;
    private static final double FEMALE_SCALE_MAX = 1.00;

    public static void initGender(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        if (!data.contains(NBT_GENDER)) {
            String gender = ThreadLocalRandom.current().nextBoolean() ? GENDER_MALE : GENDER_FEMALE;
            data.putString(NBT_GENDER, gender);
            data.putDouble(NBT_BABY_SCALE, randomScale(gender));
        }
        applyScale(animal, getBabyScale(animal));
    }

    private static double randomScale(String gender) {
        boolean male = GENDER_MALE.equals(gender);
        double min = male ? MALE_SCALE_MIN : FEMALE_SCALE_MIN;
        double max = male ? MALE_SCALE_MAX : FEMALE_SCALE_MAX;
        return min + ThreadLocalRandom.current().nextDouble() * (max - min);
    }

    public static void applyScale(Mob mob, double scale) {
        AttributeInstance attr = mob.getAttribute(Attributes.SCALE);
        if (attr != null) {
            attr.setBaseValue(scale);
            mob.refreshDimensions();
        }
    }

    public static boolean isMale(Animal animal) {
        return GENDER_MALE.equals(animal.getPersistentData().getString(NBT_GENDER));
    }

    public static boolean isFemale(Animal animal) {
        return GENDER_FEMALE.equals(animal.getPersistentData().getString(NBT_GENDER));
    }

    public static String getGender(Animal animal) {
        return animal.getPersistentData().getString(NBT_GENDER);
    }

    public static double getBabyScale(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        return data.contains(NBT_BABY_SCALE) ? data.getDouble(NBT_BABY_SCALE) : 1.0;
    }

    public static boolean isScaleApplied(Animal animal) {
        AttributeInstance attr = animal.getAttribute(Attributes.SCALE);
        if (attr == null) return true;
        return Math.abs(attr.getBaseValue() - getBabyScale(animal)) < 0.0001;
    }
}