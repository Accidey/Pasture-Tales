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
                                        .executes(ctx -> modify(ctx, false, true))))));
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
}
