package com.xulai.animalflee.mixin;

import com.xulai.animalflee.logic.EggGridHelper;
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

import java.util.BitSet;

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
            if (!level.getBlockState(hay).is(Blocks.HAY_BLOCK)
                    || !level.getBlockState(hay.above()).canBeReplaced()) {
                cir.setReturnValue(InteractionResultHolder.pass(stack));
                return;
            }

            if (!level.isClientSide) {
                ItemEntity existing = EggGridHelper.findGridEgg(level, hay,
                        hay.getX() + 0.5, hay.getZ() + 0.5);
                if (existing != null) {
                    ItemStack picked = existing.getItem().copy();
                    existing.discard();
                    if (!player.addItem(picked)) {
                        player.drop(picked, false);
                    }
                    cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
                    return;
                }

                BitSet occupied = EggGridHelper.collectOccupiedSlots(level, hay);
                int placeCount = Math.min(stack.getCount(), EggGridHelper.GRID_SIZE);
                int placed = 0;
                for (int slot = 0; slot < EggGridHelper.GRID_SIZE && placed < placeCount; slot++) {
                    if (occupied.get(slot)) continue;
                    ItemEntity egg = new ItemEntity(level,
                            EggGridHelper.slotX(hay, slot), hay.getY() + 1.0, EggGridHelper.slotZ(hay, slot),
                            stack.copyWithCount(1));
                    egg.setDeltaMovement(Vec3.ZERO);
                    EggGridHelper.setSlot(egg, slot);
                    egg.setUnlimitedLifetime();
                    level.addFreshEntity(egg);
                    placed++;
                }
                stack.shrink(placed);
            }
            cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
            return;
        }

        cir.setReturnValue(InteractionResultHolder.pass(stack));
    }
}