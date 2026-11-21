package com.xulai.animalflee.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.AnimalFoods;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public final class ItemCommands {

    private static final String PREFIX_FOOD = "animalflee.customfood.";
    private static final String PREFIX_BAIT_ALLOW = "animalflee.bait.";
    private static final String PREFIX_BAIT_BAN = "animalflee.blacklist.bait.";
    private static final String PREFIX_TROUGH_BAN = "animalflee.blacklist.trough.";

    private ItemCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pasturetales")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("item")
                        .then(Commands.literal("food")
                                .then(Commands.literal("add")
                                        .executes(ctx -> foodAdd(ctx, null))
                                        .then(Commands.argument("value", FloatArgumentType.floatArg())
                                                .executes(ctx -> foodAdd(ctx, FloatArgumentType.getFloat(ctx, "value")))))
                                .then(Commands.literal("remove")
                                        .executes(ItemCommands::foodRemove))
                                .then(Commands.literal("list")
                                        .executes(ctx -> CommandUtil.showList(ctx.getSource(), "animalflee.command.list.customfood", Config.CUSTOM_ANIMAL_FOODS.get()))))
                        .then(Commands.literal("bait")
                                .then(Commands.literal("allow")
                                        .then(Commands.literal("add").executes(ctx -> modifyList(ctx, PREFIX_BAIT_ALLOW, Config.BAIT_WHITELIST, false)))
                                        .then(Commands.literal("remove").executes(ctx -> modifyList(ctx, PREFIX_BAIT_ALLOW, Config.BAIT_WHITELIST, true)))
                                        .then(Commands.literal("list").executes(ctx -> CommandUtil.showList(ctx.getSource(), "animalflee.command.list.bait_allow", Config.BAIT_WHITELIST.get()))))
                                .then(Commands.literal("ban")
                                        .then(Commands.literal("add").executes(ctx -> modifyList(ctx, PREFIX_BAIT_BAN, Config.BAIT_BLACKLIST, false)))
                                        .then(Commands.literal("remove").executes(ctx -> modifyList(ctx, PREFIX_BAIT_BAN, Config.BAIT_BLACKLIST, true)))
                                        .then(Commands.literal("list").executes(ctx -> CommandUtil.showList(ctx.getSource(), "animalflee.command.list.bait_ban", Config.BAIT_BLACKLIST.get())))))
                        .then(Commands.literal("trough")
                                .then(Commands.literal("ban")
                                        .then(Commands.literal("add").executes(ctx -> modifyList(ctx, PREFIX_TROUGH_BAN, Config.TROUGH_ITEM_BLACKLIST, false)))
                                        .then(Commands.literal("remove").executes(ctx -> modifyList(ctx, PREFIX_TROUGH_BAN, Config.TROUGH_ITEM_BLACKLIST, true)))
                                        .then(Commands.literal("list").executes(ctx -> CommandUtil.showList(ctx.getSource(), "animalflee.command.list.trough_ban", Config.TROUGH_ITEM_BLACKLIST.get())))))));
    }

    private static int modifyList(CommandContext<CommandSourceStack> ctx, String prefix,
            ModConfigSpec.ConfigValue<List<? extends String>> value, boolean remove) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(prefix + "player_only"));
            return 0;
        }
        String id = CommandUtil.heldItemId(player, source, prefix);
        if (id == null) return 0;

        List<String> list = new ArrayList<>(value.get());
        if (remove) {
            if (!list.remove(id)) {
                source.sendFailure(Component.translatable(prefix + "not_present", id));
                return 0;
            }
        } else {
            if (list.contains(id)) {
                source.sendFailure(Component.translatable(prefix + "already_present", id));
                return 0;
            }
            list.add(id);
        }

        value.set(list);
        Config.SPEC.save();
        source.sendSuccess(() -> Component.translatable(remove ? prefix + "removed" : prefix + "added", id), true);
        return 1;
    }

    private static int foodAdd(CommandContext<CommandSourceStack> ctx, Float value) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX_FOOD + "player_only"));
            return 0;
        }
        String id = CommandUtil.heldItemId(player, source, PREFIX_FOOD);
        if (id == null) return 0;

        String entry;
        if (value != null) {
            if (value <= 0.0F) {
                source.sendFailure(Component.translatable(PREFIX_FOOD + "value_positive"));
                return 0;
            }
            entry = id + ":" + value;
        } else {
            ItemStack stack = player.getMainHandItem();
            var food = stack.getFoodProperties(null);
            if (food == null) {
                source.sendFailure(Component.translatable(PREFIX_FOOD + "no_value"));
                return 0;
            }
            entry = id;
        }

        List<String> list = new ArrayList<>(Config.CUSTOM_ANIMAL_FOODS.get());
        if (list.contains(entry)) {
            source.sendFailure(Component.translatable(PREFIX_FOOD + "already_present", entry));
            return 0;
        }
        list.add(entry);
        Config.CUSTOM_ANIMAL_FOODS.set(list);
        Config.SPEC.save();
        AnimalFoods.resetCustomFoodCache();
        source.sendSuccess(() -> Component.translatable(PREFIX_FOOD + "added", entry), true);
        return 1;
    }

    private static int foodRemove(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX_FOOD + "player_only"));
            return 0;
        }
        String id = CommandUtil.heldItemId(player, source, PREFIX_FOOD);
        if (id == null) return 0;

        List<String> list = new ArrayList<>(Config.CUSTOM_ANIMAL_FOODS.get());
        boolean changed = list.removeIf(entry -> entry.equals(id) || entry.startsWith(id + ":"));
        if (!changed) {
            source.sendFailure(Component.translatable(PREFIX_FOOD + "not_present", id));
            return 0;
        }

        Config.CUSTOM_ANIMAL_FOODS.set(list);
        Config.SPEC.save();
        AnimalFoods.resetCustomFoodCache();
        source.sendSuccess(() -> Component.translatable(PREFIX_FOOD + "removed", id), true);
        return 1;
    }
}
