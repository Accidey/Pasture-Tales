package com.xulai.animalflee.mixin.maid;

import com.github.tartaricacid.touhoulittlemaid.advancements.maid.TriggerType;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidFeedAnimalTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitTrigger;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.xulai.animalflee.integration.maid.AnimalFleeMaidCompat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MaidFeedAnimalTask.class, remap = false)
public abstract class MaidFeedAnimalTaskMixin {

    @Shadow
    private Animal feedEntity;

    @Shadow
    private long chatBubbleKey;

    @Shadow
    @Final
    private float speedModifier;

    @Shadow
    @Final
    private int maxAnimalCount;

    @Inject(method = "start(Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;J)V", at = @At("HEAD"), cancellable = true)
    private void animalflee$maidStart(ServerLevel worldIn, EntityMaid maid, long gameTimeIn, CallbackInfo ci) {
        feedEntity = null;
        NearestVisibleLivingEntities entities = maid.getBrain()
                .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .orElse(NearestVisibleLivingEntities.empty());

        long animalCount = entities
                .find(e -> maid.isWithinRestriction(e.blockPosition()))
                .filter(Entity::isAlive)
                .filter(e -> e instanceof Animal).count();

        if (animalCount < maxAnimalCount) {
            entities
                    .find(e -> maid.isWithinRestriction(e.blockPosition()))
                    .filter(Entity::isAlive)
                    .filter(e -> e instanceof Animal)
                    .filter(e -> AnimalFleeMaidCompat.canBeBreedTarget((Animal) e))
                    .filter(e -> ItemsUtil.isStackIn(maid, s -> AnimalFleeMaidCompat.isValidFood((Animal) e, s)))
                    .filter(maid::canPathReach)
                    .findFirst()
                    .ifPresent(e -> {
                        feedEntity = (Animal) e;
                        BehaviorUtils.setWalkAndLookTargetMemories(maid, e, speedModifier, 0);
                    });

            if (feedEntity != null && feedEntity.closerThan(maid, 2)) {
                ServerPlayer owner = maid.getOwner() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
                boolean fed = false;
                while (AnimalFleeMaidCompat.canBeFedMore(feedEntity)) {
                    ItemStack food = ItemsUtil.getStack(maid, s -> AnimalFleeMaidCompat.isValidFood(feedEntity, s));
                    if (food.isEmpty()) break;
                    AnimalFleeMaidCompat.feedByMaid(feedEntity, food, owner);
                    food.shrink(1);
                    fed = true;
                }
                if (fed) {
                    maid.swing(InteractionHand.MAIN_HAND);
                    if (owner != null) {
                        InitTrigger.MAID_EVENT.get().trigger(owner, TriggerType.MAID_FEED_ANIMAL);
                    }
                }
                feedEntity = null;
            }
        } else {
            chatBubbleKey = maid.getChatBubbleManager().addTextChatBubbleIfTimeout(
                    "chat_bubble.touhou_little_maid.inner.feed_animal.max_number", chatBubbleKey);
        }
        ci.cancel();
    }
}