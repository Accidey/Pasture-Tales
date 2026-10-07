package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

public final class FishPopulationHandler {

    private static final EntityTypeTest<Entity, AbstractFish> FISH_TYPE = EntityTypeTest.forClass(AbstractFish.class);

    private FishPopulationHandler() {
    }

    private static boolean isCatchable(AbstractFish fish) {
        return fish.isAlive() && fish.isInWater();
    }

    private static AbstractFish findCatchableFish(ServerLevel level, BlockPos pos, double radius) {
        AABB box = new AABB(pos).inflate(radius, radius, radius);
        AbstractFish[] found = new AbstractFish[1];
        level.getEntities().get(FISH_TYPE, box, (AbortableIterationConsumer<AbstractFish>) fish -> {
            if (isCatchable(fish)) {
                found[0] = fish;
                return AbortableIterationConsumer.Continuation.ABORT;
            }
            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
        return found[0];
    }

    public static AbstractFish pickFishNearby(ServerLevel level, BlockPos pos) {
        return findCatchableFish(level, pos, Config.FISH_FISH_SCAN_RADIUS.get());
    }

    public static void spawnFish(ServerLevel level, BlockPos pos, int count) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            int offsetX = random.nextInt(9) - 4;
            int offsetZ = random.nextInt(9) - 4;
            int y = pos.getY();
            BlockPos spawnPos = new BlockPos(pos.getX() + offsetX, y, pos.getZ() + offsetZ);
            if (!NaturalWaterHelper.isNaturalWater(level, spawnPos)) {
                continue;
            }
            EntityType<? extends AbstractFish> type = pickFishType(level, spawnPos);
            AbstractFish fish = type.create(level, null, spawnPos, MobSpawnType.NATURAL, false, false);
            if (fish == null) {
                continue;
            }
            fish.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
            if (!level.addFreshEntity(fish)) {
                fish.discard();
            }
        }
    }

    private static EntityType<? extends AbstractFish> pickFishType(ServerLevel level, BlockPos pos) {
        boolean ocean = level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_OCEAN);
        int roll = level.random.nextInt(100);
        if (ocean) {
            if (roll < 50) return EntityType.COD;
            if (roll < 75) return EntityType.SALMON;
            if (roll < 95) return EntityType.TROPICAL_FISH;
            return EntityType.PUFFERFISH;
        }
        if (roll < 70) return EntityType.SALMON;
        return EntityType.COD;
    }
}
