package com.xulai.animalflee.registry;

import com.xulai.animalflee.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BeastTamingPotionItem extends PotionItem {

    private final int tier;

    public BeastTamingPotionItem(int tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Player player = entity instanceof Player drinker ? drinker : null;
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }
        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(
                    ModRegistry.BEAST_TAMING, BeastTamingEffect.durationTicks(tier), tier - 1));
        }
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (player == null) {
                stack = bottle;
            } else if (!player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.animalflee.beast_taming", reductionPercent(), BeastTamingEffect.durationTicks(tier) / 1200)
                .withStyle(ChatFormatting.GRAY));
    }

    private int reductionPercent() {
        return switch (tier) {
            case 1 -> Config.BEAST_TAMING_REDUCTION_1.get();
            case 2 -> Config.BEAST_TAMING_REDUCTION_2.get();
            default -> Config.BEAST_TAMING_REDUCTION_3.get();
        };
    }
}
