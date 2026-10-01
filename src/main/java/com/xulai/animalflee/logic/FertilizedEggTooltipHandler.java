package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import com.xulai.animalflee.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class FertilizedEggTooltipHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.EGG)) return;
        if (PregnancySystem.isFertilizedEgg(stack)) {
            event.getToolTip().add(Component.translatable("tooltip.animalflee.fertilized_egg"));
        } else if (Config.BLOCK_NORMAL_EGG_HATCHING.get()) {
            event.getToolTip().add(Component.translatable("tooltip.animalflee.normal_egg"));
        }
    }
}