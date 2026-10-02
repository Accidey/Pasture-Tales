package com.xulai.animalflee.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.AnimalFoods;
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
import java.util.Locale;

public final class DietCommand {

    private static final String PREFIX = "animalflee.diet.";

    private DietCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pasturetales")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("diet")
                        .executes(ctx -> show(ctx))
                        .then(Commands.literal("add")
                                .then(Commands.literal("herbivore").executes(ctx -> set(ctx, "herbivore")))
                                .then(Commands.literal("carnivore").executes(ctx -> set(ctx, "carnivore")))
                                .then(Commands.literal("omnivore").executes(ctx -> set(ctx, "omnivore"))))
                        .then(Commands.literal("remove")
                                .then(Commands.literal("herbivore").executes(ctx -> remove(ctx, "herbivore")))
                                .then(Commands.literal("carnivore").executes(ctx -> remove(ctx, "carnivore"))))));
    }

    private static int show(CommandContext<CommandSourceStack> ctx) {
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

        EntityType<?> type = eggItem.getType(stack);
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) {
            source.sendFailure(Component.translatable(PREFIX + "unknown_type"));
            return 0;
        }
        String id = key.toString();
        AnimalFoods.Diet diet = AnimalFoods.getDiet(type);
        source.sendSuccess(() -> Component.translatable(PREFIX + "current", id, dietComponent(diet)), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> ctx, String diet) {
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

        EntityType<?> type = eggItem.getType(stack);
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) {
            source.sendFailure(Component.translatable(PREFIX + "unknown_type"));
            return 0;
        }
        String id = key.toString();

        if ("omnivore".equals(diet)) {
            return setOmnivore(source, id);
        }

        boolean herbivore = "herbivore".equals(diet);
        List<String> target = new ArrayList<>((herbivore ? Config.HERBIVORE_MOBS : Config.CARNIVORE_MOBS).get());
        List<String> other = new ArrayList<>((herbivore ? Config.CARNIVORE_MOBS : Config.HERBIVORE_MOBS).get());

        if (target.contains(id)) {
            source.sendFailure(Component.translatable(PREFIX + "already_present", id, dietComponent(diet)));
            return 0;
        }

        other.remove(id);
        target.add(id);

        if (herbivore) {
            Config.HERBIVORE_MOBS.set(target);
            Config.CARNIVORE_MOBS.set(other);
        } else {
            Config.CARNIVORE_MOBS.set(target);
            Config.HERBIVORE_MOBS.set(other);
        }
        Config.SPEC.save();
        AnimalFoods.resetDietCache();
        source.sendSuccess(() -> Component.translatable(PREFIX + "added", id, dietComponent(diet)), true);
        return 1;
    }

    private static int setOmnivore(CommandSourceStack source, String id) {
        List<String> herb = new ArrayList<>(Config.HERBIVORE_MOBS.get());
        List<String> carn = new ArrayList<>(Config.CARNIVORE_MOBS.get());
        boolean inHerb = herb.remove(id);
        boolean inCarn = carn.remove(id);
        if (!inHerb && !inCarn) {
            source.sendFailure(Component.translatable(PREFIX + "already_present", id, dietComponent("omnivore")));
            return 0;
        }
        Config.HERBIVORE_MOBS.set(herb);
        Config.CARNIVORE_MOBS.set(carn);
        Config.SPEC.save();
        AnimalFoods.resetDietCache();
        source.sendSuccess(() -> Component.translatable(PREFIX + "added", id, dietComponent("omnivore")), true);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> ctx, String diet) {
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

        EntityType<?> type = eggItem.getType(stack);
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) {
            source.sendFailure(Component.translatable(PREFIX + "unknown_type"));
            return 0;
        }
        String id = key.toString();

        boolean herbivore = "herbivore".equals(diet);
        List<String> list = new ArrayList<>((herbivore ? Config.HERBIVORE_MOBS : Config.CARNIVORE_MOBS).get());
        if (!list.remove(id)) {
            source.sendFailure(Component.translatable(PREFIX + "not_present", id, dietComponent(diet)));
            return 0;
        }
        if (herbivore) {
            Config.HERBIVORE_MOBS.set(list);
        } else {
            Config.CARNIVORE_MOBS.set(list);
        }
        Config.SPEC.save();
        AnimalFoods.resetDietCache();
        source.sendSuccess(() -> Component.translatable(PREFIX + "removed", id, dietComponent(diet)), true);
        return 1;
    }

    private static Component dietComponent(String diet) {
        return Component.translatable(PREFIX + diet);
    }

    private static Component dietComponent(AnimalFoods.Diet diet) {
        return Component.translatable(PREFIX + diet.name().toLowerCase(Locale.ROOT));
    }
}