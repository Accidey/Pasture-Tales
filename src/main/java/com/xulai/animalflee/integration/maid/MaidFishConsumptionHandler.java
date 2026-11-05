package com.xulai.animalflee.integration.maid;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidFishedEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.MaidFishingHook;
import com.xulai.animalflee.logic.FishPopulationHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class MaidFishConsumptionHandler {

    private MaidFishConsumptionHandler() {
    }

    @SubscribeEvent
    public static void onMaidFished(MaidFishedEvent event) {
        if (!containsFish(event)) {
            return;
        }

        MaidFishingHook hook = event.getHook();
        if (!(hook.level() instanceof ServerLevel serverLevel)) return;

        AbstractFish fish = FishPopulationHandler.pickFishNearby(serverLevel, hook.blockPosition());
        if (fish != null) {
            fish.discard();
            return;
        }

        for (ItemStack stack : event.getDrops()) {
            if (stack.is(ItemTags.FISHES)) {
                stack.setCount(0);
            }
        }
    }

    private static boolean containsFish(MaidFishedEvent event) {
        for (ItemStack stack : event.getDrops()) {
            if (stack.is(ItemTags.FISHES)) {
                return true;
            }
        }
        return false;
    }
}
