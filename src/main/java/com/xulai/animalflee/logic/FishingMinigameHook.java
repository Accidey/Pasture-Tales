package com.xulai.animalflee.logic;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

public interface FishingMinigameHook {

    void animalflee$acceptLoot(ObjectArrayList<ItemStack> loot, LootParams params);

    void animalflee$finishMinigame(FishingMinigameResult result);

    @Nullable
    AbstractFish animalflee$getMinigameTargetFish();
}
