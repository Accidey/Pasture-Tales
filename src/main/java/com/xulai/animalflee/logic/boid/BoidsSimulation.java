package com.xulai.animalflee.logic.boid;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record BoidsSimulation(
        float separationInfluence,
        float separationRange,
        float separationAngle,
        float alignmentInfluence,
        float alignmentAngle,
        float cohesionInfluence,
        float cohesionAngle,
        float randomness
) {
    public static float degrees(float angle) {
        return (float) Math.cos(Math.toRadians(angle));
    }

    public Vec3 apply(Mob mob, List<? extends Entity> nearbyMobs) {
        Vec3 separation = Vec3.ZERO;
        Vec3 alignment = Vec3.ZERO;
        Vec3 cohesion = Vec3.ZERO;

        Vec3 random = new Vec3(
                mob.getRandom().nextGaussian() * randomness,
                mob.getRandom().nextGaussian() * randomness,
                mob.getRandom().nextGaussian() * randomness);

        int alignmentCount = 0;
        int cohesionCount = 0;

        for (Entity other : nearbyMobs) {
            if (mob == other) continue;

            Vec3 delta = other.position().subtract(mob.position());
            double dist = Math.max(0.00001d, delta.length());
            double angle = mob.getLookAngle().dot(delta.normalize());

            if (dist < separationRange && angle >= separationAngle) {
                separation = separation.add(delta.scale(-((1.0 / dist) - (1.0 / separationRange))));
            }

            if (angle >= alignmentAngle) {
                alignment = alignment.add(other.getDeltaMovement().normalize());
                alignmentCount++;
            }

            if (angle >= cohesionAngle) {
                cohesion = cohesion.add(other.position());
                cohesionCount++;
            }
        }

        if (alignmentCount == 0) {
            alignment = Vec3.ZERO;
        } else {
            alignment = alignment.scale(1.0 / alignmentCount);
        }

        if (cohesionCount == 0) {
            cohesion = Vec3.ZERO;
        } else {
            cohesion = cohesion.scale(1.0 / cohesionCount).subtract(mob.position());
        }

        return alignment.scale(alignmentInfluence)
                .add(separation.scale(separationInfluence))
                .add(cohesion.scale(cohesionInfluence))
                .add(random);
    }
}
