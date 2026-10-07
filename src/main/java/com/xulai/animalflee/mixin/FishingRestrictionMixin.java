package com.xulai.animalflee.mixin;

import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.NaturalWaterHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class FishingRestrictionMixin {

    @Unique
    private static final int RECHECK_INTERVAL = 20;

    @Unique
    private int animalflee$lastWaterCheckTick = -RECHECK_INTERVAL;

    @Unique
    private boolean animalflee$naturalWater;

    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    private void animalflee$restrictFishing(BlockPos pos, CallbackInfo ci) {
        if (!Config.FISHING_REQUIRES_NATURAL_WATER.get()) {
            return;
        }
        FishingHook hook = (FishingHook) (Object) this;
        if (hook.tickCount - this.animalflee$lastWaterCheckTick >= RECHECK_INTERVAL) {
            this.animalflee$lastWaterCheckTick = hook.tickCount;
            this.animalflee$naturalWater = NaturalWaterHelper.isNaturalWater(hook.level(), pos);
        }
        if (!this.animalflee$naturalWater) {
            ci.cancel();
        }
    }
}
