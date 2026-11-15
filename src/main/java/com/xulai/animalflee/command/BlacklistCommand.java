package com.xulai.animalflee.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.AnimalFleeEntityHelper;
import com.xulai.animalflee.logic.MobFleeLogic;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class BlacklistCommand {

    private static final String PREFIX_FLEE = "animalflee.blacklist.flee.";
    private static final String PREFIX_EXCLUDE = "animalflee.blacklist.exclude.";
    private static final String PREFIX_BAIT = "animalflee.blacklist.bait.";

    private BlacklistCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pasturetales")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("blacklist")
                        .then(Commands.literal("flee")
                                .then(Commands.literal("add")
                                        .executes(ctx -> modify(ctx, true, false)))
                                .then(Commands.literal("remove")
                                        .executes(ctx -> modify(ctx, true, true))))
                        .then(Commands.literal("exclude")
                                .then(Commands.literal("add")
                                        .executes(ctx -> modify(ctx, false, false)))
                                .then(Commands.literal("remove")
                                        .executes(ctx -> modify(ctx, false, true))))
                        .then(Commands.literal("bait")
                                .then(Commands.literal("add")
                                        .executes(ctx -> modifyBait(ctx, false)))
                                .then(Commands.literal("remove")
                                        .executes(ctx -> modifyBait(ctx, true))))));
    }

    private static int modify(CommandContext<CommandSourceStack> ctx, boolean flee, boolean remove) {
        CommandSourceStack source = ctx.getSource();
        String prefix = flee ? PREFIX_FLEE : PREFIX_EXCLUDE;
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(prefix + "player_only"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SpawnEggItem eggItem)) {
            source.sendFailure(Component.translatable(prefix + "not_spawn_egg"));
            return 0;
        }

        EntityType<?> type = eggItem.getType(stack);
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) {
            source.sendFailure(Component.translatable(prefix + "unknown_type"));
            return 0;
        }
        String id = key.toString();

        List<String> list = new ArrayList<>(flee ? Config.BLACKLISTED_MOBS.get() : Config.EXCLUDED_MOBS.get());
        boolean changed;
        if (remove) {
            changed = list.remove(id);
            if (!changed) {
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

        if (flee) {
            Config.BLACKLISTED_MOBS.set(list);
            Config.SPEC.save();
            MobFleeLogic.resetBlacklistCache();
        } else {
            Config.EXCLUDED_MOBS.set(list);
            Config.SPEC.save();
            AnimalFleeEntityHelper.resetExcludedCache();
        }

        source.sendSuccess(() -> Component.translatable(remove ? prefix + "removed" : prefix + "added", id), true);
        return 1;
    }

    private static int modifyBait(CommandContext<CommandSourceStack> ctx, boolean remove) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX_BAIT + "player_only"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable(PREFIX_BAIT + "empty_hand"));
            return 0;
        }

        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) {
            source.sendFailure(Component.translatable(PREFIX_BAIT + "unknown_item"));
            return 0;
        }
        String id = key.toString();

        List<String> list = new ArrayList<>(Config.BAIT_BLACKLIST.get());
        if (remove) {
            if (!list.remove(id)) {
                source.sendFailure(Component.translatable(PREFIX_BAIT + "not_present", id));
                return 0;
            }
        } else {
            if (list.contains(id)) {
                source.sendFailure(Component.translatable(PREFIX_BAIT + "already_present", id));
                return 0;
            }
            list.add(id);
        }

        Config.BAIT_BLACKLIST.set(list);
        Config.SPEC.save();
        source.sendSuccess(() -> Component.translatable(remove ? PREFIX_BAIT + "removed" : PREFIX_BAIT + "added", id), true);
        return 1;
    }
}
