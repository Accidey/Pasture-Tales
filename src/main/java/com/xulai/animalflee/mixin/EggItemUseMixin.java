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
import net.minecraft.world.phys.AABB;
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

    private static final String NBT_SLOT = "af_egg_slot";

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
                    BitSet occupied = collectOccupiedSlots(level, hay);
                    int placeCount = Math.min(stack.getCount(), 9);
                    int placed = 0;
                    for (int slot = 0; slot < 9 && placed < placeCount; slot++) {
                        if (occupied.get(slot)) continue;
                        ItemEntity egg = new ItemEntity(level,
                                slotX(hay, slot), hay.getY() + 1.0, slotZ(hay, slot),
                                stack.copyWithCount(1));
                        egg.setDeltaMovement(Vec3.ZERO);
                        egg.getPersistentData().putInt(NBT_SLOT, slot);
                        egg.setUnlimitedLifetime();
                        level.addFreshEntity(egg);
                        placed++;
                    }
                    stack.shrink(placed);
                }
                cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
                return;
            }
        }

        cir.setReturnValue(InteractionResultHolder.pass(stack));
    }

    public static boolean isGridEgg(ItemEntity egg) {
        return egg.getPersistentData().contains(NBT_SLOT);
    }

    public static int getSlot(ItemEntity egg) {
        return egg.getPersistentData().getInt(NBT_SLOT);
    }

    public static double slotX(BlockPos hay, int slot) {
        return hay.getX() + 0.5 + (slot % 3 - 1) * 0.25;
    }

    public static double slotZ(BlockPos hay, int slot) {
        return hay.getZ() + 0.5 + (slot / 3 - 1) * 0.25;
    }

    public static int findFreeSlot(Level level, BlockPos hay) {
        BitSet occupied = collectOccupiedSlots(level, hay);
        for (int slot = 0; slot < 9; slot++) {
            if (!occupied.get(slot)) return slot;
        }
        return -1;
    }

    public static BitSet collectOccupiedSlots(Level level, BlockPos hay) {
        BitSet occupied = new BitSet(9);
        for (int slot = 0; slot < 9; slot++) {
            double x = slotX(hay, slot);
            double z = slotZ(hay, slot);
            AABB box = new AABB(x - 0.3, hay.getY() + 0.8, z - 0.3,
                    x + 0.3, hay.getY() + 1.3, z + 0.3);
            for (ItemEntity egg : level.getEntitiesOfClass(ItemEntity.class, box)) {
                if (!isGridEgg(egg)) continue;
                if (getSlot(egg) == slot) {
                    occupied.set(slot);
                    break;
                }
            }
        }
        return occupied;
    }
}
