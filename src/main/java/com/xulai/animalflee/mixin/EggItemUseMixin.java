package com.xulai.animalflee.mixin;

import com.xulai.animalflee.logic.PregnancySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EggItem.class)
public class EggItemUseMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void animalflee$placeOnHay(Level level, Player player, InteractionHand hand,
                                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (!PregnancySystem.isFertilizedEgg(stack)) return;

        HitResult hit = player.pick(5.0, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit
                && blockHit.getDirection() == Direction.UP) {
            BlockPos hay = blockHit.getBlockPos();
            if (level.getBlockState(hay).is(Blocks.HAY_BLOCK)
                    && level.getBlockState(hay.above()).canBeReplaced()) {
                if (!level.isClientSide) {
                    ItemEntity egg = new ItemEntity(level,
                            hay.getX() + 0.5, hay.getY() + 1.0, hay.getZ() + 0.5,
                            stack.copyWithCount(1));
                    egg.setDeltaMovement(Vec3.ZERO);
                    level.addFreshEntity(egg);
                    stack.shrink(1);
                }
                cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
                return;
            }
        }

        cir.setReturnValue(InteractionResultHolder.pass(stack));
    }
}