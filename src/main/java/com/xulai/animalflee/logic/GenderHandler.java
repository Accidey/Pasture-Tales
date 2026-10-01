package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class GenderHandler {

    private static final int SCALE_RECHECK_INTERVAL = 20;

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!AnimalFleeEntityHelper.isSupportedAnimal(event.getEntity())) return;
        GenderLogic.initGender((Animal) event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!AnimalFleeEntityHelper.isSupportedAnimal(event.getEntity())) return;
        Animal animal = (Animal) event.getEntity();
        if (animal.tickCount % SCALE_RECHECK_INTERVAL != 0) return;
        if (GenderLogic.isScaleApplied(animal)) return;
        GenderLogic.initGender(animal);
    }
}