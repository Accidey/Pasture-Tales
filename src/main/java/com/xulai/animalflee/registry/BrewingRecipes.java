package com.xulai.animalflee.registry;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

public final class BrewingRecipes {

    private BrewingRecipes() {
    }

    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, Items.WHEAT, PotionEffects.BEAST_TAMING);
        builder.addMix(PotionEffects.BEAST_TAMING, Items.CORNFLOWER, PotionEffects.BEAST_TAMING_2);
        builder.addMix(PotionEffects.BEAST_TAMING_2, Items.LILY_OF_THE_VALLEY, PotionEffects.BEAST_TAMING_3);
    }
}
