package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public final class BreedingEventBridge {

    private static EntityType<?> birthChildType = null;
    private static boolean birthDomesticated = false;

    private BreedingEventBridge() {
    }

    public static AgeableMob postBirth(Animal mother, Animal partner, AgeableMob child, ServerPlayer breeder, boolean domesticate) {
        EntityType<?> previousType = birthChildType;
        boolean previousDomesticated = birthDomesticated;
        birthChildType = child.getType();
        birthDomesticated = domesticate;
        try {
            BabyEntitySpawnEvent event = new AnimalFleeBabySpawnEvent(mother, partner, child, breeder);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                return null;
            }
            AgeableMob result = event.getChild();
            if (result != null && breeder != null) {
                breeder.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(breeder, mother, partner, result);
            }
            return result;
        } finally {
            birthChildType = previousType;
            birthDomesticated = previousDomesticated;
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (birthChildType == null) return;
        if (event.loadedFromDisk()) return;
        if (!(event.getEntity() instanceof Animal baby)) return;
        if (!baby.isBaby()) return;
        if (baby.getType() != birthChildType) return;
        if (birthDomesticated) {
            HungerSystem.setDomesticated(baby);
        }
    }
}
