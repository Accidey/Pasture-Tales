package com.xulai.animalflee.command;

import com.xulai.animalflee.AnimalFleeMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommandRegistrationHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        BlacklistCommand.register(event.getDispatcher());
        BaitCommand.register(event.getDispatcher());
        CustomFoodCommand.register(event.getDispatcher());
        DietCommand.register(event.getDispatcher());
    }
}