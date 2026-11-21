package com.xulai.animalflee.command;

import com.xulai.animalflee.AnimalFleeMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommandRegistrationHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        MobCommands.register(event.getDispatcher());
        ItemCommands.register(event.getDispatcher());
    }
}