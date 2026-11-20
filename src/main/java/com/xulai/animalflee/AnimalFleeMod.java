package com.xulai.animalflee;

import com.mojang.logging.LogUtils;
import com.xulai.animalflee.client.FishingMinigameClientHandler;
import com.xulai.animalflee.logic.FishingMinigameServerHandler;
import com.xulai.animalflee.network.FishingMinigameOpenPacket;
import com.xulai.animalflee.network.FishingMinigameResultPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(AnimalFleeMod.MODID)
public class AnimalFleeMod {
    public static final String MODID = "animalflee";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AnimalFleeMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "pasturetales-common.toml");
        modEventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(
                FishingMinigameOpenPacket.TYPE,
                FishingMinigameOpenPacket.CODEC,
                (payload, context) -> context.enqueueWork(() -> FishingMinigameClientHandler.open(payload))
        );
        registrar.playToServer(
                FishingMinigameResultPacket.TYPE,
                FishingMinigameResultPacket.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        FishingMinigameServerHandler.finish(player, payload.result());
                    }
                })
        );
    }
}
