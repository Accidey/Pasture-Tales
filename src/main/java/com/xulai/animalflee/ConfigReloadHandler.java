package com.xulai.animalflee;

import com.xulai.animalflee.logic.AnimalFoods;
import com.xulai.animalflee.logic.MobFleeLogic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = AnimalFleeMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ConfigReloadHandler {

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        MobFleeLogic.resetBlacklistCache();
        AnimalFoods.resetCustomFoodCache();
        AnimalFoods.resetDietCache();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Mob mob) {
                    MobFleeLogic.stopFlee(mob);
                }
            }
        }
    }
}