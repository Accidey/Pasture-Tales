package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import com.xulai.animalflee.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class EggIncubationHandler {

    private static final String NBT_ELAPSED = "af_incubation";
    private static final String NBT_LAST_DAY_TIME = "af_incubation_last";
    private static final long MAX_STEP = 2400L;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (!PregnancySystem.isFertilizedEgg(stack)) return;

        CompoundTag data = itemEntity.getPersistentData();
        if (!isIncubating(itemEntity)) {
            data.remove(NBT_ELAPSED);
            data.remove(NBT_LAST_DAY_TIME);
            return;
        }

        itemEntity.setUnlimitedLifetime();
        holdInSlot(itemEntity);

        long dayTime = itemEntity.level().getDayTime();
        long last = data.contains(NBT_LAST_DAY_TIME) ? data.getLong(NBT_LAST_DAY_TIME) : dayTime;
        long delta = dayTime - last;
        if (delta < 0) delta = 0;
        delta = Math.min(delta, MAX_STEP);

        long elapsed = data.getLong(NBT_ELAPSED) + delta;
        data.putLong(NBT_LAST_DAY_TIME, dayTime);
        data.putLong(NBT_ELAPSED, elapsed);

        if (elapsed >= Config.EGG_INCUBATION_MINUTES.get() * 1200L) {
            hatch(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onPickupPre(ItemEntityPickupEvent.Pre event) {
        ItemEntity egg = event.getItemEntity();
        if (egg.level().isClientSide) return;
        if (!isIncubatingEgg(egg)) return;
        event.setCanPickup(TriState.FALSE);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getTarget() instanceof ItemEntity egg)) return;
        if (!isIncubatingEgg(egg)) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        Player player = event.getEntity();
        ItemStack stack = egg.getItem().copy();
        egg.discard();
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getHitVec() instanceof BlockHitResult blockHit)) return;
        if (blockHit.getDirection() != Direction.UP) return;

        BlockPos hay = blockHit.getBlockPos();
        if (!event.getLevel().getBlockState(hay).is(Blocks.HAY_BLOCK)) return;

        ItemEntity existing = EggGridHelper.findGridEgg(event.getLevel(), hay,
                hay.getX() + 0.5, hay.getZ() + 0.5);
        if (existing == null) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        Player player = event.getEntity();
        ItemStack picked = existing.getItem().copy();
        existing.discard();
        if (!player.addItem(picked)) {
            player.drop(picked, false);
        }
    }

    private static boolean isIncubatingEgg(ItemEntity egg) {
        ItemStack stack = egg.getItem();
        if (!PregnancySystem.isFertilizedEgg(stack)) return false;
        return isIncubating(egg);
    }

    private static boolean isIncubating(ItemEntity egg) {
        BlockPos pos = egg.blockPosition();
        BlockState here = egg.level().getBlockState(pos);
        BlockState below = egg.level().getBlockState(pos.below());
        if (!below.is(Blocks.HAY_BLOCK) && !here.is(Blocks.HAY_BLOCK)) {
            return false;
        }
        return !egg.level().canSeeSky(pos);
    }

    private static void holdInSlot(ItemEntity egg) {
        if (!EggGridHelper.isGridEgg(egg)) return;
        BlockPos pos = egg.blockPosition();
        BlockState below = egg.level().getBlockState(pos.below());
        BlockState here = egg.level().getBlockState(pos);
        BlockPos hay;
        if (below.is(Blocks.HAY_BLOCK)) {
            hay = pos.below();
        } else if (here.is(Blocks.HAY_BLOCK)) {
            hay = pos;
        } else {
            return;
        }
        int slot = EggGridHelper.getSlot(egg);
        if (slot < 0 || slot >= EggGridHelper.GRID_SIZE) {
            slot = EggGridHelper.findFreeSlot(egg.level(), hay);
            if (slot < 0) return;
            EggGridHelper.setSlot(egg, slot);
        }
        double x = EggGridHelper.slotX(hay, slot);
        double z = EggGridHelper.slotZ(hay, slot);
        egg.setPos(x, hay.getY() + 1.0, z);
        egg.setDeltaMovement(0.0, 0.0, 0.0);
    }

    private static void hatch(ItemEntity egg) {
        if (!(egg.level() instanceof ServerLevel level)) return;
        Chicken chick = EntityType.CHICKEN.create(level);
        if (chick != null) {
            chick.setAge(-24000);
            chick.moveTo(egg.getX(), egg.getY(), egg.getZ(), egg.getYRot(), 0.0F);
            level.addFreshEntity(chick);
        }
        egg.discard();
    }
}