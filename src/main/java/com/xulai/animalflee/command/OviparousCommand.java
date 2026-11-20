package com.xulai.animalflee.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.OviparousRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class OviparousCommand {

    private static final String PREFIX = "animalflee.oviparous.";

    private OviparousCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pasturetales")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("oviparous")
                        .then(Commands.literal("add")
                                .executes(OviparousCommand::add))
                        .then(Commands.literal("remove")
                                .executes(OviparousCommand::remove))));
    }

    private static int add(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX + "player_only"));
            return 0;
        }

        EntityType<?> type = mainHandSpawnEggType(player, source);
        if (type == null) {
            return 0;
        }
        ResourceLocation mobKey = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (mobKey == null) {
            source.sendFailure(Component.translatable(PREFIX + "unknown_type"));
            return 0;
        }
        String mobId = mobKey.toString();

        ItemStack eggStack = player.getOffhandItem();
        if (eggStack.isEmpty()) {
            source.sendFailure(Component.translatable(PREFIX + "offhand_empty"));
            return 0;
        }
        if (eggStack.getItem() instanceof SpawnEggItem) {
            source.sendFailure(Component.translatable(PREFIX + "offhand_spawn_egg"));
            return 0;
        }

        if (!isAnimal(type, player)) {
            source.sendFailure(Component.translatable(PREFIX + "not_animal", mobId));
            return 0;
        }

        ResourceLocation eggKey = BuiltInRegistries.ITEM.getKey(eggStack.getItem());
        if (eggKey == null) {
            source.sendFailure(Component.translatable(PREFIX + "unknown_egg"));
            return 0;
        }
        String eggId = eggKey.toString();

        List<String> list = new ArrayList<>(Config.OVIPAROUS_MOBS.get());
        for (String entry : list) {
            String[] parts = entry.split(",", 2);
            if (parts.length == 2 && parts[0].trim().equals(mobId)) {
                source.sendFailure(Component.translatable(PREFIX + "already_present", mobId));
                return 0;
            }
        }

        list.add(mobId + "," + eggId);
        Config.OVIPAROUS_MOBS.set(list);
        Config.SPEC.save();
        OviparousRegistry.resetCache();
        source.sendSuccess(() -> Component.translatable(PREFIX + "added", mobId, eggId), true);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX + "player_only"));
            return 0;
        }

        EntityType<?> type = mainHandSpawnEggType(player, source);
        if (type == null) {
            return 0;
        }
        ResourceLocation mobKey = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (mobKey == null) {
            source.sendFailure(Component.translatable(PREFIX + "unknown_type"));
            return 0;
        }
        String mobId = mobKey.toString();

        List<String> list = new ArrayList<>(Config.OVIPAROUS_MOBS.get());
        boolean removed = list.removeIf(entry -> {
            String[] parts = entry.split(",", 2);
            return parts.length == 2 && parts[0].trim().equals(mobId);
        });
        if (!removed) {
            source.sendFailure(Component.translatable(PREFIX + "not_present", mobId));
            return 0;
        }

        Config.OVIPAROUS_MOBS.set(list);
        Config.SPEC.save();
        OviparousRegistry.resetCache();
        source.sendSuccess(() -> Component.translatable(PREFIX + "removed", mobId), true);
        return 1;
    }

    @Nullable
    private static EntityType<?> mainHandSpawnEggType(ServerPlayer player, CommandSourceStack source) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SpawnEggItem eggItem)) {
            source.sendFailure(Component.translatable(PREFIX + "not_spawn_egg"));
            return null;
        }
        return eggItem.getType(stack);
    }

    private static boolean isAnimal(EntityType<?> type, ServerPlayer player) {
        try {
            return type.create(player.serverLevel()) instanceof Animal;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
