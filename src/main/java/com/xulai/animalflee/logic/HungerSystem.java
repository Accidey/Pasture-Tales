package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class HungerSystem {

    public static final String NBT_DOMESTICATED = "af_domesticated";
    public static final String NBT_HUNGER = "af_hunger";
    public static final String NBT_MAX_HUNGER = "af_max_hunger";
    public static final String NBT_METABOLISM = "af_metabolism";
    public static final String NBT_HUNGER_LAST_DAY_TIME = "af_hunger_last_daytime";
    public static final String NBT_STARVE_DAY_TIME = "af_starve_start_daytime";
    public static final String NBT_LAST_FED_BY = "af_last_fed_by";

    public static final float MAX_HUNGER = 20.0F;
    public static final float MIN_HUNGER = 0.0F;
    private static final float PLAYER_REFERENCE_VOLUME = 0.9F * 1.4F;
    private static final float MIN_CAPACITY = 8.0F;
    private static final float PREGNANT_HUNGER_MULTIPLIER = 1.5F;
    private static final float FULL_HUNGER_EPSILON = 1.0F;
    private static final int TICK_INTERVAL = 20;
    private static final int STARVE_DAMAGE_INTERVAL = 20;
    private static final int STARVE_PARTICLE_INTERVAL = 40;
    private static final int HEAL_INTERVAL_TICKS = 40;
    private static final int HEAL_PARTICLE_INTERVAL = 80;
    private static final float HEAL_HUNGER_RATIO = 0.8F;
    private static final long DAY_TICKS = 24000L;
    private static final float METABOLISM_MIN = 0.8F;
    private static final float METABOLISM_MAX = 1.2F;
    private static final float WILD_INITIAL_HUNGER_MIN = 0.5F;
    private static final float WILD_INITIAL_HUNGER_MAX = 0.8F;

    private HungerSystem() {
    }

    public static boolean isDomesticated(Mob mob) {
        return mob.getPersistentData().getBoolean(NBT_DOMESTICATED);
    }

    public static UUID getLastFedBy(Animal animal) {
        CompoundTag data = animal.getPersistentData();
        return data.hasUUID(NBT_LAST_FED_BY) ? data.getUUID(NBT_LAST_FED_BY) : null;
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
        float max = getMaxHunger(animal);
        if (hunger >= max - 0.001F) {
            hunger = max;
        }
        animal.getPersistentData().putFloat(NBT_HUNGER, Math.max(MIN_HUNGER, Math.min(max, hunger)));
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
            float max = getMaxHunger(animal);
            float ratio = WILD_INITIAL_HUNGER_MIN
                    + ThreadLocalRandom.current().nextFloat()
                    * (WILD_INITIAL_HUNGER_MAX - WILD_INITIAL_HUNGER_MIN);
            data.putFloat(NBT_HUNGER, max * ratio);
        }
        if (!data.contains(NBT_HUNGER_LAST_DAY_TIME)) {
            data.putLong(NBT_HUNGER_LAST_DAY_TIME, animal.level().getDayTime());
        }
    }

    public static boolean canEat(Animal animal, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (getHunger(animal) >= getMaxHunger(animal) - FULL_HUNGER_EPSILON) return false;
        if (AnimalFoods.getSaturation(animal, stack) <= 0.0F) return false;
        return AnimalFoods.canEatByDiet(animal, stack);
    }

    public static float feed(Animal animal, ItemStack stack, Player feeder) {
        initHunger(animal);
        setDomesticated(animal);
        if (feeder != null) {
            animal.getPersistentData().putUUID(NBT_LAST_FED_BY, feeder.getUUID());
        }
        MobFleeLogic.stopFlee(animal);
        float current = getHunger(animal);
        float restored = AnimalFoods.getSaturation(animal, stack);
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
            } else if (hunger >= getMaxHunger(animal) * HEAL_HUNGER_RATIO
                    && animal.getHealth() < animal.getMaxHealth()) {
                if (animal.tickCount % HEAL_INTERVAL_TICKS == 0) {
                    animal.heal(1.0F);
                }
                if (animal.tickCount % HEAL_PARTICLE_INTERVAL == 0) {
                    spawnHealParticles(animal);
                }
            }
        } else {
            long starveStart = data.getLong(NBT_STARVE_DAY_TIME);
            int starveDays = Config.HUNGER_STARVE_DAYS.get();
            long starveDuration = starveDays * DAY_TICKS;
            long starveElapsed = dayTime - starveStart;
            if (starveElapsed < starveDuration) {
                float damage = animal.getMaxHealth() / (starveDuration / (float) STARVE_DAMAGE_INTERVAL);
                animal.setHealth(Math.max(0.0F, animal.getHealth() - damage));
                PregnancySystem.tryMiscarriage(animal);
                if (animal.tickCount % STARVE_PARTICLE_INTERVAL == 0) {
                    spawnStarveParticles(animal);
                }
            }
        }
    }

    private static void spawnStarveParticles(Animal animal) {
        if (!(animal.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(
                ParticleTypes.ANGRY_VILLAGER,
                animal.getX(),
                animal.getY() + animal.getBbHeight() + 0.5D,
                animal.getZ(),
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D);
    }

    private static void spawnHealParticles(Animal animal) {
        if (!(animal.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(
                ParticleTypes.HEART,
                animal.getX(),
                animal.getY() + animal.getBbHeight() + 0.5D,
                animal.getZ(),
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D);
    }

    private static float calculateCapacity(Animal animal) {
        EntityDimensions dims = animal.getType().getDimensions();
        float volume = dims.width() * dims.height();
        if (volume <= 0.0F) return MAX_HUNGER;
        float capacity = MAX_HUNGER * (volume / PLAYER_REFERENCE_VOLUME);
        return Math.max(MIN_CAPACITY, capacity);
    }
}