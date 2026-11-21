package com.xulai.animalflee.logic;

import com.xulai.animalflee.mixin.minecraft.LivingEntityEatParticlesAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;

public final class AnimalEatAnimation {

    private AnimalEatAnimation() {
    }

    public static void playBite(Animal animal, ItemStack food) {
        if (animal instanceof Sheep) {
            animal.level().broadcastEntityEvent(animal, (byte) 10);
        } else if (animal instanceof AbstractHorse horse) {
            horse.setEating(true);
        }
        if (animal.level() instanceof ServerLevel serverLevel) {
            ((LivingEntityEatParticlesAccessor) animal).animalflee$spawnItemParticles(food, 5);
            animal.playSound(
                    animal.getEatingSound(food),
                    0.5F + 0.5F * animal.getRandom().nextInt(2),
                    (animal.getRandom().nextFloat() - animal.getRandom().nextFloat()) * 0.2F + 1.0F);
        }
    }

    public static void stop(Animal animal) {
        if (animal instanceof AbstractHorse horse) {
            horse.setEating(false);
        }
    }
}
