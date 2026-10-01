package com.xulai.animalflee.integration.jade;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class AnimalFleeJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(AnimalStatusProvider.INSTANCE, Animal.class);
        registration.registerEntityDataProvider(EggIncubationProvider.INSTANCE, ItemEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(AnimalStatusProvider.INSTANCE, Animal.class);
        registration.registerEntityComponent(EggIncubationProvider.INSTANCE, ItemEntity.class);
        registration.markAsClientFeature(AnimalStatusProvider.UID);
        registration.markAsClientFeature(EggIncubationProvider.UID);
    }
}