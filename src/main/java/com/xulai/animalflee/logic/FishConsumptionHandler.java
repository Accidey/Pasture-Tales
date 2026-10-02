package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

import java.util.List;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class FishConsumptionHandler {

    private static final double FISH_PULL_RADIUS = 16.0;

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        FishingHook hook = event.getHookEntity();
        if (hook == null) return;
        if (!(hook.level() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.isClientSide) return;

        Player player = event.getEntity();
        BlockPos hookPos = hook.blockPosition();

        AbstractFish fish = FishPopulationHandler.pickFishNearby(serverLevel, hookPos, FISH_PULL_RADIUS);
        if (fish != null) {
            pullFishToPlayer(fish, player, hook);
        }

        List<ItemStack> drops = event.getDrops();
        for (ItemStack stack : drops) {
            if (stack.is(ItemTags.FISHES)) {
                stack.setCount(0);
            }
        }
    }

    private static void pullFishToPlayer(AbstractFish fish, Player player, FishingHook hook) {
        Vec3 hookPos = hook.position();
        fish.setPos(hookPos.x, hookPos.y, hookPos.z);
        Vec3 toPlayer = player.position().subtract(hookPos);
        double dist = toPlayer.horizontalDistance();
        if (dist < 0.01) {
            toPlayer = new Vec3(0, 0, 0);
        }
        double strength = 0.12;
        double vertical = 0.18;
        fish.setDeltaMovement(
                toPlayer.x * strength,
                toPlayer.y * strength + Math.sqrt(Math.sqrt(toPlayer.lengthSqr())) * vertical,
                toPlayer.z * strength);
        fish.hurtMarked = true;
    }
}