package com.xulai.animalflee.logic;

import net.minecraft.world.entity.animal.Animal;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class TroughGoalRegistry {

    private static final int EAT_PRIORITY = 2;
    private static final double EAT_SPEED = 1.1;

    private static final Set<Animal> REGISTERED = Collections.newSetFromMap(new WeakHashMap<>());

    private TroughGoalRegistry() {
    }

    public static void ensure(Animal animal) {
        if (!REGISTERED.add(animal)) return;
        animal.goalSelector.removeAllGoals(goal -> goal instanceof EatFromTroughGoal);
        animal.goalSelector.addGoal(EAT_PRIORITY, new EatFromTroughGoal(animal, EAT_SPEED));
    }
}
