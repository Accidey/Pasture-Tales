package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import com.xulai.animalflee.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class OviparousRegistry {

    private static volatile Map<String, String> eggItemCache = Map.of();
    private static volatile Set<Item> eggItems = Set.of();
    private static volatile boolean cacheReady = false;
    private static final Set<String> warnedEntries = new HashSet<>();

    private OviparousRegistry() {
    }

    public static void resetCache() {
        Map<String, String> map = new HashMap<>();
        Set<Item> items = new HashSet<>();
        for (String entry : Config.OVIPAROUS_MOBS.get()) {
            String[] parts = entry.split(",", 2);
            String mobId = parts.length == 2 ? parts[0].trim() : "";
            String eggId = parts.length == 2 ? parts[1].trim() : "";
            ResourceLocation mobKey = mobId.isEmpty() ? null : ResourceLocation.tryParse(mobId);
            ResourceLocation eggKey = eggId.isEmpty() ? null : ResourceLocation.tryParse(eggId);
            if (mobKey != null && eggKey != null
                    && BuiltInRegistries.ENTITY_TYPE.containsKey(mobKey)
                    && BuiltInRegistries.ITEM.containsKey(eggKey)) {
                map.put(mobId, eggId);
                items.add(BuiltInRegistries.ITEM.get(eggKey));
            } else {
                warnInvalid(entry);
            }
        }
        eggItemCache = Map.copyOf(map);
        eggItems = Set.copyOf(items);
        cacheReady = true;
    }

    private static void warnInvalid(String entry) {
        if (warnedEntries.add(entry)) {
            AnimalFleeMod.LOGGER.warn("[AnimalFlee] Skipping invalid oviparousMobs entry: {}", entry);
        }
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

    public static boolean isConfiguredEgg(Item item) {
        ensureInitialized();
        return eggItems.contains(item);
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
