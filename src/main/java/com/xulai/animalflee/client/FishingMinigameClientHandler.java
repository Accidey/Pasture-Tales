package com.xulai.animalflee.client;

import com.xulai.animalflee.network.FishingMinigameOpenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class FishingMinigameClientHandler {

    private FishingMinigameClientHandler() {
    }

    public static void open(FishingMinigameOpenPacket packet) {
        Minecraft.getInstance().setScreen(new FishingMinigameScreen(
                Component.translatable("screen.animalflee.fishing_minigame"),
                packet.catchWindow(),
                packet.critShare(),
                packet.speedMultiplier(),
                packet.seconds()
        ));
    }
}
