package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class NaturalWaterHelper {

    private static final int BFS_CAP = 1024;
    private static final int QUEUE_SIZE = BFS_CAP + 8;
    private static final int VISITED_CAPACITY = 2048;
    private static final int VISITED_MASK = VISITED_CAPACITY - 1;
    private static final int CACHE_SLOTS = 64;
    private static final int CACHE_MASK = CACHE_SLOTS - 1;
    private static final int CACHE_TTL = 100;

    private static final long[] queue = new long[QUEUE_SIZE];
    private static final long[] visitedKeys = new long[VISITED_CAPACITY];
    private static final int[] visitedStamps = new int[VISITED_CAPACITY];
    private static final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private static int stamp;

    private static final long[] cacheKeys = new long[CACHE_SLOTS];
    private static final int[] cacheDimensions = new int[CACHE_SLOTS];
    private static final boolean[] cacheResults = new boolean[CACHE_SLOTS];
    private static final int[] cacheTimes = new int[CACHE_SLOTS];

    private NaturalWaterHelper() {
    }

    public static boolean isNaturalWater(Level level, BlockPos pos) {
        if (!level.canSeeSky(pos.above())) {
            return false;
        }
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH)) {
            return true;
        }
        int threshold = Config.FISHING_MIN_WATER_VOLUME.get();
        long packed = pos.asLong();
        int dimension = level.dimension().location().hashCode();
        int now = (int) level.getGameTime();
        int slot = cacheSlot(packed);
        if (cacheTimes[slot] == now - CACHE_TTL + 1
                && cacheKeys[slot] == packed
                && cacheDimensions[slot] == dimension) {
            return cacheResults[slot];
        }
        boolean large = connectedWaterVolume(level, pos, threshold) >= threshold;
        cacheKeys[slot] = packed;
        cacheDimensions[slot] = dimension;
        cacheResults[slot] = large;
        cacheTimes[slot] = now;
        return large;
    }

    private static int cacheSlot(long packed) {
        long h = packed * 0x9E3779B97F4A7C15L;
        return (int) (h >>> 58) & CACHE_MASK;
    }

    public static int connectedWaterVolume(Level level, BlockPos start) {
        return connectedWaterVolume(level, start, BFS_CAP);
    }

    private static int connectedWaterVolume(Level level, BlockPos start, int stopAt) {
        int startX = start.getX();
        int startY = start.getY();
        int startZ = start.getZ();
        if (!isWater(level, startX, startY, startZ)) {
            return 0;
        }
        int currentStamp = ++stamp;
        int count = 0;
        int head = 0;
        int tail = 0;
        long startPacked = start.asLong();
        queue[tail++] = startPacked;
        markVisited(startPacked, currentStamp);
        while (head < tail) {
            long node = queue[head++];
            count++;
            if (count >= stopAt) {
                return count;
            }
            int x = BlockPos.getX(node);
            int y = BlockPos.getY(node);
            int z = BlockPos.getZ(node);
            tail = enqueueWater(level, currentStamp, tail, x + 1, y, z);
            tail = enqueueWater(level, currentStamp, tail, x - 1, y, z);
            tail = enqueueWater(level, currentStamp, tail, x, y, z + 1);
            tail = enqueueWater(level, currentStamp, tail, x, y, z - 1);
        }
        return count;
    }

    private static int enqueueWater(Level level, int currentStamp, int tail, int x, int y, int z) {
        if (tail >= QUEUE_SIZE) {
            return tail;
        }
        long packed = BlockPos.asLong(x, y, z);
        if (isVisited(currentStamp, packed)) {
            return tail;
        }
        if (!isWater(level, x, y, z)) {
            return tail;
        }
        markVisited(packed, currentStamp);
        queue[tail++] = packed;
        return tail;
    }

    private static boolean isVisited(int currentStamp, long packed) {
        int index = probe(packed, currentStamp);
        return visitedStamps[index] == currentStamp && visitedKeys[index] == packed;
    }

    private static void markVisited(long packed, int currentStamp) {
        visitedStamps[probe(packed, currentStamp)] = currentStamp;
        visitedKeys[probe(packed, currentStamp)] = packed;
    }

    private static int probe(long packed, int currentStamp) {
        long h = packed * 0x9E3779B97F4A7C15L;
        int index = (int) (h >>> 45) & VISITED_MASK;
        while (visitedStamps[index] == currentStamp) {
            if (visitedKeys[index] == packed) {
                return index;
            }
            index = (index + 1) & VISITED_MASK;
        }
        return index;
    }

    public static boolean isWater(Level level, BlockPos pos) {
        return isWater(level, pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isWater(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState(cursor.set(x, y, z));
        if (state.is(Blocks.WATER)) {
            return true;
        }
        FluidState fluid = state.getFluidState();
        return fluid.is(FluidTags.WATER) && fluid.isSource();
    }
}
