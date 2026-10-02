package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public final class OviparousRegistry {

    private static volatile Map<String, String> eggItemCache = Map.of();
    private static volatile boolean cacheReady = false;

    private OviparousRegistry() {
    }

    public static void resetCache() {
        eggItemCache = buildCache();
        cacheReady = true;
    }

    private static Map<String, String> buildCache() {
        Map<String, String> map = new HashMap<>();
        for (String entry : Config.OVIPAROUS_MOBS.get()) {
            String[] parts = entry.split(",", 2);
            if (parts.length != 2) continue;
            String mobId = parts[0].trim();
            String eggId = parts[1].trim();
            if (mobId.isEmpty() || eggId.isEmpty()) continue;
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(mobId))
                    && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(eggId))) {
                map.put(mobId, eggId);
            }
        }
        return Map.copyOf(map);
    }

    private static void ensureInitialized() {
        if (!cacheReady || (eggItemCache.isEmpty() && !Config.OVIPAROUS_MOBS.get().isEmpty())) {
            resetCache();
        }
    }

    public static boolean isOviparous(Animal animal) {
        ensureInitialized();
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        if (key == null) return false;
        return eggItemCache.containsKey(key.toString());
    }

    public static boolean isOviparous(EntityType<?> type) {
        ensureInitialized();
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return key != null && eggItemCache.containsKey(key.toString());
    }

    public static ItemStack createEgg(Animal mother) {
        ensureInitialized();
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(mother.getType());
        if (key == null) return ItemStack.EMPTY;
        String eggId = eggItemCache.get(key.toString());
        if (eggId == null) return ItemStack.EMPTY;
        ResourceLocation eggKey = ResourceLocation.parse(eggId);
        Item item = BuiltInRegistries.ITEM.get(eggKey);
        if (item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(item);
    }

    public static String getEggItemId(Animal animal) {
        ensureInitialized();
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        if (key == null) return null;
        return eggItemCache.get(key.toString());
    }
}