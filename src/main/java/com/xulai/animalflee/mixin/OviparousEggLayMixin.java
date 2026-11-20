package com.xulai.animalflee.mixin;

import com.xulai.animalflee.logic.OviparousRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class OviparousEggLayMixin {

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void animalflee$cancelThirdPartyEggLay(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        if (!((Object) this instanceof Animal animal) || (Object) this instanceof Chicken) return;
        if (!OviparousRegistry.isOviparous(animal)) return;
        if (!OviparousRegistry.isConfiguredEgg(stack.getItem())) return;
        cir.setReturnValue(null);
    }
}
