package com.xulai.animalflee.registry;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistry {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, AnimalFleeMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, AnimalFleeMod.MODID);

    public static final DeferredHolder<MobEffect, BeastTamingEffect> BEAST_TAMING = MOB_EFFECTS.register("beast_taming", BeastTamingEffect::new);

    public static final DeferredHolder<Item, BeastTamingPotionItem> BEAST_TAMING_POTION_1 = ITEMS.register(
            "beast_taming_potion", () -> new BeastTamingPotionItem(1, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, BeastTamingPotionItem> BEAST_TAMING_POTION_2 = ITEMS.register(
            "beast_taming_potion_2", () -> new BeastTamingPotionItem(2, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, BeastTamingPotionItem> BEAST_TAMING_POTION_3 = ITEMS.register(
            "beast_taming_potion_3", () -> new BeastTamingPotionItem(3, new Item.Properties().stacksTo(1)));

    private ModRegistry() {
    }

    public static void init(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
        ITEMS.register(modBus);
    }
}
