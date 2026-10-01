package com.xulai.animalflee.integration.jade;

import com.xulai.animalflee.logic.GenderLogic;
import com.xulai.animalflee.logic.HungerSystem;
import com.xulai.animalflee.logic.PregnancySystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum AnimalStatusProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    INSTANCE;

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("animalflee", "animal_status");

    private static final String KEY_GENDER = "af_gender";
    private static final String KEY_PREGNANT = "af_pregnant";
    private static final String KEY_PREGNANCY_PROGRESS = "af_pregnancy_progress";
    private static final String KEY_HUNGER = "af_hunger";
    private static final String KEY_MAX_HUNGER = "af_max_hunger";
    private static final String KEY_DOMESTICATED = "af_domesticated";

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Animal)) return;

        CompoundTag data = accessor.getServerData();
        if (!data.contains(KEY_GENDER)) {
            tooltip.add(Component.translatable("jade.animalflee.gender.unknown"));
            return;
        }

        String gender = data.getString(KEY_GENDER);
        Component genderText = GenderLogic.GENDER_MALE.equals(gender)
                ? Component.translatable("jade.animalflee.gender.male")
                : Component.translatable("jade.animalflee.gender.female");
        tooltip.add(Component.translatable("jade.animalflee.gender", genderText));

        if (data.getBoolean(KEY_DOMESTICATED)) {
            tooltip.add(Component.translatable("jade.animalflee.domesticated"));
        }

        if (data.getBoolean(KEY_PREGNANT)) {
            if (data.contains(KEY_PREGNANCY_PROGRESS)) {
                float progress = data.getFloat(KEY_PREGNANCY_PROGRESS);
                tooltip.add(Component.translatable("jade.animalflee.pregnancy_progress", String.format("%.0f%%", progress * 100.0F)));
            } else {
                tooltip.add(Component.translatable("jade.animalflee.pregnant"));
            }
        }

        if (data.contains(KEY_HUNGER)) {
            float hunger = data.getFloat(KEY_HUNGER);
            if (hunger <= 0.0F) {
                tooltip.add(Component.translatable("jade.animalflee.starving"));
            } else {
                float max = data.contains(KEY_MAX_HUNGER) ? data.getFloat(KEY_MAX_HUNGER) : HungerSystem.MAX_HUNGER;
                tooltip.add(Component.translatable("jade.animalflee.hunger", String.format("%.0f/%.0f", hunger, max)));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof Animal animal)) return;

        GenderLogic.initGender(animal);
        String gender = GenderLogic.getGender(animal);
        if (!gender.isEmpty()) {
            tag.putString(KEY_GENDER, gender);
        }

        CompoundTag persistent = animal.getPersistentData();
        if (persistent.getBoolean(KEY_DOMESTICATED)) {
            tag.putBoolean(KEY_DOMESTICATED, true);
        }
        if (PregnancySystem.isPregnant(animal)) {
            tag.putBoolean(KEY_PREGNANT, true);
            tag.putFloat(KEY_PREGNANCY_PROGRESS, PregnancySystem.getProgress(animal));
        }
        if (persistent.contains(KEY_HUNGER)) {
            tag.putFloat(KEY_HUNGER, persistent.getFloat(KEY_HUNGER));
        }
        if (persistent.contains(KEY_MAX_HUNGER)) {
            tag.putFloat(KEY_MAX_HUNGER, persistent.getFloat(KEY_MAX_HUNGER));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}