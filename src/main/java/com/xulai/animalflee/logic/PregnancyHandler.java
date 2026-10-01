package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class PregnancyHandler {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!AnimalFleeEntityHelper.isSupportedAnimal(event.getEntity())) return;
        PregnancySystem.tickPregnancy((Animal) event.getEntity());
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!AnimalFleeEntityHelper.isSupportedAnimal(event.getEntity())) return;
        if (event.getAmount() <= 0.0F) return;
        PregnancySystem.tryMiscarriage((Animal) event.getEntity());
    }
}