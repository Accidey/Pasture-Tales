package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class GrowthHandler {

    private static final int TICK_INTERVAL = 100;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Animal animal)) return;
        if (animal instanceof Turtle) return;
        if (AnimalFleeEntityHelper.isMaidEntity(animal)) return;
        if (animal.tickCount % TICK_INTERVAL != 0) return;
        GrowthSystem.tickGrowth(animal);
    }
}