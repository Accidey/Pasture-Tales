package com.xulai.animalflee.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.BitSet;

public final class EggGridHelper {

    public static final String NBT_SLOT = "af_egg_slot";
    public static final int GRID_SIZE = 9;
    private static final double OFFSET = 0.25;

    private EggGridHelper() {
    }

    public static boolean isGridEgg(ItemEntity egg) {
        return egg.getPersistentData().contains(NBT_SLOT);
    }

    public static int getSlot(ItemEntity egg) {
        return egg.getPersistentData().getInt(NBT_SLOT);
    }

    public static void setSlot(ItemEntity egg, int slot) {
        egg.getPersistentData().putInt(NBT_SLOT, slot);
    }

    public static double slotX(BlockPos hay, int slot) {
        return hay.getX() + 0.5 + (slot % 3 - 1) * OFFSET;
    }

    public static double slotZ(BlockPos hay, int slot) {
        return hay.getZ() + 0.5 + (slot / 3 - 1) * OFFSET;
    }

    public static int findFreeSlot(Level level, BlockPos hay) {
        BitSet occupied = collectOccupiedSlots(level, hay);
        for (int slot = 0; slot < GRID_SIZE; slot++) {
            if (!occupied.get(slot)) return slot;
        }
        return -1;
    }

    public static BitSet collectOccupiedSlots(Level level, BlockPos hay) {
        BitSet occupied = new BitSet(GRID_SIZE);
        for (int slot = 0; slot < GRID_SIZE; slot++) {
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

    public static ItemEntity findGridEgg(Level level, BlockPos hay, double x, double z) {
        double bestDist = Double.MAX_VALUE;
        ItemEntity best = null;
        for (int slot = 0; slot < GRID_SIZE; slot++) {
            double sx = slotX(hay, slot);
            double sz = slotZ(hay, slot);
            AABB box = new AABB(sx - 0.3, hay.getY() + 0.8, sz - 0.3,
                    sx + 0.3, hay.getY() + 1.3, sz + 0.3);
            for (ItemEntity egg : level.getEntitiesOfClass(ItemEntity.class, box)) {
                if (!isGridEgg(egg)) continue;
                if (getSlot(egg) != slot) continue;
                double dist = Math.pow(egg.getX() - x, 2) + Math.pow(egg.getZ() - z, 2);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = egg;
                }
            }
        }
        return best;
    }
}