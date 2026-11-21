package com.xulai.animalflee.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class CommandUtil {

    private CommandUtil() {
    }

    record SpawnEggTarget(EntityType<?> type, String id) {
    }

    @Nullable
    static String heldItemId(ServerPlayer player, CommandSourceStack source, String prefix) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable(prefix + "empty_hand"));
            return null;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) {
            source.sendFailure(Component.translatable(prefix + "unknown_item"));
            return null;
        }
        return key.toString();
    }

    @Nullable
    static EntityType<?> heldSpawnEggType(ServerPlayer player, CommandSourceStack source, String prefix) {
        SpawnEggTarget target = heldSpawnEgg(player, source, prefix);
        return target == null ? null : target.type();
    }

    @Nullable
    static SpawnEggTarget heldSpawnEgg(ServerPlayer player, CommandSourceStack source, String prefix) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SpawnEggItem eggItem)) {
            source.sendFailure(Component.translatable(prefix + "not_spawn_egg"));
            return null;
        }
        EntityType<?> type = eggItem.getType(stack);
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) {
            source.sendFailure(Component.translatable(prefix + "unknown_type"));
            return null;
        }
        return new SpawnEggTarget(type, key.toString());
    }

    @Nullable
    static String heldSpawnEggId(ServerPlayer player, CommandSourceStack source, String prefix) {
        SpawnEggTarget target = heldSpawnEgg(player, source, prefix);
        return target == null ? null : target.id();
    }

    static int showList(CommandSourceStack source, String labelKey, List<? extends String> entries) {
        Component label = Component.translatable(labelKey);
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("animalflee.command.list_empty", label), false);
        } else {
            source.sendSuccess(() -> Component.translatable("animalflee.command.list_entries", label, String.join(", ", entries)), false);
        }
        return 1;
    }
}
