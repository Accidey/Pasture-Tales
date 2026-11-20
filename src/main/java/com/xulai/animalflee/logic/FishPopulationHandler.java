package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.tags.ItemTags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FishPopulationHandler {

    private static final EntityTypeTest<Entity, AbstractFish> FISH_TYPE = EntityTypeTest.forClass(AbstractFish.class);
    private static final int EMPTY_LOOT_REROLL_LIMIT = 8;

    private FishPopulationHandler() {
    }

    private static boolean isCatchable(AbstractFish fish) {
        return fish.isAlive() && fish.isInWater();
    }

    private static AbstractFish findCatchableFish(ServerLevel level, BlockPos pos, double radius, @Nullable Entity excluded) {
        AABB box = new AABB(pos).inflate(radius, radius, radius);
        AbstractFish[] found = new AbstractFish[1];
        level.getEntities().get(FISH_TYPE, box, (AbortableIterationConsumer<AbstractFish>) fish -> {
            if (fish != excluded && isCatchable(fish)) {
                found[0] = fish;
                return AbortableIterationConsumer.Continuation.ABORT;
            }
            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
        return found[0];
    }

    public static AbstractFish pickFishNearby(ServerLevel level, BlockPos pos) {
        return findCatchableFish(level, pos, Config.FISH_FISH_SCAN_RADIUS.get(), null);
    }

    public static AbstractFish pickFishNearby(ServerLevel level, BlockPos pos, @Nullable Entity excluded) {
        return findCatchableFish(level, pos, Config.FISH_FISH_SCAN_RADIUS.get(), excluded);
    }

    public static boolean containsFishItem(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (stack.is(ItemTags.FISHES)) {
                return true;
            }
        }
        return false;
    }

    public static LootParams fishingParams(ServerLevel level, Entity hook, @Nullable Entity owner, ItemStack rod, float luck) {
        return new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, hook.position())
                .withParameter(LootContextParams.TOOL, rod)
                .withParameter(LootContextParams.THIS_ENTITY, hook)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, owner)
                .withLuck(luck)
                .create(LootContextParamSets.FISHING);
    }

    public static List<ItemStack> rollFishingLoot(ServerLevel level, LootParams params) {
        return table(level, BuiltInLootTables.FISHING).getRandomItems(params);
    }

    public static List<ItemStack> rollJunkOrTreasure(ServerLevel level, LootParams params, boolean openWater) {
        LootTable lootTable = table(level, openWater && level.random.nextInt(3) == 0
                ? BuiltInLootTables.FISHING_TREASURE
                : BuiltInLootTables.FISHING_JUNK);
        List<ItemStack> rolled = lootTable.getRandomItems(params);
        for (int attempt = 1; rolled.isEmpty() && attempt < EMPTY_LOOT_REROLL_LIMIT; attempt++) {
            rolled = lootTable.getRandomItems(params);
        }
        return rolled;
    }

    private static LootTable table(ServerLevel level, ResourceKey<LootTable> key) {
        return level.getServer().reloadableRegistries().getLootTable(key);
    }

    public static List<ItemStack> harvestFish(ServerLevel level, AbstractFish fish, @Nullable LivingEntity killer) {
        LootTable lootTable = table(level, fish.getType().getDefaultLootTable());
        List<ItemStack> drops = lootTable.getRandomItems(new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, fish)
                .withParameter(LootContextParams.ORIGIN, fish.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, killer)
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, killer)
                .create(LootContextParamSets.ENTITY));
        fish.discard();
        return drops;
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
