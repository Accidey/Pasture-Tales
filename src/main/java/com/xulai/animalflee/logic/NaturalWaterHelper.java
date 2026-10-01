package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public final class NaturalWaterHelper {

    private NaturalWaterHelper() {
    }

    public static boolean isNaturalWater(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        if (!level.canSeeSky(above)) {
            return false;
        }
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH)) {
            return true;
        }
        return connectedWaterVolume(level, pos) >= Config.FISHING_MIN_WATER_VOLUME.get();
    }

    public static int connectedWaterVolume(Level level, BlockPos start) {
        if (!isWater(level, start)) {
            return 0;
        }
        Set<Long> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start.asLong());
        int count = 0;
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            count++;
            if (count >= 1024) {
                return count;
            }
            BlockPos[] neighbors = {
                    current.east(),
                    current.west(),
                    current.north(),
                    current.south()
            };
            for (BlockPos neighbor : neighbors) {
                if (!isWater(level, neighbor)) {
                    continue;
                }
                long key = neighbor.asLong();
                if (visited.add(key)) {
                    queue.add(neighbor);
                }
            }
        }
        return count;
    }

    public static boolean isWater(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluid = state.getFluidState();
        return fluid.is(FluidTags.WATER) && fluid.isSource();
    }
}