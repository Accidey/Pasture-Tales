package com.xulai.animalflee;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;

@Mod(AnimalFleeMod.MODID)
public class AnimalFleeMod {
    public static final String MODID = "animalflee";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AnimalFleeMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "pasturetales-common.toml");
    }
}
