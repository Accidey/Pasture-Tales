package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import com.xulai.animalflee.Config;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class FertilizedEggTooltipHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (PregnancySystem.isFertilizedEgg(stack)) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            String species = tag.getString(PregnancySystem.NBT_SPECIES);
            if (!species.isEmpty()) {
                try {
                    EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(species));
                    if (type != null) {
                        event.getToolTip().add(Component.translatable(
                                "tooltip.animalflee.fertilized_egg_species", type.getDescription()));
                        return;
                    }
                } catch (RuntimeException ignored) {
                }
            }
            event.getToolTip().add(Component.translatable("tooltip.animalflee.fertilized_egg"));
        } else if (stack.is(Items.EGG) && Config.BLOCK_NORMAL_EGG_HATCHING.get()) {
            event.getToolTip().add(Component.translatable("tooltip.animalflee.normal_egg"));
        }
    }
}