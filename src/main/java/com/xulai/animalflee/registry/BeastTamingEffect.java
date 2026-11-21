package com.xulai.animalflee.registry;

import com.xulai.animalflee.Config;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class BeastTamingEffect extends MobEffect {

    public BeastTamingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB08A55);
    }

    public static double factor(Player player) {
        MobEffectInstance instance = player.getEffect(PotionEffects.BEAST_TAMING_EFFECT);
        if (instance == null) return 1.0D;
        int amplifier = Math.min(instance.getAmplifier(), 2);
        int reduction = switch (amplifier) {
            case 0 -> Config.BEAST_TAMING_REDUCTION_1.get();
            case 1 -> Config.BEAST_TAMING_REDUCTION_2.get();
            default -> Config.BEAST_TAMING_REDUCTION_3.get();
        };
        return Math.max(0.0D, 1.0D - reduction / 100.0D);
    }
}
