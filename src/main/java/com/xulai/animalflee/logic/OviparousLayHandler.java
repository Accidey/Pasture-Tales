package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class OviparousLayHandler {

    private static final long DAY_TICKS = 24000L;
    private static final long HOUR_TICKS = 1000L;
    private static final long MIN_FLOAT = 1L * HOUR_TICKS;
    private static final long MAX_FLOAT = 3L * HOUR_TICKS;
    private static final int TICK_INTERVAL = 100;
    private static final String NBT_NEXT_EGG = "af_next_egg_daytime";

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Animal animal)) return;
        if (!AnimalFleeEntityHelper.isSupportedAnimal(animal)) return;
        if (animal.tickCount % TICK_INTERVAL != 0) return;
        tickLay(animal);
    }

    private static void tickLay(Animal animal) {
        if (animal instanceof Chicken) return;
        if (!OviparousRegistry.isOviparous(animal)) return;
        if (animal.isBaby()) return;
        if (!GenderLogic.isFemale(animal)) return;
        if (animal.getAge() != 0) return;

        CompoundTag data = animal.getPersistentData();
        long dayTime = animal.level().getDayTime();
        if (!data.contains(NBT_NEXT_EGG)) {
            data.putLong(NBT_NEXT_EGG, nextEggInterval(animal, dayTime));
            return;
        }
        if (dayTime < data.getLong(NBT_NEXT_EGG)) return;

        data.putLong(NBT_NEXT_EGG, nextEggInterval(animal, dayTime));
        layEgg(animal);
    }

    private static void layEgg(Animal animal) {
        if (!(animal.level() instanceof ServerLevel level)) return;
        ItemStack egg = OviparousRegistry.createEgg(animal);
        if (egg.isEmpty()) return;

        if (HungerSystem.isDomesticated(animal)) {
            Animal mate = PregnancySystem.findLayMate(animal);
            if (mate != null) {
                PregnancySystem.markFertilizedEgg(egg, animal, mate);
            }
        }

        ItemEntity itemEntity = new ItemEntity(level,
                animal.getX(), animal.getY() + 0.3, animal.getZ(), egg);
        level.addFreshEntity(itemEntity);
    }

    private static long nextEggInterval(Animal animal, long dayTime) {
        long range = MAX_FLOAT - MIN_FLOAT + 1L;
        long randomFloat = MIN_FLOAT + animal.getRandom().nextInt((int) range);
        return dayTime + DAY_TICKS + randomFloat;
    }
}