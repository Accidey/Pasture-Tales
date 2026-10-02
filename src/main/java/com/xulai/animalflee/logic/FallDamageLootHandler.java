package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import com.xulai.animalflee.Config;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Collection;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class FallDamageLootHandler {

    private static final double PLAYER_SEARCH_RANGE = 32.0;

    private FallDamageLootHandler() {
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!Config.FALL_DAMAGE_LOOT.get()) return;
        if (event.isRecentlyHit()) return;
        LivingEntity entity = event.getEntity();
        if (!AnimalFleeEntityHelper.isSupportedAnimal(entity)) return;
        if (!event.getSource().is(DamageTypes.FALL)) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        Player player = findNearestPlayer(serverLevel, entity);
        if (player == null) return;

        Collection<ItemEntity> drops = event.getDrops();
        drops.clear();

        ResourceKey<LootTable> lootTableKey = entity.getLootTable();
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableKey);
        DamageSource source = event.getSource();
        LootParams.Builder builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity())
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withLuck(player.getLuck());
        LootParams params = builder.create(LootContextParamSets.ENTITY);
        lootTable.getRandomItems(params, entity.getLootTableSeed(), stack -> {
            ItemEntity itemEntity = new ItemEntity(serverLevel, entity.getX(), entity.getY(), entity.getZ(), stack);
            itemEntity.setDefaultPickUpDelay();
            drops.add(itemEntity);
        });
    }

    private static Player findNearestPlayer(ServerLevel level, LivingEntity entity) {
        Player nearest = null;
        double closest = Double.MAX_VALUE;
        for (ServerPlayer player : level.getPlayers(ServerPlayer::isAlive)) {
            double dist = player.distanceToSqr(entity);
            if (dist > PLAYER_SEARCH_RANGE * PLAYER_SEARCH_RANGE) continue;
            if (dist < closest) {
                closest = dist;
                nearest = player;
            }
        }
        return nearest;
    }
}