package com.xulai.animalflee.integration.jade;

import com.xulai.animalflee.logic.AnimalFleeEntityHelper;
import com.xulai.animalflee.logic.GenderLogic;
import com.xulai.animalflee.logic.GrowthSystem;
import com.xulai.animalflee.logic.HungerSystem;
import com.xulai.animalflee.logic.PregnancySystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
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
    private static final String KEY_COOLDOWN_REMAINING = "af_cooldown_remaining";
    private static final String KEY_DOMESTICATED = "af_domesticated";
    private static final String KEY_GROWTH_PROGRESS = "af_growth_progress";
    private static final String KEY_GROWTH_PAUSED = "af_growth_paused";
    private static final String KEY_FEATHER_REMAINING = "af_feather_remaining";
    private static final String KEY_NEXT_EGG_REMAINING = "af_next_egg_remaining";

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof Animal)) return;
        if (AnimalFleeEntityHelper.isExcluded(accessor.getEntity())) return;
        if (AnimalFleeEntityHelper.isMaidEntity(accessor.getEntity())) return;

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

        if (data.getBoolean(KEY_PREGNANT) && !(accessor.getEntity() instanceof Animal animal && PregnancySystem.isOviparous(animal))) {
            if (data.contains(KEY_PREGNANCY_PROGRESS)) {
                float progress = data.getFloat(KEY_PREGNANCY_PROGRESS);
                tooltip.add(Component.translatable("jade.animalflee.pregnancy_progress", String.format("%.0f%%", progress * 100.0F)));
            } else {
                tooltip.add(Component.translatable("jade.animalflee.pregnant"));
            }
        }

        if (data.contains(KEY_COOLDOWN_REMAINING)) {
            long remaining = data.getLong(KEY_COOLDOWN_REMAINING);
            int totalSeconds = (int) (remaining * 60 / 1000);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            tooltip.add(Component.translatable("jade.animalflee.cooldown", minutes, seconds));
        }

        if (data.contains(KEY_NEXT_EGG_REMAINING)) {
            long remaining = data.getLong(KEY_NEXT_EGG_REMAINING);
            int totalSeconds = (int) (remaining * 60 / 1000);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            tooltip.add(Component.translatable("jade.animalflee.next_egg", minutes, seconds));
        }

        if (data.contains(KEY_FEATHER_REMAINING)) {
            long remaining = data.getLong(KEY_FEATHER_REMAINING);
            int totalSeconds = (int) (remaining * 60 / 1000);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            tooltip.add(Component.translatable("jade.animalflee.feather_countdown", minutes, seconds));
        }

        if (accessor.getEntity() instanceof Animal animal && animal.isBaby()) {
            if (data.contains(KEY_GROWTH_PROGRESS)) {
                tooltip.add(Component.translatable("jade.animalflee.growth_progress",
                        String.format("%.0f%%", data.getFloat(KEY_GROWTH_PROGRESS) * 100.0F)));
            } else {
                tooltip.add(Component.translatable("jade.animalflee.growth_paused"));
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
        if (AnimalFleeEntityHelper.isExcluded(animal)) return;
        if (AnimalFleeEntityHelper.isMaidEntity(animal)) return;

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
        if (PregnancySystem.isOviparous(animal)) {
            long next = animal.getPersistentData().getLong("af_next_egg_daytime");
            long remaining = next - animal.level().getDayTime();
            if (remaining > 0L) {
                tag.putLong(KEY_NEXT_EGG_REMAINING, remaining);
            }
        } else {
            long cooldown = PregnancySystem.getCooldownRemainingTicks(animal);
            if (cooldown > 0L) {
                tag.putLong(KEY_COOLDOWN_REMAINING, cooldown);
            }
        }
        if (animal.isBaby()) {
            tag.putFloat(KEY_GROWTH_PROGRESS, GrowthSystem.getProgress(animal));
            if (!GrowthSystem.hasEnoughHunger(animal)) {
                tag.putBoolean(KEY_GROWTH_PAUSED, true);
            }
        }

        if (persistent.contains(KEY_HUNGER)) {
            tag.putFloat(KEY_HUNGER, persistent.getFloat(KEY_HUNGER));
        }
        if (persistent.contains(KEY_MAX_HUNGER)) {
            tag.putFloat(KEY_MAX_HUNGER, persistent.getFloat(KEY_MAX_HUNGER));
        }

        if (animal instanceof Chicken && !animal.isBaby() && persistent.contains("af_next_feather_daytime")) {
            long next = persistent.getLong("af_next_feather_daytime");
            long remaining = next - animal.level().getDayTime();
            if (remaining > 0L) {
                tag.putLong(KEY_FEATHER_REMAINING, remaining);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}