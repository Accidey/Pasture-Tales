package com.xulai.animalflee.integration.sereneseasons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

public final class SereneSeasonsCompat {

    private static final String MOD_ID = "sereneseasons";

    private static Method getSeasonState;
    private static Method getSeason;
    private static Method usesTropicalSeasons;
    private static boolean initialized;
    private static boolean available;

    private SereneSeasonsCompat() {
    }

    public static boolean isWinter(Level level, BlockPos pos) {
        initialize();
        if (!available) {
            return false;
        }
        try {
            if (Boolean.TRUE.equals(usesTropicalSeasons.invoke(null, level.getBiome(pos)))) {
                return false;
            }
            Object state = getSeasonState.invoke(null, level);
            if (state == null) {
                return false;
            }
            Object season = getSeason.invoke(state);
            return season != null && "WINTER".equals(season.toString());
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (!ModList.get().isLoaded(MOD_ID)) {
            return;
        }
        try {
            Class<?> helper = Class.forName("sereneseasons.api.season.SeasonHelper");
            Class<?> seasonState = Class.forName("sereneseasons.api.season.ISeasonState");
            getSeasonState = helper.getMethod("getSeasonState", Level.class);
            usesTropicalSeasons = helper.getMethod("usesTropicalSeasons", Holder.class);
            getSeason = seasonState.getMethod("getSeason");
            available = true;
        } catch (ReflectiveOperationException | LinkageError e) {
            available = false;
        }
    }
}
