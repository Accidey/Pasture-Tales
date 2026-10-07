package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
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

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class FishConsumptionHandler {

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (!containsFish(event)) {
            return;
        }

        FishingHook hook = event.getHookEntity();
        if (hook == null) return;
        if (!(hook.level() instanceof ServerLevel serverLevel)) return;

        AbstractFish fish = FishPopulationHandler.pickFishNearby(serverLevel, hook.blockPosition());
        if (fish != null) {
            pullFishToPlayer(fish, event.getEntity(), hook);
        }

        for (ItemStack stack : event.getDrops()) {
            if (stack.is(ItemTags.FISHES)) {
                stack.setCount(0);
            }
        }
    }

    private static boolean containsFish(ItemFishedEvent event) {
        for (ItemStack stack : event.getDrops()) {
            if (stack.is(ItemTags.FISHES)) {
                return true;
            }
        }
        return false;
    }

    private static void pullFishToPlayer(AbstractFish fish, Player player, FishingHook hook) {
        Vec3 hookPos = hook.position();
        fish.setPos(hookPos.x, hookPos.y, hookPos.z);
        Vec3 toPlayer = player.position().subtract(hookPos);
        if (toPlayer.horizontalDistance() < 0.01) {
            toPlayer = Vec3.ZERO;
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
