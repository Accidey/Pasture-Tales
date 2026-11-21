package com.xulai.animalflee.integration.jade;

import com.xulai.animalflee.registry.FeedingTroughBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum TroughFoodProvider implements IComponentProvider<BlockAccessor>, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("animalflee", "trough_food");

    private static final String KEY_FOODS = "af_foods";
    private static final String KEY_ITEM = "item";
    private static final String KEY_COUNT = "count";
    private static final int MAX_SHOWN = 3;

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof FeedingTroughBlockEntity trough)) return;
        Map<Item, Integer> counts = new LinkedHashMap<>();
        for (ItemStack slot : trough.getItems()) {
            if (slot.isEmpty()) continue;
            counts.merge(slot.getItem(), slot.getCount(), Integer::sum);
        }
        if (counts.isEmpty()) return;
        List<Map.Entry<Item, Integer>> top = counts.entrySet().stream()
                .sorted(Map.Entry.<Item, Integer>comparingByValue().reversed())
                .limit(MAX_SHOWN)
                .toList();
        ListTag list = new ListTag();
        for (Map.Entry<Item, Integer> entry : top) {
            CompoundTag food = new CompoundTag();
            food.putString(KEY_ITEM, BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
            food.putInt(KEY_COUNT, entry.getValue());
            list.add(food);
        }
        tag.put(KEY_FOODS, list);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        ListTag list = accessor.getServerData().getList(KEY_FOODS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag food = list.getCompound(i);
            Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(food.getString(KEY_ITEM))).orElse(null);
            if (item == null || item == Items.AIR) continue;
            tooltip.add(Component.translatable("jade.animalflee.trough_food",
                    new ItemStack(item).getHoverName(), food.getInt(KEY_COUNT)));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
