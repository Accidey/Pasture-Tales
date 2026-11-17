package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import com.xulai.animalflee.Config;
import com.xulai.animalflee.integration.sereneseasons.SereneSeasonsCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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

    private static final String BAIT_TICKS_TAG = "af_bait_ticks";
    private static final int BAIT_DELAY_TICKS = 200;

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
        if (Config.WINTER_BLOCKS_BAIT.get() && SereneSeasonsCompat.isWinter(level, pos)) return;

        CompoundTag data = itemEntity.getPersistentData();
        int soaked = data.getInt(BAIT_TICKS_TAG) + 1;
        if (soaked < BAIT_DELAY_TICKS) {
            data.putInt(BAIT_TICKS_TAG, soaked);
            return;
        }

        long startDayTime = level.getDayTime();
        int targetDelta = 12000 + level.random.nextInt(12001);
        int count = 2 + level.random.nextInt(4);
        FishFeedingSavedData.get(level).addEntry(pos, startDayTime, targetDelta, count);
        itemEntity.discard();
    }

    public static boolean isFishBait(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (Config.BAIT_BLACKLIST.get().contains(id)) {
            return false;
        }
        if (Config.BAIT_WHITELIST.get().contains(id)) {
            return true;
        }
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