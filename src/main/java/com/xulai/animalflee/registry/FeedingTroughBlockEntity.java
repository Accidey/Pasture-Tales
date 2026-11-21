package com.xulai.animalflee.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.ContainerHelper;

public class FeedingTroughBlockEntity extends BlockEntity {

    public static final int SLOTS = 4;
    public static final int CAPACITY_PER_LEVEL = 8;
    public static final int MAX_ITEMS = 8 * CAPACITY_PER_LEVEL;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    public FeedingTroughBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FEEDING_TROUGH_BE.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public int getTotalCount() {
        int total = 0;
        for (ItemStack stack : items) {
            total += stack.getCount();
        }
        return total;
    }

    public int getFeedLevel() {
        return Math.min(8, (getTotalCount() + CAPACITY_PER_LEVEL - 1) / CAPACITY_PER_LEVEL);
    }

    public ItemStack insert(ItemStack stack) {
        int allowed = Math.min(stack.getCount(), MAX_ITEMS - getTotalCount());
        if (allowed <= 0) return stack;
        ItemStack toInsert = stack.copyWithCount(allowed);
        for (int i = 0; i < items.size() && !toInsert.isEmpty(); i++) {
            ItemStack slot = items.get(i);
            if (slot.isEmpty()) {
                items.set(i, toInsert.split(toInsert.getCount()));
            } else if (ItemStack.isSameItemSameComponents(slot, toInsert)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int move = Math.min(space, toInsert.getCount());
                if (move > 0) {
                    slot.grow(move);
                    toInsert.shrink(move);
                }
            }
        }
        setChanged();
        return toInsert;
    }

    public ItemStack extractOne() {
        for (int i = 0; i < items.size(); i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty()) {
                ItemStack taken = slot.split(1);
                if (slot.isEmpty()) items.set(i, ItemStack.EMPTY);
                setChanged();
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> extractAll() {
        NonNullList<ItemStack> extracted = NonNullList.create();
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                extracted.add(items.get(i));
                items.set(i, ItemStack.EMPTY);
            }
        }
        setChanged();
        return extracted;
    }

    public void syncLevel() {
        if (level != null && !level.isClientSide) {
            int newLevel = getFeedLevel();
            BlockState state = getBlockState();
            if (state.hasProperty(FeedingTroughBlock.LEVEL) && state.getValue(FeedingTroughBlock.LEVEL) != newLevel) {
                level.setBlock(worldPosition, state.setValue(FeedingTroughBlock.LEVEL, newLevel), 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}
