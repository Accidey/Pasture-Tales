package com.xulai.animalflee.integration.jade;

import com.xulai.animalflee.registry.FeedingTroughBlock;
import com.xulai.animalflee.registry.FeedingTroughBlockEntity;
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
        registration.registerBlockDataProvider(TroughFoodProvider.INSTANCE, FeedingTroughBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(AnimalStatusProvider.INSTANCE, Animal.class);
        registration.registerEntityComponent(EggIncubationProvider.INSTANCE, ItemEntity.class);
        registration.registerBlockComponent(TroughFoodProvider.INSTANCE, FeedingTroughBlock.class);
        registration.markAsClientFeature(AnimalStatusProvider.UID);
        registration.markAsClientFeature(EggIncubationProvider.UID);
        registration.markAsClientFeature(TroughFoodProvider.UID);
    }
}