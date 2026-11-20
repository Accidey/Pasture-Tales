package com.xulai.animalflee.mixin.untitledduck;

import com.xulai.animalflee.logic.OviparousRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Animal;
import net.untitledduckmod.compat.jade.provider.LayEggProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.EntityAccessor;

@Mixin(LayEggProvider.class)
public class LayEggProviderMixin {

    @Inject(method = "appendServerData", at = @At("HEAD"), cancellable = true)
    private void animalflee$hideSupersededEggTimer(CompoundTag tag, EntityAccessor accessor, CallbackInfo ci) {
        if (accessor.getEntity() instanceof Animal animal && OviparousRegistry.isOviparous(animal)) {
            ci.cancel();
        }
    }
}
