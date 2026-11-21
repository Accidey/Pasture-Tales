package com.xulai.animalflee.logic;

import com.xulai.animalflee.Config;
import com.xulai.animalflee.registry.FeedingTroughBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

public class EatFromTroughGoal extends MoveToBlockGoal {

    private static final int BITE_INTERVAL_TICKS = 30;

    private final Animal animal;
    private int biteCooldown;

    public EatFromTroughGoal(Animal animal, double speedModifier) {
        super(animal, speedModifier, Config.HUNGER_TROUGH_SEARCH_RANGE.get(), 2);
        this.animal = animal;
    }

    @Override
    public boolean canUse() {
        if (!Config.HUNGER_ENABLED.get() || !Config.HUNGER_TROUGH_EATING.get()) return false;
        if (!HungerSystem.isDomesticated(this.animal)) return false;
        if (this.animal.getPersistentData().hasUUID(PregnancySystem.NBT_ESTRUS_FATHER)) return false;
        if (!isHungryEnough()) return false;
        return super.canUse();
    }

    private boolean isHungryEnough() {
        float max = HungerSystem.getMaxHunger(this.animal);
        return HungerSystem.getHunger(this.animal) < max * Config.HUNGER_TROUGH_SEEK_PERCENT.get() / 100.0F;
    }

    private boolean isEdible(ItemStack stack) {
        return HungerSystem.canEat(this.animal, stack);
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
            return trough.hasFood(this::isEdible);
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        // isValidTarget 里已经包含"还有能吃的食物"（吃饱后 canEat 不再通过），吃到饱或槽空自动结束
        return super.canContinueToUse();
    }

    @Override
    public double acceptedDistance() {
        return 2.0;
    }

    @Override
    public void start() {
        super.start();
        MobFleeLogic.stopFlee(this.animal);
        this.biteCooldown = BITE_INTERVAL_TICKS;
    }

    @Override
    public void stop() {
        super.stop();
        AnimalEatAnimation.stop(this.animal);
        this.biteCooldown = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isReachedTarget()) return;
        this.animal.getLookControl().setLookAt(
                (double) this.blockPos.getX() + 0.5,
                (double) this.blockPos.getY(),
                (double) this.blockPos.getZ() + 0.5);
        if (--this.biteCooldown > 0) return;
        this.biteCooldown = BITE_INTERVAL_TICKS;
        this.bite();
    }

    private void bite() {
        if (!(this.animal.level().getBlockEntity(this.blockPos) instanceof FeedingTroughBlockEntity trough)) return;
        ItemStack eaten = trough.extractOne(this::isEdible);
        if (eaten.isEmpty()) return;
        trough.syncLevel();
        HungerSystem.feed(this.animal, eaten, null);
        AnimalEatAnimation.playBite(this.animal, eaten);
    }
}
