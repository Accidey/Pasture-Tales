package com.xulai.animalflee.mixin.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.MaidFishingHook;
import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.FishPopulationHandler;
import com.xulai.animalflee.logic.NaturalWaterHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = MaidFishingHook.class, remap = false)
public class MaidFishingHookMixin {

    @Shadow
    private int nibble;

    @Shadow
    private int timeUntilHooked;

    @Shadow
    @Final
    private int luck;

    @Shadow
    protected List<ItemStack> getLoot(MinecraftServer server, LootParams params) {
        throw new IllegalStateException("unshadowed");
    }

    @Unique
    private static final int RECHECK_INTERVAL = 20;

    @Unique
    private int animalflee$lastWaterCheckTick = -RECHECK_INTERVAL;

    @Unique
    private boolean animalflee$naturalWater;

    @Unique
    private boolean animalflee$decided;

    @Unique
    @Nullable
    private List<ItemStack> animalflee$pendingLoot;

    @Unique
    private boolean animalflee$rolling;

    @Inject(
            method = "catchingFish(Lnet/minecraft/core/BlockPos;Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void animalflee$biteGate(BlockPos pos, ServerLevel level, CallbackInfo ci) {
        MaidFishingHook hook = (MaidFishingHook) (Object) this;
        if (Config.FISHING_REQUIRES_NATURAL_WATER.get()) {
            if (hook.tickCount - this.animalflee$lastWaterCheckTick >= RECHECK_INTERVAL) {
                this.animalflee$lastWaterCheckTick = hook.tickCount;
                this.animalflee$naturalWater = NaturalWaterHelper.isNaturalWater(hook.level(), pos);
            }
            if (!this.animalflee$naturalWater) {
                ci.cancel();
                return;
            }
        }
        if (this.nibble == 0 && this.timeUntilHooked <= 0) {
            this.animalflee$pendingLoot = null;
            this.animalflee$decided = false;
            return;
        }
        if (this.nibble != 0 || this.animalflee$decided) {
            return;
        }
        this.animalflee$decided = true;
        List<ItemStack> rolled = animalflee$rollLoot(hook, level);
        if (rolled == null) {
            return;
        }
        if (rolled.isEmpty()
                || (FishPopulationHandler.containsFishItem(rolled)
                    && FishPopulationHandler.pickFishNearby(level, hook.blockPosition()) == null)) {
            this.animalflee$pendingLoot = null;
            this.animalflee$decided = false;
            this.timeUntilHooked = 0;
            ci.cancel();
            return;
        }
        this.animalflee$pendingLoot = rolled;
    }

    @Nullable
    @Unique
    private List<ItemStack> animalflee$rollLoot(MaidFishingHook hook, ServerLevel serverLevel) {
        EntityMaid maid = hook.getMaidOwner();
        if (maid == null) {
            return null;
        }
        LootParams params = FishPopulationHandler.fishingParams(
                serverLevel, hook, maid, maid.getMainHandItem(), (float) this.luck + maid.getLuck());
        this.animalflee$rolling = true;
        try {
            return this.getLoot(serverLevel.getServer(), params);
        } finally {
            this.animalflee$rolling = false;
        }
    }

    @Inject(
            method = "getLoot(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/level/storage/loot/LootParams;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void animalflee$deliverLoot(MinecraftServer server, LootParams params, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (this.animalflee$rolling) {
            return;
        }
        MaidFishingHook hook = (MaidFishingHook) (Object) this;
        List<ItemStack> pending = this.animalflee$pendingLoot;
        this.animalflee$pendingLoot = null;
        if (!(hook.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ItemStack> base = pending != null ? pending : cir.getReturnValue();
        if (!FishPopulationHandler.containsFishItem(base)) {
            if (pending != null) {
                cir.setReturnValue(base);
            }
            return;
        }
        AbstractFish fish = FishPopulationHandler.pickFishNearby(serverLevel, hook.blockPosition());
        if (fish == null) {
            cir.setReturnValue(FishPopulationHandler.rollJunkOrTreasure(
                    serverLevel, params, hook.isOpenWaterFishing()));
            return;
        }
        List<ItemStack> result = new ArrayList<>(base);
        List<ItemStack> drops = FishPopulationHandler.harvestFish(serverLevel, fish, hook.getMaidOwner());
        if (drops.isEmpty()) {
            cir.setReturnValue(result);
            return;
        }
        result.removeIf(stack -> stack.is(ItemTags.FISHES));
        result.addAll(drops);
        cir.setReturnValue(result);
    }
}
