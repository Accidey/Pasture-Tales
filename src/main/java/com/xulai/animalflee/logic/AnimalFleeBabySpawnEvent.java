package com.xulai.animalflee.logic;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;

public class AnimalFleeBabySpawnEvent extends BabyEntitySpawnEvent {

    private final Player breeder;

    public AnimalFleeBabySpawnEvent(Animal mother, Animal partner, AgeableMob child, Player breeder) {
        super(mother, partner, child);
        this.breeder = breeder;
    }

    @Override
    public Player getCausedByPlayer() {
        return this.breeder;
    }
}
