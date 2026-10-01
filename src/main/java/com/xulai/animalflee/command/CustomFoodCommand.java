package com.xulai.animalflee.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.AnimalFoods;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CustomFoodCommand {

    private static final String PREFIX = "animalflee.customfood.";

    private CustomFoodCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("animalflee")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("customfood")
                        .then(Commands.literal("add")
                                .executes(ctx -> add(ctx, null))
                                .then(Commands.argument("value", FloatArgumentType.floatArg())
                                        .executes(ctx -> add(ctx, FloatArgumentType.getFloat(ctx, "value")))))
                        .then(Commands.literal("remove")
                                .executes(ctx -> remove(ctx)))));
    }

    private static int add(CommandContext<CommandSourceStack> ctx, Float value) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX + "player_only"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable(PREFIX + "empty_hand"));
            return 0;
        }

        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) {
            source.sendFailure(Component.translatable(PREFIX + "unknown_item"));
            return 0;
        }
        String id = key.toString();

        String entry;
        if (value != null) {
            if (value <= 0.0F) {
                source.sendFailure(Component.translatable(PREFIX + "value_positive"));
                return 0;
            }
            entry = id + ":" + value;
        } else {
            var food = stack.getFoodProperties(null);
            if (food == null) {
                source.sendFailure(Component.translatable(PREFIX + "no_value"));
                return 0;
            }
            entry = id;
        }

        List<String> list = new ArrayList<>(Config.CUSTOM_ANIMAL_FOODS.get());
        if (list.contains(entry)) {
            source.sendFailure(Component.translatable(PREFIX + "already_present", entry));
            return 0;
        }
        list.add(entry);
        Config.CUSTOM_ANIMAL_FOODS.set(list);
        AnimalFoods.resetCustomFoodCache();
        source.sendSuccess(() -> Component.translatable(PREFIX + "added", entry), true);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX + "player_only"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable(PREFIX + "empty_hand"));
            return 0;
        }

        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) {
            source.sendFailure(Component.translatable(PREFIX + "unknown_item"));
            return 0;
        }
        String id = key.toString();

        List<String> list = new ArrayList<>(Config.CUSTOM_ANIMAL_FOODS.get());
        boolean changed = list.removeIf(entry -> entry.equals(id) || entry.startsWith(id + ":"));
        if (!changed) {
            source.sendFailure(Component.translatable(PREFIX + "not_present", id));
            return 0;
        }

        Config.CUSTOM_ANIMAL_FOODS.set(list);
        Config.SPEC.save();
        AnimalFoods.resetCustomFoodCache();
        source.sendSuccess(() -> Component.translatable(PREFIX + "removed", id), true);
        return 1;
    }
}