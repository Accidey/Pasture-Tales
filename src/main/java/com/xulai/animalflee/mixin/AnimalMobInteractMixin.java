package com.xulai.animalflee.mixin;

import com.xulai.animalflee.logic.AnimalFleeEntityHelper;
import com.xulai.animalflee.logic.HungerSystem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public class AnimalMobInteractMixin {

    private static final int POISON_DURATION_TICKS = 80;

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void animalflee$onMobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.level().isClientSide) return;
        Animal animal = (Animal) (Object) this;
        if (AnimalFleeEntityHelper.isExcluded(animal)) return;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }
        if (!HungerSystem.canEat(animal, stack)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        if (animal.isBaby()) {
            HungerSystem.feed(animal, stack);
            stack.shrink(1);
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        if (animal.getAge() != 0) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }
        if (!animal.canFallInLove()) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        if (stack.is(Items.POISONOUS_POTATO) && animal.getRandom().nextFloat() < 0.6F) {
            animal.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, 0));
        }

        HungerSystem.feed(animal, stack);
        stack.shrink(1);
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}