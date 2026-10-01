package com.xulai.animalflee;

import com.xulai.animalflee.command.BlacklistCommand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@Mod(AnimalFleeMod.MODID)
public class AnimalFleeMod {
    public static final String MODID = "animalflee";

    public AnimalFleeMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(this::registerCommands);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        BlacklistCommand.register(event.getDispatcher());
    }
}