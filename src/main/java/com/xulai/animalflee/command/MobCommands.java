package com.xulai.animalflee.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.AnimalFleeEntityHelper;
import com.xulai.animalflee.logic.AnimalFoods;
import com.xulai.animalflee.logic.MobFleeLogic;
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
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MobCommands {

    private static final String PREFIX_DIET = "animalflee.diet.";
    private static final String PREFIX_OVIPAROUS = "animalflee.oviparous.";
    private static final String PREFIX_FLEE = "animalflee.blacklist.flee.";
    private static final String PREFIX_EXCLUDE = "animalflee.blacklist.exclude.";

    private MobCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pasturetales")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("mob")
                        .then(Commands.literal("diet")
                                .executes(MobCommands::dietShow)
                                .then(Commands.literal("add")
                                        .then(Commands.literal("herbivore").executes(ctx -> dietSet(ctx, "herbivore")))
                                        .then(Commands.literal("carnivore").executes(ctx -> dietSet(ctx, "carnivore")))
                                        .then(Commands.literal("omnivore").executes(ctx -> dietSet(ctx, "omnivore"))))
                                .then(Commands.literal("remove")
                                        .then(Commands.literal("herbivore").executes(ctx -> dietRemove(ctx, "herbivore")))
                                        .then(Commands.literal("carnivore").executes(ctx -> dietRemove(ctx, "carnivore"))))
                                .then(Commands.literal("list")
                                        .executes(MobCommands::dietList)))
                        .then(Commands.literal("oviparous")
                                .then(Commands.literal("add").executes(MobCommands::oviparousAdd))
                                .then(Commands.literal("remove").executes(MobCommands::oviparousRemove))
                                .then(Commands.literal("list").executes(MobCommands::oviparousList)))
                        .then(Commands.literal("flee")
                                .then(Commands.literal("add").executes(ctx -> modifyList(ctx, PREFIX_FLEE, Config.BLACKLISTED_MOBS, false, MobFleeLogic::resetBlacklistCache)))
                                .then(Commands.literal("remove").executes(ctx -> modifyList(ctx, PREFIX_FLEE, Config.BLACKLISTED_MOBS, true, MobFleeLogic::resetBlacklistCache)))
                                .then(Commands.literal("list").executes(ctx -> CommandUtil.showList(ctx.getSource(), "animalflee.command.list.flee", Config.BLACKLISTED_MOBS.get()))))
                        .then(Commands.literal("exclude")
                                .then(Commands.literal("add").executes(ctx -> modifyList(ctx, PREFIX_EXCLUDE, Config.EXCLUDED_MOBS, false, AnimalFleeEntityHelper::resetExcludedCache)))
                                .then(Commands.literal("remove").executes(ctx -> modifyList(ctx, PREFIX_EXCLUDE, Config.EXCLUDED_MOBS, true, AnimalFleeEntityHelper::resetExcludedCache)))
                                .then(Commands.literal("list").executes(ctx -> CommandUtil.showList(ctx.getSource(), "animalflee.command.list.exclude", Config.EXCLUDED_MOBS.get()))))));
    }

    private static int modifyList(CommandContext<CommandSourceStack> ctx, String prefix,
            ModConfigSpec.ConfigValue<List<? extends String>> value, boolean remove, Runnable afterSave) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(prefix + "player_only"));
            return 0;
        }
        String id = CommandUtil.heldSpawnEggId(player, source, prefix);
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
        afterSave.run();
        source.sendSuccess(() -> Component.translatable(remove ? prefix + "removed" : prefix + "added", id), true);
        return 1;
    }

    private static int dietShow(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX_DIET + "player_only"));
            return 0;
        }
        CommandUtil.SpawnEggTarget target = CommandUtil.heldSpawnEgg(player, source, PREFIX_DIET);
        if (target == null) return 0;

        AnimalFoods.Diet diet = AnimalFoods.getDiet(target.type());
        source.sendSuccess(() -> Component.translatable(PREFIX_DIET + "current", target.id(), dietComponent(diet)), false);
        return 1;
    }

    private static int dietSet(CommandContext<CommandSourceStack> ctx, String diet) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX_DIET + "player_only"));
            return 0;
        }
        CommandUtil.SpawnEggTarget target = CommandUtil.heldSpawnEgg(player, source, PREFIX_DIET);
        if (target == null) return 0;
        String id = target.id();

        if ("omnivore".equals(diet)) {
            return setOmnivore(source, id);
        }

        boolean herbivore = "herbivore".equals(diet);
        List<String> targetList = new ArrayList<>((herbivore ? Config.HERBIVORE_MOBS : Config.CARNIVORE_MOBS).get());
        List<String> otherList = new ArrayList<>((herbivore ? Config.CARNIVORE_MOBS : Config.HERBIVORE_MOBS).get());

        if (targetList.contains(id)) {
            source.sendFailure(Component.translatable(PREFIX_DIET + "already_present", id, dietComponent(diet)));
            return 0;
        }

        otherList.remove(id);
        targetList.add(id);

        if (herbivore) {
            Config.HERBIVORE_MOBS.set(targetList);
            Config.CARNIVORE_MOBS.set(otherList);
        } else {
            Config.CARNIVORE_MOBS.set(targetList);
            Config.HERBIVORE_MOBS.set(otherList);
        }
        Config.SPEC.save();
        AnimalFoods.resetDietCache();
        source.sendSuccess(() -> Component.translatable(PREFIX_DIET + "added", id, dietComponent(diet)), true);
        return 1;
    }

    private static int setOmnivore(CommandSourceStack source, String id) {
        List<String> herb = new ArrayList<>(Config.HERBIVORE_MOBS.get());
        List<String> carn = new ArrayList<>(Config.CARNIVORE_MOBS.get());
        boolean inHerb = herb.remove(id);
        boolean inCarn = carn.remove(id);
        if (!inHerb && !inCarn) {
            source.sendFailure(Component.translatable(PREFIX_DIET + "already_present", id, dietComponent("omnivore")));
            return 0;
        }
        Config.HERBIVORE_MOBS.set(herb);
        Config.CARNIVORE_MOBS.set(carn);
        Config.SPEC.save();
        AnimalFoods.resetDietCache();
        source.sendSuccess(() -> Component.translatable(PREFIX_DIET + "added", id, dietComponent("omnivore")), true);
        return 1;
    }

    private static int dietRemove(CommandContext<CommandSourceStack> ctx, String diet) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX_DIET + "player_only"));
            return 0;
        }
        String id = CommandUtil.heldSpawnEggId(player, source, PREFIX_DIET);
        if (id == null) return 0;

        boolean herbivore = "herbivore".equals(diet);
        List<String> list = new ArrayList<>((herbivore ? Config.HERBIVORE_MOBS : Config.CARNIVORE_MOBS).get());
        if (!list.remove(id)) {
            source.sendFailure(Component.translatable(PREFIX_DIET + "not_present", id, dietComponent(diet)));
            return 0;
        }
        if (herbivore) {
            Config.HERBIVORE_MOBS.set(list);
        } else {
            Config.CARNIVORE_MOBS.set(list);
        }
        Config.SPEC.save();
        AnimalFoods.resetDietCache();
        source.sendSuccess(() -> Component.translatable(PREFIX_DIET + "removed", id, dietComponent(diet)), true);
        return 1;
    }

    private static int dietList(CommandContext<CommandSourceStack> ctx) {
        CommandUtil.showList(ctx.getSource(), "animalflee.diet.herbivore", Config.HERBIVORE_MOBS.get());
        return CommandUtil.showList(ctx.getSource(), "animalflee.diet.carnivore", Config.CARNIVORE_MOBS.get());
    }

    private static int oviparousAdd(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX_OVIPAROUS + "player_only"));
            return 0;
        }

        EntityType<?> type = CommandUtil.heldSpawnEggType(player, source, PREFIX_OVIPAROUS);
        if (type == null) {
            return 0;
        }
        ResourceLocation mobKey = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (mobKey == null) {
            source.sendFailure(Component.translatable(PREFIX_OVIPAROUS + "unknown_type"));
            return 0;
        }
        String mobId = mobKey.toString();

        ItemStack eggStack = player.getOffhandItem();
        if (eggStack.isEmpty()) {
            source.sendFailure(Component.translatable(PREFIX_OVIPAROUS + "offhand_empty"));
            return 0;
        }
        if (eggStack.getItem() instanceof net.minecraft.world.item.SpawnEggItem) {
            source.sendFailure(Component.translatable(PREFIX_OVIPAROUS + "offhand_spawn_egg"));
            return 0;
        }

        if (!isAnimal(type, player)) {
            source.sendFailure(Component.translatable(PREFIX_OVIPAROUS + "not_animal", mobId));
            return 0;
        }

        ResourceLocation eggKey = BuiltInRegistries.ITEM.getKey(eggStack.getItem());
        if (eggKey == null) {
            source.sendFailure(Component.translatable(PREFIX_OVIPAROUS + "unknown_egg"));
            return 0;
        }
        String eggId = eggKey.toString();

        List<String> list = new ArrayList<>(Config.OVIPAROUS_MOBS.get());
        for (String entry : list) {
            String[] parts = entry.split(",", 2);
            if (parts.length == 2 && parts[0].trim().equals(mobId)) {
                source.sendFailure(Component.translatable(PREFIX_OVIPAROUS + "already_present", mobId));
                return 0;
            }
        }

        list.add(mobId + "," + eggId);
        Config.OVIPAROUS_MOBS.set(list);
        Config.SPEC.save();
        OviparousRegistry.resetCache();
        source.sendSuccess(() -> Component.translatable(PREFIX_OVIPAROUS + "added", mobId, eggId), true);
        return 1;
    }

    private static int oviparousRemove(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(PREFIX_OVIPAROUS + "player_only"));
            return 0;
        }

        EntityType<?> type = CommandUtil.heldSpawnEggType(player, source, PREFIX_OVIPAROUS);
        if (type == null) {
            return 0;
        }
        ResourceLocation mobKey = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (mobKey == null) {
            source.sendFailure(Component.translatable(PREFIX_OVIPAROUS + "unknown_type"));
            return 0;
        }
        String mobId = mobKey.toString();

        List<String> list = new ArrayList<>(Config.OVIPAROUS_MOBS.get());
        boolean removed = list.removeIf(entry -> {
            String[] parts = entry.split(",", 2);
            return parts.length == 2 && parts[0].trim().equals(mobId);
        });
        if (!removed) {
            source.sendFailure(Component.translatable(PREFIX_OVIPAROUS + "not_present", mobId));
            return 0;
        }

        Config.OVIPAROUS_MOBS.set(list);
        Config.SPEC.save();
        OviparousRegistry.resetCache();
        source.sendSuccess(() -> Component.translatable(PREFIX_OVIPAROUS + "removed", mobId), true);
        return 1;
    }

    private static int oviparousList(CommandContext<CommandSourceStack> ctx) {
        return CommandUtil.showList(ctx.getSource(), "animalflee.command.list.oviparous", Config.OVIPAROUS_MOBS.get());
    }

    private static boolean isAnimal(EntityType<?> type, ServerPlayer player) {
        try {
            return type.create(player.serverLevel()) instanceof Animal;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static Component dietComponent(String diet) {
        return Component.translatable(PREFIX_DIET + diet);
    }

    private static Component dietComponent(AnimalFoods.Diet diet) {
        return Component.translatable(PREFIX_DIET + diet.name().toLowerCase(Locale.ROOT));
    }
}
