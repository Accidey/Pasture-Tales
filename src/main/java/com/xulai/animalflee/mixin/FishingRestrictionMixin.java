package com.xulai.animalflee.mixin;

import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.FishPopulationHandler;
import com.xulai.animalflee.logic.FishingMinigameHook;
import com.xulai.animalflee.logic.NaturalWaterHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FishingHook.class)
public class FishingRestrictionMixin {

    @Shadow
    private int nibble;

    @Shadow
    private int timeUntilHooked;

    @Shadow
    @Final
    private int luck;

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

    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    private void animalflee$biteGate(BlockPos pos, CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
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
        if (!(hook.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.animalflee$decided = true;
        List<ItemStack> rolled = animalflee$rollLoot(hook, serverLevel);
        if (rolled == null) {
            return;
        }
        boolean fishBranch = FishPopulationHandler.containsFishItem(rolled);
        if (rolled.isEmpty()
                || (fishBranch && FishPopulationHandler.pickFishNearby(serverLevel, hook.blockPosition()) == null)) {
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
    private List<ItemStack> animalflee$rollLoot(FishingHook hook, ServerLevel serverLevel) {
        Player owner = hook.getPlayerOwner();
        if (owner == null) {
            return null;
        }
        LootParams params = FishPopulationHandler.fishingParams(
                serverLevel, hook, owner, owner.getMainHandItem(), (float) this.luck + owner.getLuck());
        return FishPopulationHandler.rollFishingLoot(serverLevel, params);
    }

    @Redirect(
            method = "retrieve",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"
            )
    )
    private ObjectArrayList<ItemStack> animalflee$deliverLoot(LootTable table, LootParams params) {
        FishingHook hook = (FishingHook) (Object) this;
        List<ItemStack> pending = this.animalflee$pendingLoot;
        this.animalflee$pendingLoot = null;
        List<ItemStack> loot = pending != null ? pending : table.getRandomItems(params);
        if (hook.level() instanceof ServerLevel serverLevel
                && FishPopulationHandler.containsFishItem(loot)
                && FishPopulationHandler.pickFishNearby(serverLevel, hook.blockPosition()) == null) {
            loot = FishPopulationHandler.rollJunkOrTreasure(serverLevel, params, hook.isOpenWaterFishing());
        }
        ObjectArrayList<ItemStack> delivered = new ObjectArrayList<>(loot);
        if (hook instanceof FishingMinigameHook minigameHook) {
            minigameHook.animalflee$acceptLoot(delivered, params);
        }
        return delivered;
    }
}
