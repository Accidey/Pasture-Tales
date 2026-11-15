package com.xulai.animalflee.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.xulai.animalflee.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class BaitCommand {

    private static final String PREFIX = "animalflee.bait.";

    private BaitCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pasturetales")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("bait")
                        .then(Commands.literal("add")
                                .executes(ctx -> modify(ctx, false)))
                        .then(Commands.literal("remove")
                                .executes(ctx -> modify(ctx, true)))));
    }

    private static int modify(CommandContext<CommandSourceStack> ctx, boolean remove) {
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

        List<String> list = new ArrayList<>(Config.BAIT_WHITELIST.get());
        if (remove) {
            if (!list.remove(id)) {
                source.sendFailure(Component.translatable(PREFIX + "not_present", id));
                return 0;
            }
        } else {
            if (list.contains(id)) {
                source.sendFailure(Component.translatable(PREFIX + "already_present", id));
                return 0;
            }
            list.add(id);
        }

        Config.BAIT_WHITELIST.set(list);
        Config.SPEC.save();
        source.sendSuccess(() -> Component.translatable(remove ? PREFIX + "removed" : PREFIX + "added", id), true);
        return 1;
    }
}
