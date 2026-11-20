package com.xulai.animalflee.integration.majruszsaccessories;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class MajruszAccessoriesCompat {

    private static final String MOD_ID = "majruszsaccessories";
    private static final String EVENT_CLASS = "com.xulai.majruszlibrary.events.OnAnimalTamed";
    private static final String DISPATCH_CLASS = "com.xulai.majruszlibrary.events.base.Events";

    private static Method dispatch;
    private static Constructor<?> animalTamed;
    private static boolean initialized;
    private static boolean available;

    private MajruszAccessoriesCompat() {
    }

    public static void notifyAnimalTamed(Animal animal, Player player) {
        if (player == null) return;
        if (animal.level().isClientSide) return;
        if (animal instanceof TamableAnimal) return;
        initialize();
        if (!available) return;
        try {
            Object event = animalTamed.newInstance(animal, player);
            dispatch.invoke(null, event);
        } catch (ReflectiveOperationException | LinkageError e) {
            available = false;
        }
    }

    private static void initialize() {
        if (initialized) return;
        initialized = true;
        if (!ModList.get().isLoaded(MOD_ID)) return;
        try {
            Class<?> eventClass = Class.forName(EVENT_CLASS);
            Class<?> eventsClass = Class.forName(DISPATCH_CLASS);
            animalTamed = eventClass.getConstructor(Animal.class, Player.class);
            dispatch = eventsClass.getMethod("dispatch", Object.class);
            available = true;
        } catch (ReflectiveOperationException | LinkageError e) {
            available = false;
        }
    }
}
