package com.xulai.animalflee.registry;

import java.util.List;

import com.xulai.animalflee.Config;
import com.xulai.animalflee.mixin.minecraft.PotionAccessor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class PotionPatcher {

    private static boolean patched;

    private PotionPatcher() {
    }

    public static void apply() {
        patch(PotionEffects.BEAST_TAMING, 1);
        patch(PotionEffects.BEAST_TAMING_2, 2);
        patch(PotionEffects.BEAST_TAMING_3, 3);
        patched = true;
    }

    public static boolean isPatched() {
        return patched;
    }

    private static void patch(DeferredHolder<Potion, Potion> potion, int tier) {
        if (!potion.isBound()) return;
        int minutes = switch (tier) {
            case 1 -> Config.BEAST_TAMING_MINUTES_1.get();
            case 2 -> Config.BEAST_TAMING_MINUTES_2.get();
            default -> Config.BEAST_TAMING_MINUTES_3.get();
        };
        List<MobEffectInstance> effects = List.of(new MobEffectInstance(
                PotionEffects.BEAST_TAMING_EFFECT, minutes * 1200, tier - 1));
        ((PotionAccessor) (Object) potion.value()).animalflee$setEffects(effects);
    }
}
