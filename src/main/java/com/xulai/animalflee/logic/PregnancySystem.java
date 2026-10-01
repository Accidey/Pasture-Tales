package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class PregnancySystem {

    public static final String NBT_PREGNANT = "af_pregnant";
    public static final String NBT_PREGNANCY_START = "af_pregnancy_start";
    public static final String NBT_PREGNANCY_DURATION = "af_pregnancy_duration";
    public static final String NBT_PREGNANCY_FATHER = "af_pregnancy_father";
    public static final String NBT_PREGNANCY_FATHER_TYPE = "af_pregnancy_father_type";
    public static final String NBT_COOLDOWN_UNTIL = "af_cooldown_until";
    public static final String NBT_FERTILIZED_EGG = "af_fertilized";

    private static final int CHECK_INTERVAL = 100;
    private static final double CONCEPTION_CHANCE = 0.05;
    private static final double MATE_RADIUS = 8.0;
    private static final double BELLY_MAX_SCALE = 0.15;
    private static final double BELLY_START_PROGRESS = 0.4;
    private static final String BELLY_MODIFIER_ID = "animalflee:pregnancy_belly";
    private static final double COW_REFERENCE_VOLUME = 0.9 * 1.4;

    private PregnancySystem() {
    }

    public static boolean isPregnant(Animal animal) {
        return animal.getPersistentData().getBoolean(NBT_PREGNANT);
    }

    public static void tickPregnancy(Animal animal) {
        if (animal.level().isClientSide) return;
        if (animal.tickCount % CHECK_INTERVAL != 0) return;

        CompoundTag data = animal.getPersistentData();
        if (!data.getBoolean(NBT_PREGNANT)) {
            tryStartPregnancy(animal, data);
            return;
        }

        long start = data.getLong(NBT_PREGNANCY_START);
        long duration = data.getLong(NBT_PREGNANCY_DURATION);
        if (start <= 0 || duration <= 0) {
            clearPregnancy(animal);
            return;
        }

        long elapsed = animal.level().getDayTime() - start;
        float progress = Math.min(1.0F, Math.max(0.0F, (float) elapsed / (float) duration));
        updateBellyScale(animal, progress);

        if (elapsed >= duration) {
            giveBirth(animal, data);
        }
    }

    public static float getProgress(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        if (!data.getBoolean(NBT_PREGNANT)) return 0.0F;
        long start = data.getLong(NBT_PREGNANCY_START);
        long duration = data.getLong(NBT_PREGNANCY_DURATION);
        if (start <= 0 || duration <= 0) return 0.0F;
        long elapsed = animal.level().getDayTime() - start;
        return Math.min(1.0F, Math.max(0.0F, (float) elapsed / (float) duration));
    }

    private static void tryStartPregnancy(Animal animal, CompoundTag data) {
        if (animal.isBaby()) return;
        if (animal.getAge() != 0) return;
        if (!GenderLogic.isFemale(animal)) return;
        if (!HungerSystem.isDomesticated(animal)) return;
        if (isPregnant(animal)) return;
        if (isInCooldown(animal, data)) return;
        if (HungerSystem.getHunger(animal) < HungerSystem.getMaxHunger(animal) * breedMinHungerRatio()) return;
        if (animal.level().random.nextDouble() > CONCEPTION_CHANCE) return;

        Animal father = findNearbyMale(animal);
        if (father == null) return;

        conceive(animal, father);
    }

    public static void conceive(Animal mother, Animal father) {
        CompoundTag data = mother.getPersistentData();
        data.putBoolean(NBT_PREGNANT, true);
        data.putLong(NBT_PREGNANCY_START, mother.level().getDayTime());
        data.putLong(NBT_PREGNANCY_DURATION, calculatePregnancyDuration(mother));
        data.putUUID(NBT_PREGNANCY_FATHER, father.getUUID());
        ResourceLocation fatherType = BuiltInRegistries.ENTITY_TYPE.getKey(father.getType());
        if (fatherType != null) {
            data.putString(NBT_PREGNANCY_FATHER_TYPE, fatherType.toString());
        }
    }

    private static Animal findNearbyMale(Animal mother) {
        if (!(mother.level() instanceof ServerLevel serverLevel)) return null;
        boolean oviparous = isOviparous(mother);
        AABB box = mother.getBoundingBox().inflate(MATE_RADIUS);
        List<? extends Animal> candidates = serverLevel.getEntitiesOfClass(Animal.class, box, candidate ->
                candidate != mother
                        && isCompatibleMate(mother, candidate)
                        && !candidate.isBaby()
                        && candidate.getAge() == 0
                        && GenderLogic.isMale(candidate)
                        && (oviparous
                            || (HungerSystem.isDomesticated(candidate)
                                && HungerSystem.getHunger(candidate) >= HungerSystem.getMaxHunger(candidate) * breedMinHungerRatio()
                                && !isPregnant(candidate)
                                && !isInCooldown(candidate, candidate.getPersistentData()))));
        if (candidates.isEmpty()) return null;
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private static boolean isCompatibleMate(Animal mother, Animal candidate) {
        if (mother.getType() == candidate.getType()) return true;
        return isHorseDonkeyPair(mother.getType(), candidate.getType());
    }

    private static boolean isHorseDonkeyPair(EntityType<?> a, EntityType<?> b) {
        return (a == EntityType.HORSE && b == EntityType.DONKEY)
                || (a == EntityType.DONKEY && b == EntityType.HORSE);
    }

    private static boolean isInCooldown(Animal animal, CompoundTag data) {
        if (!data.contains(NBT_COOLDOWN_UNTIL)) return false;
        return animal.level().getDayTime() < data.getLong(NBT_COOLDOWN_UNTIL);
    }

    private static long calculatePregnancyDuration(Animal animal) {
        int baseMinutes = Config.COW_PREGNANCY_MINUTES.get();
        int floatMinutes = Config.PREGNANCY_FLOAT_MINUTES.get();
        double baseTicks = baseMinutes * 1200.0;
        double floatTicks = floatMinutes * 1200.0;
        double duration = baseTicks + ThreadLocalRandom.current().nextDouble() * floatTicks;
        return Math.max(1200L, (long) (duration * sizeFactor(animal)));
    }

    private static double sizeFactor(Animal animal) {
        EntityDimensions dims = animal.getType().getDimensions();
        double volume = dims.width() * dims.height();
        if (volume <= 0.0) return 1.0;
        return Math.max(0.25, Math.sqrt(volume / COW_REFERENCE_VOLUME));
    }

    private static void updateBellyScale(Animal animal, float progress) {
        AttributeInstance attr = animal.getAttribute(Attributes.SCALE);
        if (attr == null) return;
        ResourceLocation id = ResourceLocation.parse(BELLY_MODIFIER_ID);
        attr.removeModifier(id);
        if (progress < BELLY_START_PROGRESS) return;
        double span = 1.0 - BELLY_START_PROGRESS;
        double factor = Math.min(1.0, (progress - BELLY_START_PROGRESS) / span);
        attr.addTransientModifier(new AttributeModifier(
                id, BELLY_MAX_SCALE * factor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    public static void clearPregnancy(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        data.remove(NBT_PREGNANT);
        data.remove(NBT_PREGNANCY_START);
        data.remove(NBT_PREGNANCY_DURATION);
        data.remove(NBT_PREGNANCY_FATHER);
        data.remove(NBT_PREGNANCY_FATHER_TYPE);
        removeBellyScale(animal);
    }

    private static void removeBellyScale(Animal animal) {
        AttributeInstance attr = animal.getAttribute(Attributes.SCALE);
        if (attr == null) return;
        attr.removeModifier(ResourceLocation.parse(BELLY_MODIFIER_ID));
    }

    public static void tryMiscarriage(Animal animal) {
        if (animal.level().isClientSide) return;
        if (!isPregnant(animal)) return;
        if (animal.level().random.nextDouble() < Config.MISCARRIAGE_CHANCE.get()) {
            clearPregnancy(animal);
            setCooldown(animal);
        }
    }

    private static void giveBirth(Animal animal, CompoundTag data) {
        if (!(animal.level() instanceof ServerLevel serverLevel)) return;

        if (isOviparous(animal)) {
            layFertilizedEgg(serverLevel, animal);
        } else {
            AgeableMob baby = createOffspring(serverLevel, animal, data);
            if (baby != null) {
                baby.setBaby(true);
                baby.moveTo(animal.getX(), animal.getY(), animal.getZ(), animal.getYRot(), 0.0F);
                serverLevel.addFreshEntity(baby);
            }
        }

        clearPregnancy(animal);
        setCooldown(animal);
    }

    private static AgeableMob createOffspring(ServerLevel level, Animal mother, CompoundTag data) {
        Animal father = findFather(level, data);
        if (father != null) {
            return mother.getBreedOffspring(level, father);
        }
        EntityType<?> fatherType = fatherTypeOf(data);
        if (fatherType != null && isHorseDonkeyPair(mother.getType(), fatherType)
                && fatherType.create(level) instanceof AgeableMob dummy) {
            return mother.getBreedOffspring(level, dummy);
        }
        return mother.getBreedOffspring(level, mother);
    }

    private static EntityType<?> fatherTypeOf(CompoundTag data) {
        String id = data.getString(NBT_PREGNANCY_FATHER_TYPE);
        if (id.isEmpty()) return null;
        return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
    }

    private static Animal findFather(ServerLevel level, CompoundTag data) {
        if (!data.hasUUID(NBT_PREGNANCY_FATHER)) return null;
        UUID uuid = data.getUUID(NBT_PREGNANCY_FATHER);
        return level.getEntity(uuid) instanceof Animal animal ? animal : null;
    }

    public static boolean isOviparous(Animal animal) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        return key != null && Config.OVIPAROUS_MOBS.get().contains(key.toString());
    }

    private static void layFertilizedEgg(ServerLevel level, Animal mother) {
        ItemStack egg = new ItemStack(Items.EGG);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(NBT_FERTILIZED_EGG, true);
        CustomData.set(DataComponents.CUSTOM_DATA, egg, tag);
        ItemEntity itemEntity = new ItemEntity(level, mother.getX(), mother.getY() + 0.3, mother.getZ(), egg);
        level.addFreshEntity(itemEntity);
    }

    private static void setCooldown(Animal animal) {
        long cooldownTicks = (long) (Config.COOLDOWN_MINUTES.get() * 1200.0 * sizeFactor(animal));
        long dayTime = animal.level().getDayTime();
        animal.getPersistentData().putLong(NBT_COOLDOWN_UNTIL, dayTime + cooldownTicks);
    }

    public static boolean isFertilizedEgg(ItemStack stack) {
        if (!stack.is(Items.EGG)) return false;
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getBoolean(NBT_FERTILIZED_EGG);
    }

    public static boolean canLayFertilizedEgg(Animal hen) {
        if (hen.level().isClientSide) return false;
        if (hen.isBaby()) return false;
        if (hen.getAge() != 0) return false;
        return findNearbyMale(hen) != null;
    }

    private static double breedMinHungerRatio() {
        return Config.BREED_MIN_HUNGER_PERCENT.get() / 100.0;
    }
}