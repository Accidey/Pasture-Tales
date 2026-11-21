package com.xulai.animalflee.registry;

import com.xulai.animalflee.Config;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BeastTamingEffect extends MobEffect {

    public BeastTamingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB08A55);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }

    public static double factor(Player player) {
        MobEffectInstance instance = player.getEffect(ModRegistry.BEAST_TAMING);
        if (instance == null) return 1.0D;
        int amplifier = Math.min(instance.getAmplifier(), 2);
        int reduction = switch (amplifier) {
            case 0 -> Config.BEAST_TAMING_REDUCTION_1.get();
            case 1 -> Config.BEAST_TAMING_REDUCTION_2.get();
            default -> Config.BEAST_TAMING_REDUCTION_3.get();
        };
        return Math.max(0.0D, 1.0D - reduction / 100.0D);
    }

    public static int durationTicks(int tier) {
        int minutes = switch (tier) {
            case 1 -> Config.BEAST_TAMING_MINUTES_1.get();
            case 2 -> Config.BEAST_TAMING_MINUTES_2.get();
            default -> Config.BEAST_TAMING_MINUTES_3.get();
        };
        return minutes * 1200;
    }
}
