package com.xulai.animalflee.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, "pasture-tales");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "pasture-tales");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "pasture-tales");

    public static final DeferredHolder<Block, FeedingTroughBlock> FEEDING_TROUGH =
            BLOCKS.register("feeding_trough", () -> new FeedingTroughBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)
                    .noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredHolder<Item, BlockItem> FEEDING_TROUGH_ITEM =
            ITEMS.register("feeding_trough", () -> new BlockItem(FEEDING_TROUGH.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FeedingTroughBlockEntity>> FEEDING_TROUGH_BE =
            BLOCK_ENTITY_TYPES.register("feeding_trough",
                    () -> BlockEntityType.Builder.of(FeedingTroughBlockEntity::new, FEEDING_TROUGH.get()).build(null));

    private ModBlocks() {
    }

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
    }
}
