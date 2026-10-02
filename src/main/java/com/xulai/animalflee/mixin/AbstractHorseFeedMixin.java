package com.xulai.animalflee.mixin;

import com.xulai.animalflee.logic.HungerSystem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public class AbstractHorseFeedMixin {

    private static final int POISON_DURATION_TICKS = 80;

    @Inject(method = "fedFood", at = @At("HEAD"), cancellable = true)
    private void animalflee$fedFood(Player player, ItemStack stack, CallbackInfoReturnable<InteractionResult> cir) {
        AbstractHorse horse = (AbstractHorse) (Object) this;
        if (horse.level().isClientSide) return;
        if (horse.isBaby()) {
            if (!HungerSystem.canEat(horse, stack)) {
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }
            HungerSystem.feed(horse, stack);
            stack.consume(1, player);
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }
        if (!HungerSystem.canEat(horse, stack)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        float heal = 0.0F;
        int temper = 0;
        if (stack.is(Items.WHEAT)) {
            heal = 2.0F;
            temper = 3;
        } else if (stack.is(Items.SUGAR)) {
            heal = 1.0F;
            temper = 3;
        } else if (stack.is(Blocks.HAY_BLOCK.asItem())) {
            heal = 20.0F;
        } else if (stack.is(Items.APPLE)) {
            heal = 3.0F;
            temper = 3;
        } else if (stack.is(Items.GOLDEN_CARROT)) {
            heal = 4.0F;
            temper = 5;
        } else if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            heal = 10.0F;
            temper = 10;
        }

        if (stack.is(Items.POISONOUS_POTATO) && horse.getRandom().nextFloat() < 0.6F) {
            horse.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, 0));
        }

        if (heal > 0.0F && horse.getHealth() < horse.getMaxHealth()) {
            horse.heal(heal);
        }
        if (temper > 0 && horse.getTemper() < horse.getMaxTemper()) {
            horse.modifyTemper(temper);
        }

        HungerSystem.feed(horse, stack);
        stack.consume(1, player);
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}