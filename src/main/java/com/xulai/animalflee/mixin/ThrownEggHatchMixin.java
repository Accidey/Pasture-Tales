package com.xulai.animalflee.mixin;

import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.PregnancySystem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.ThrownEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownEgg.class)
public class ThrownEggHatchMixin {

    @Redirect(
            method = "onHit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0)
    )
    private int animalflee$hatchChance(RandomSource random, int bound) {
        ThrownEgg self = (ThrownEgg) (Object) this;
        if (PregnancySystem.isFertilizedEgg(self.getItem())) {
            return 1;
        }
        if (Config.BLOCK_NORMAL_EGG_HATCHING.get()) {
            return 1;
        }
        return random.nextInt(bound);
    }
}