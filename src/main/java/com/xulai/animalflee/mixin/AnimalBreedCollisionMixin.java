package com.xulai.animalflee.mixin;

import com.xulai.animalflee.logic.PregnancySystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class AnimalBreedCollisionMixin {

    @Inject(
            method = "spawnChildFromBreeding(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void animalflee$conceiveInsteadOfBirth(ServerLevel level, Animal other, CallbackInfo ci) {
        Animal caller = (Animal) (Object) this;
        if (PregnancySystem.handleBreedingCollision(level, caller, other)) {
            ci.cancel();
        }
    }
}
