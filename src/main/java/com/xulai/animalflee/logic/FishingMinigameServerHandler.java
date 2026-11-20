package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import com.xulai.animalflee.network.FishingMinigameOpenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FishingMinigameServerHandler {

    private static final Map<UUID, UUID> ACTIVE_HOOKS = new HashMap<>();

    private FishingMinigameServerHandler() {
    }

    public static void begin(ServerPlayer player, FishingHook hook) {
        ACTIVE_HOOKS.put(player.getUUID(), hook.getUUID());
        PacketDistributor.sendToPlayer(player, new FishingMinigameOpenPacket(
                Config.FISHING_MINIGAME_CATCH_WINDOW.get().floatValue(),
                Config.FISHING_MINIGAME_CRIT_RATE.get().floatValue(),
                Config.FISHING_MINIGAME_SPEED.get().floatValue(),
                Config.FISHING_MINIGAME_SECONDS.get()
        ));
    }

    public static void finish(ServerPlayer player, FishingMinigameResult result) {
        UUID hookId = ACTIVE_HOOKS.remove(player.getUUID());
        if (hookId == null) {
            return;
        }
        Entity entity = player.serverLevel().getEntities().get(hookId);
        if (entity instanceof FishingMinigameHook hook) {
            hook.animalflee$finishMinigame(result);
        }
    }

    public static void clear(ServerPlayer player) {
        ACTIVE_HOOKS.remove(player.getUUID());
    }
}
