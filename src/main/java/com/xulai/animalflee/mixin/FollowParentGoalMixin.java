package com.xulai.animalflee.mixin;

import com.xulai.animalflee.logic.GenderLogic;
import com.xulai.animalflee.logic.PregnancySystem;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FollowParentGoal.class)
public class FollowParentGoalMixin {

    @Shadow
    @Final
    private Animal animal;

    @Shadow
    private Animal parent;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void animalflee$followMother(CallbackInfoReturnable<Boolean> cir) {
        if (this.animal.getAge() >= 0) {
            cir.setReturnValue(false);
            return;
        }

        List<Animal> candidates = this.animal.level().getEntitiesOfClass(
                Animal.class,
                this.animal.getBoundingBox().inflate(
                        FollowParentGoal.HORIZONTAL_SCAN_RANGE,
                        FollowParentGoal.VERTICAL_SCAN_RANGE,
                        FollowParentGoal.HORIZONTAL_SCAN_RANGE),
                candidate -> candidate.getAge() >= 0
                        && GenderLogic.isFemale(candidate)
                        && PregnancySystem.isCompatibleMate(this.animal, candidate));

        Animal mother = null;
        double closest = Double.MAX_VALUE;
        for (Animal candidate : candidates) {
            double dist = this.animal.distanceToSqr(candidate);
            if (dist < closest) {
                closest = dist;
                mother = candidate;
            }
        }

        if (mother == null || closest < FollowParentGoal.DONT_FOLLOW_IF_CLOSER_THAN) {
            cir.setReturnValue(false);
            return;
        }

        this.parent = mother;
        cir.setReturnValue(true);
    }
}