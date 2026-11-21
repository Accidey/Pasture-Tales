package com.xulai.animalflee.mixin.minecraft;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityEatParticlesAccessor {

    @Invoker("spawnItemParticles")
    void animalflee$spawnItemParticles(ItemStack stack, int amount);
}
