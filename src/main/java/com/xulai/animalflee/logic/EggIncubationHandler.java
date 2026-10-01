package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import com.xulai.animalflee.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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

    private static boolean isIncubating(ItemEntity egg) {
        BlockPos pos = egg.blockPosition();
        BlockState here = egg.level().getBlockState(pos);
        BlockState below = egg.level().getBlockState(pos.below());
        if (!below.is(Blocks.HAY_BLOCK) && !here.is(Blocks.HAY_BLOCK)) {
            return false;
        }
        return !egg.level().canSeeSky(pos);
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