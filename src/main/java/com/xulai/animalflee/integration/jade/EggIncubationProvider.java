package com.xulai.animalflee.integration.jade;

import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.PregnancySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EggIncubationProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    INSTANCE;

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("animalflee", "egg_incubation");

    private static final String KEY_INCUBATING = "af_incubating";
    private static final String KEY_PROGRESS = "af_progress";

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof ItemEntity itemEntity)) return;
        ItemStack stack = itemEntity.getItem();
        if (!PregnancySystem.isFertilizedEgg(stack)) return;

        CompoundTag data = accessor.getServerData();
        if (data.getBoolean(KEY_INCUBATING)) {
            float progress = data.getFloat(KEY_PROGRESS);
            tooltip.add(Component.translatable("jade.animalflee.egg_progress", String.format("%.0f%%", progress * 100.0F)));
        } else {
            tooltip.add(Component.translatable("jade.animalflee.egg_not_shaded"));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof ItemEntity itemEntity)) return;
        ItemStack stack = itemEntity.getItem();
        if (!PregnancySystem.isFertilizedEgg(stack)) return;

        if (isIncubating(itemEntity)) {
            CompoundTag data = itemEntity.getPersistentData();
            long elapsed = data.getLong("af_incubation");
            long total = Config.EGG_INCUBATION_MINUTES.get() * 1200L;
            float progress = Math.min(1.0F, Math.max(0.0F, (float) elapsed / (float) total));
            tag.putBoolean(KEY_INCUBATING, true);
            tag.putFloat(KEY_PROGRESS, progress);
        } else {
            tag.putBoolean(KEY_INCUBATING, false);
        }
    }

    private static boolean isIncubating(ItemEntity egg) {
        BlockPos pos = egg.blockPosition();
        BlockState here = egg.level().getBlockState(pos);
        BlockState below = egg.level().getBlockState(pos.below());
        if (!below.is(Blocks.HAY_BLOCK) && !here.is(Blocks.HAY_BLOCK)) {
            return false;
        }
        return !egg.level().canSeeSky(pos);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}