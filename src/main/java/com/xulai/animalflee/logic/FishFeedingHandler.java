package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class FishFeedingHandler {

    @SubscribeEvent
    public static void onItemEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;
        if (!itemEntity.isAlive()) return;
        if (!itemEntity.isInWater()) return;
        ItemStack stack = itemEntity.getItem();
        if (!isFishBait(stack)) return;

        ServerLevel level = (ServerLevel) itemEntity.level();
        BlockPos pos = itemEntity.blockPosition();
        long startDayTime = level.getDayTime();
        int targetDelta = 12000 + level.random.nextInt(12001);
        int count = 2 + level.random.nextInt(4);
        FishFeedingSavedData.get(level).addEntry(pos, startDayTime, targetDelta, count);
        itemEntity.discard();
    }

    public static boolean isFishBait(ItemStack stack) {
        if (stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)) {
            return true;
        }
        if (stack.is(Items.COCOA_BEANS) || stack.is(Items.PUMPKIN_SEEDS) || stack.is(Items.MELON_SEEDS)) {
            return true;
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof CropBlock || blockItem.getBlock() instanceof NetherWartBlock) {
                return true;
            }
        }
        return stack.has(DataComponents.FOOD) && stack.getUseAnimation() == UseAnim.EAT;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            FishFeedingSavedData.get(level).tick(level);
        }
    }
}