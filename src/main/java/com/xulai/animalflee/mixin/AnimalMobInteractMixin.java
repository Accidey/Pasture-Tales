package com.xulai.animalflee.mixin;

import com.xulai.animalflee.logic.HungerSystem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public class AnimalMobInteractMixin {

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void animalflee$onMobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.level().isClientSide) return;
        Animal animal = (Animal) (Object) this;
        if (animal.isBaby()) return;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return;
        if (!HungerSystem.canEat(animal, stack)) return;
        if (animal.getAge() != 0) return;
        if (!animal.canFallInLove()) return;

        HungerSystem.feed(animal, stack);
        stack.shrink(1);
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}