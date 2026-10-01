package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.lang.reflect.Method;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class BookGiveHandler {

    private static final String BOOK_ID = "animalflee:animal_husbandry_guide";

    private BookGiveHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getServer() == null) return;
        ServerLevel level = player.getServer().overworld();
        BookGiftSavedData data = BookGiftSavedData.get(level);
        if (data.hasGifted(player.getUUID())) return;

        ItemStack book = createBookStack();
        if (book == null || book.isEmpty()) return;

        data.markGifted(player.getUUID());
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
    }

    private static ItemStack createBookStack() {
        try {
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Object api = apiClass.getMethod("get").invoke(null);
            if (api == null) return null;
            Method getBookStack = api.getClass().getMethod("getBookStack", ResourceLocation.class);
            Object result = getBookStack.invoke(api, ResourceLocation.parse(BOOK_ID));
            return result instanceof ItemStack itemStack ? itemStack : null;
        } catch (Exception e) {
            return null;
        }
    }
}