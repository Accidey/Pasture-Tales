package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.BitSet;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class EggPlaceHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());
        if (!PregnancySystem.isFertilizedEgg(stack)) return;

        HitResult hit = player.pick(5.0, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult blockHit)) return;
        if (blockHit.getDirection() != Direction.UP) return;

        BlockPos hay = blockHit.getBlockPos();
        if (!level.getBlockState(hay).is(Blocks.HAY_BLOCK)
                || !level.getBlockState(hay.above()).canBeReplaced()) return;

        ItemEntity existing = EggGridHelper.findGridEgg(level, hay, hay.getX() + 0.5, hay.getZ() + 0.5);
        if (existing != null) return;

        if (level.isClientSide) return;

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
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}