package com.xulai.animalflee.mixin;

import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.FishPopulationHandler;
import com.xulai.animalflee.logic.NaturalWaterHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class FishingRestrictionMixin {

    @Unique
    private static final String CACHE_KEY = "animalflee_legal_water";
    @Unique
    private static final String CACHE_TICK = "animalflee_legal_tick";
    @Unique
    private static final int RECHECK_INTERVAL = 20;

    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    private void animalflee$restrictFishing(BlockPos pos, CallbackInfo ci) {
        if (!Config.FISHING_REQUIRES_NATURAL_WATER.get()) {
            return;
        }
        FishingHook hook = (FishingHook) (Object) this;
        Level level = hook.level();
        if (level.isClientSide) {
            return;
        }
        BlockPos hookPos = hook.blockPosition();

        if (!isLegalCached(hook, level, hookPos)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean isLegalCached(FishingHook hook, Level level, BlockPos pos) {
        var data = hook.getPersistentData();
        int lastCheck = data.getInt(CACHE_TICK);
        if (hook.tickCount - lastCheck < RECHECK_INTERVAL && data.contains(CACHE_KEY)) {
            return data.getBoolean(CACHE_KEY);
        }
        data.putInt(CACHE_TICK, hook.tickCount);

        boolean legal = NaturalWaterHelper.isNaturalWater(level, pos);
        if (legal && level instanceof ServerLevel serverLevel) {
            legal = FishPopulationHandler.countFishNearby(serverLevel, pos, Config.FISH_FISH_SCAN_RADIUS.get()) > 0;
        }
        data.putBoolean(CACHE_KEY, legal);
        return legal;
    }
}