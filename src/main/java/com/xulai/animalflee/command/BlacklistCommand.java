package com.xulai.animalflee.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.xulai.animalflee.Config;
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

public final class BlacklistCommand {

    private static final String PREFIX = "animalflee.blacklist.";

    private BlacklistCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("animalflee")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("blacklist")
                        .then(Commands.literal("add")
                                .executes(ctx -> add(ctx, false)))
                        .then(Commands.literal("remove")
                                .executes(ctx -> add(ctx, true)))));
    }

    private static int add(CommandContext<CommandSourceStack> ctx, boolean remove) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX + "player_only"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SpawnEggItem eggItem)) {
            source.sendFailure(Component.translatable(PREFIX + "not_spawn_egg"));
            return 0;
        }

        EntityType<?> type = eggItem.getType(stack.getTag());
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) {
            source.sendFailure(Component.translatable(PREFIX + "unknown_type"));
            return 0;
        }
        String id = key.toString();

        List<String> list = new ArrayList<>(Config.BLACKLISTED_MOBS.get());
        boolean changed;
        if (remove) {
            changed = list.remove(id);
        } else {
            if (list.contains(id)) {
                source.sendFailure(Component.translatable(PREFIX + "already_present", id));
                return 0;
            }
            changed = list.add(id);
        }

        if (!changed) {
            source.sendFailure(Component.translatable(PREFIX + "not_present", id));
            return 0;
        }

        Config.BLACKLISTED_MOBS.set(list);
        Config.SPEC.save();
        MobFleeLogic.resetBlacklistCache();
        source.sendSuccess(() -> Component.translatable(remove ? PREFIX + "removed" : PREFIX + "added", id), true);
        return 1;
    }
}