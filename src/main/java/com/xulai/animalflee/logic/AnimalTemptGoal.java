package com.xulai.animalflee.logic;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public class AnimalTemptGoal extends Goal {

    private static final TargetingConditions TEMP_TARGETING =
            TargetingConditions.forNonCombat().range(10.0).ignoreLineOfSight();
    private static final double STOP_DISTANCE_SQR = 6.25;

    private final Animal animal;
    private final double speedModifier;
    private final TargetingConditions targetingConditions;
    private Player player;
    private int calmDown;

    public AnimalTemptGoal(Animal animal, double speedModifier) {
        this.animal = animal;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.targetingConditions = TEMP_TARGETING.copy().selector(this::shouldFollow);
    }

    @Override
    public boolean canUse() {
        if (this.calmDown > 0) {
            this.calmDown--;
            return false;
        }
        if (!HungerSystem.isDomesticated(this.animal)) return false;
        if (this.animal.getPersistentData().hasUUID(PregnancySystem.NBT_ESTRUS_FATHER)) return false;
        this.player = this.animal.level().getNearestPlayer(this.targetingConditions, this.animal);
        return this.player != null;
    }

    private boolean shouldFollow(LivingEntity entity) {
        if (!(entity instanceof Player candidate)) return false;
        return isTempting(candidate.getMainHandItem()) || isTempting(candidate.getOffhandItem());
    }

    private boolean isTempting(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (AnimalFoods.getSaturation(this.animal, stack) <= 0.0F) return false;
        return AnimalFoods.canEatByDiet(this.animal, stack);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.animal.getNavigation().moveTo(this.player, this.speedModifier);
    }

    @Override
    public void stop() {
        this.player = null;
        this.animal.getNavigation().stop();
        this.calmDown = 100;
    }

    @Override
    public void tick() {
        this.animal.getLookControl().setLookAt(this.player, 10.0F, this.animal.getMaxHeadXRot());
        if (this.animal.distanceToSqr(this.player) < STOP_DISTANCE_SQR) {
            this.animal.getNavigation().stop();
        } else {
            this.animal.getNavigation().moveTo(this.player, this.speedModifier);
        }
    }
}
