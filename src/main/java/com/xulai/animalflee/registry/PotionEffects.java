package com.xulai.animalflee.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PotionEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "pasture-tales");
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, "pasture-tales");

    public static final DeferredHolder<MobEffect, BeastTamingEffect> BEAST_TAMING_EFFECT =
            EFFECTS.register("beast_taming", BeastTamingEffect::new);

    public static final DeferredHolder<Potion, Potion> BEAST_TAMING =
            POTIONS.register("beast_taming", () -> new Potion("beast_taming", new MobEffectInstance(BEAST_TAMING_EFFECT, 3600, 0)));
    public static final DeferredHolder<Potion, Potion> BEAST_TAMING_2 =
            POTIONS.register("beast_taming_2", () -> new Potion("beast_taming_2", new MobEffectInstance(BEAST_TAMING_EFFECT, 6000, 1)));
    public static final DeferredHolder<Potion, Potion> BEAST_TAMING_3 =
            POTIONS.register("beast_taming_3", () -> new Potion("beast_taming_3", new MobEffectInstance(BEAST_TAMING_EFFECT, 9600, 2)));

    private PotionEffects() {
    }

    public static void init(IEventBus modBus) {
        EFFECTS.register(modBus);
        POTIONS.register(modBus);
    }
}
