package com.xulai.animalflee;

import com.xulai.animalflee.integration.maid.MaidFishConsumptionHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(AnimalFleeMod.MODID)
public class AnimalFleeMod {
    public static final String MODID = "animalflee";
    public static final String MAID_MODID = "touhou_little_maid";

    public AnimalFleeMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "pasturetales-common.toml");
        if (ModList.get().isLoaded(MAID_MODID)) {
            NeoForge.EVENT_BUS.register(MaidFishConsumptionHandler.class);
        }
    }
}