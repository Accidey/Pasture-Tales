package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;

import java.util.HashSet;
import java.util.Set;

public final class AnimalFleeEntityHelper {

    private static final String MAID_NAMESPACE = "touhou_little_maid";
    private static Set<String> cachedExcluded = null;

    private AnimalFleeEntityHelper() {
    }

    public static boolean isMaidEntity(Entity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && MAID_NAMESPACE.equals(key.getNamespace());
    }

    public static boolean isExcluded(Entity entity) {
        if (cachedExcluded == null) {
            cachedExcluded = new HashSet<>(Config.EXCLUDED_MOBS.get());
        }
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && cachedExcluded.contains(key.toString());
    }

    public static void resetExcludedCache() {
        cachedExcluded = null;
    }

    public static boolean isSupportedAnimal(Entity entity) {
        if (isExcluded(entity)) return false;
        return entity instanceof Animal && !isMaidEntity(entity);
    }
}