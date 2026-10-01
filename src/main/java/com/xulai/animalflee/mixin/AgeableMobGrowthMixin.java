package com.xulai.animalflee.mixin;

import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AgeableMob.class)
public class AgeableMobGrowthMixin {

    @Redirect(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AgeableMob;setAge(I)V")
    )
    private void animalflee$blockBabyAgeUp(AgeableMob self, int age) {
        if (age < 0) {
            return;
        }
        self.setAge(age);
    }
}