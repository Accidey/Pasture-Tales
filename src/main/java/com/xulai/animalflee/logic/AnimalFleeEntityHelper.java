package com.xulai.animalflee.logic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;

public final class AnimalFleeEntityHelper {

    private static final String MAID_NAMESPACE = "touhou_little_maid";

    private AnimalFleeEntityHelper() {
    }

    public static boolean isMaidEntity(Entity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && MAID_NAMESPACE.equals(key.getNamespace());
    }

    public static boolean isSupportedAnimal(Entity entity) {
        return entity instanceof Animal && !isMaidEntity(entity);
    }
}