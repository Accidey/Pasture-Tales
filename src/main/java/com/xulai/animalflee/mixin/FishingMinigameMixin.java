package com.xulai.animalflee.mixin;

import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.FishConsumptionHandler;
import com.xulai.animalflee.logic.FishPopulationHandler;
import com.xulai.animalflee.logic.FishingMinigameHook;
import com.xulai.animalflee.logic.FishingMinigameResult;
import com.xulai.animalflee.logic.FishingMinigameServerHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingMinigameMixin implements FishingMinigameHook {

    @Shadow
    private int nibble;

    @Unique
    private ObjectArrayList<ItemStack> animalflee$capturedLoot;

    @Unique
    private LootParams animalflee$capturedParams;

    @Unique
    private boolean animalflee$minigameActive;

    @Unique
    private int animalflee$minigameStartTick;

    @Unique
    private List<ItemStack> animalflee$minigameLoot = List.of();

    @Unique
    private LootParams animalflee$minigameParams;

    @Unique
    private ItemStack animalflee$minigameRod = ItemStack.EMPTY;

    @Unique
    private AbstractFish animalflee$minigameTargetFish;

    @Override
    public void animalflee$acceptLoot(ObjectArrayList<ItemStack> loot, LootParams params) {
        this.animalflee$capturedLoot = loot;
        this.animalflee$capturedParams = params;
    }

    @Nullable
    @Override
    public AbstractFish animalflee$getMinigameTargetFish() {
        return this.animalflee$minigameTargetFish;
    }

    @Override
    public void animalflee$finishMinigame(FishingMinigameResult result) {
        if (!this.animalflee$minigameActive) {
            return;
        }
        FishingHook hook = (FishingHook) (Object) this;
        Player owner = hook.getPlayerOwner();
        this.animalflee$minigameActive = false;
        try {
            if (result != FishingMinigameResult.FAIL) {
                animalflee$deliverCatch(hook, result);
            }
        } finally {
            this.animalflee$minigameLoot = List.of();
            this.animalflee$minigameParams = null;
            this.animalflee$minigameRod = ItemStack.EMPTY;
            this.animalflee$minigameTargetFish = null;
            if (owner instanceof ServerPlayer serverPlayer) {
                FishingMinigameServerHandler.clear(serverPlayer);
            }
            hook.discard();
        }
    }

    @Unique
    private void animalflee$deliverCatch(FishingHook hook, FishingMinigameResult result) {
        if (!(hook.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(hook.getPlayerOwner() instanceof ServerPlayer player)) {
            return;
        }
        List<ItemStack> drops = this.animalflee$minigameLoot;
        boolean caughtFish = FishPopulationHandler.containsFishItem(drops);
        if (result == FishingMinigameResult.CRIT && this.animalflee$minigameParams != null) {
            drops = new ArrayList<>(drops);
            drops.addAll(FishPopulationHandler.rollFishingLoot(serverLevel, this.animalflee$minigameParams));
        }
        if (result == FishingMinigameResult.CRIT && caughtFish) {
            animalflee$pullCritFish(serverLevel, player, hook);
        }
        ItemFishedEvent event = new ItemFishedEvent(drops, hook.onGround() ? 2 : 1, hook);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return;
        }
        CriteriaTriggers.FISHING_ROD_HOOKED.trigger(player, this.animalflee$minigameRod, hook, drops);
        animalflee$spawnDrops(serverLevel, player, hook, drops);
    }

    @Unique
    private void animalflee$pullCritFish(ServerLevel level, ServerPlayer player, FishingHook hook) {
        AbstractFish extra = FishPopulationHandler.pickFishNearby(level, hook.blockPosition(), this.animalflee$minigameTargetFish);
        if (extra != null) {
            FishConsumptionHandler.pullFishToPlayer(extra, player, hook);
        }
    }

    @Unique
    private void animalflee$spawnDrops(ServerLevel level, ServerPlayer player, FishingHook hook, List<ItemStack> drops) {
        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), stack);
                double dx = player.getX() - hook.getX();
                double dy = player.getY() - hook.getY();
                double dz = player.getZ() - hook.getZ();
                itemEntity.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * 0.1);
                level.addFreshEntity(itemEntity);
            }
            level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, hook.getRandom().nextInt(6) + 1));
            if (stack.is(ItemTags.FISHES)) {
                player.awardStat(Stats.FISH_CAUGHT, 1);
            }
        }
    }

    @Inject(method = "retrieve", at = @At("HEAD"), cancellable = true)
    private void animalflee$blockReelDuringMinigame(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (this.animalflee$minigameActive) {
            cir.setReturnValue(0);
        }
    }

    @Inject(
            method = "retrieve",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/event/entity/player/ItemFishedEvent;<init>(Ljava/util/List;ILnet/minecraft/world/entity/projectile/FishingHook;)V",
                    remap = false
            ),
            cancellable = true
    )
    private void animalflee$openMinigame(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ObjectArrayList<ItemStack> loot = this.animalflee$capturedLoot;
        LootParams params = this.animalflee$capturedParams;
        this.animalflee$capturedLoot = null;
        this.animalflee$capturedParams = null;
        if (this.animalflee$minigameActive || loot == null || loot.isEmpty() || !Config.FISHING_MINIGAME_ENABLED.get()) {
            return;
        }
        FishingHook hook = (FishingHook) (Object) this;
        if (!(hook.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(hook.getPlayerOwner() instanceof ServerPlayer player)) {
            return;
        }
        this.animalflee$minigameActive = true;
        this.animalflee$minigameStartTick = hook.tickCount;
        this.animalflee$minigameLoot = loot;
        this.animalflee$minigameParams = params;
        this.animalflee$minigameRod = stack;
        this.animalflee$minigameTargetFish = FishPopulationHandler.pickFishNearby(serverLevel, hook.blockPosition());
        this.nibble = Integer.MAX_VALUE;
        FishingMinigameServerHandler.begin(player, hook);
        cir.setReturnValue(hook.onGround() ? 2 : 1);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void animalflee$timeoutMinigame(CallbackInfo ci) {
        if (!this.animalflee$minigameActive) {
            return;
        }
        FishingHook hook = (FishingHook) (Object) this;
        int limit = Config.FISHING_MINIGAME_SECONDS.get() * 20 + 200;
        if (hook.tickCount - this.animalflee$minigameStartTick > limit) {
            this.animalflee$finishMinigame(FishingMinigameResult.FAIL);
        }
    }
}
