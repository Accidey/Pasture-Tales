package com.xulai.animalflee.logic;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class TemptGoalRegistry {

    private static final int TEMPT_PRIORITY = 3;
    private static final double TEMPT_SPEED = 1.25;

    private static final Set<Animal> REGISTERED = Collections.newSetFromMap(new WeakHashMap<>());

    private TemptGoalRegistry() {
    }

    public static void ensure(Animal animal) {
        if (!REGISTERED.add(animal)) return;
        animal.goalSelector.removeAllGoals(goal -> goal instanceof TemptGoal);
        animal.goalSelector.addGoal(TEMPT_PRIORITY, new AnimalTemptGoal(animal, TEMPT_SPEED));
    }
}
