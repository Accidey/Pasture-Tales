package com.xulai.animalflee;

import com.mojang.logging.LogUtils;
import com.xulai.animalflee.client.FishingMinigameClientHandler;
import com.xulai.animalflee.logic.FishingMinigameServerHandler;
import com.xulai.animalflee.network.FishingMinigameOpenPacket;
import com.xulai.animalflee.network.FishingMinigameResultPacket;
import com.xulai.animalflee.registry.ModRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(AnimalFleeMod.MODID)
public class AnimalFleeMod {
    public static final String MODID = "animalflee";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AnimalFleeMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "pasturetales-common.toml");
        ModRegistry.init(modEventBus);
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::addToCreativeTab);
        modEventBus.addListener(this::registerItemColors);
        NeoForge.EVENT_BUS.addListener(this::registerBrewingRecipes);
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.getItemColors().register(
                (stack, tintIndex) -> tintIndex == 1 ? 0xB8860B : -1,
                ModRegistry.BEAST_TAMING_POTION_1.get(),
                ModRegistry.BEAST_TAMING_POTION_2.get(),
                ModRegistry.BEAST_TAMING_POTION_3.get());
    }

    private void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addRecipe(
                Ingredient.of(PotionContents.createItemStack(Items.POTION, Potions.AWKWARD)),
                Ingredient.of(Items.RABBIT_FOOT),
                new ItemStack(ModRegistry.BEAST_TAMING_POTION_1.get()));
        builder.addRecipe(
                Ingredient.of(ModRegistry.BEAST_TAMING_POTION_1.get()),
                Ingredient.of(Items.CORNFLOWER),
                new ItemStack(ModRegistry.BEAST_TAMING_POTION_2.get()));
        builder.addRecipe(
                Ingredient.of(ModRegistry.BEAST_TAMING_POTION_2.get()),
                Ingredient.of(Items.LILY_OF_THE_VALLEY),
                new ItemStack(ModRegistry.BEAST_TAMING_POTION_3.get()));
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModRegistry.BEAST_TAMING_POTION_1.get());
            event.accept(ModRegistry.BEAST_TAMING_POTION_2.get());
            event.accept(ModRegistry.BEAST_TAMING_POTION_3.get());
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(
                FishingMinigameOpenPacket.TYPE,
                FishingMinigameOpenPacket.CODEC,
                (payload, context) -> context.enqueueWork(() -> FishingMinigameClientHandler.open(payload))
        );
        registrar.playToServer(
                FishingMinigameResultPacket.TYPE,
                FishingMinigameResultPacket.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        FishingMinigameServerHandler.finish(player, payload.result());
                    }
                })
        );
    }
}
