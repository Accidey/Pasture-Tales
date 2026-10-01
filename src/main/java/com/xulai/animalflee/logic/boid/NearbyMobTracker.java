package com.xulai.animalflee.logic.boid;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.List;

public final class NearbyMobTracker {

    private NearbyMobTracker() {
    }

    public static List<Mob> sameTypeWithin(Mob mob, double range) {
        return mob.level().getEntities(
                EntityTypeTest.forClass(Mob.class),
                mob.getBoundingBox().inflate(range, range, range),
                other -> other != mob && other.isAlive() && other.getType() == mob.getType());
    }
}
