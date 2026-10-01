package com.xulai.animalflee.logic;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BookGiftSavedData extends SavedData {

    private static final String DATA_NAME = "animalflee_book_gift";
    private static final String KEY_UUIDS = "GiftedPlayers";
    private static final String KEY_UUID = "UUID";

    private final Set<UUID> giftedPlayers = new HashSet<>();

    public static BookGiftSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(BookGiftSavedData::new, BookGiftSavedData::load),
                DATA_NAME);
    }

    public boolean hasGifted(UUID playerUuid) {
        return giftedPlayers.contains(playerUuid);
    }

    public void markGifted(UUID playerUuid) {
        giftedPlayers.add(playerUuid);
        setDirty();
    }

    public static BookGiftSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        BookGiftSavedData data = new BookGiftSavedData();
        ListTag list = tag.getList(KEY_UUIDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID uuid = entry.getUUID(KEY_UUID);
            if (uuid != null) {
                data.giftedPlayers.add(uuid);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (UUID uuid : giftedPlayers) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(KEY_UUID, uuid);
            list.add(entry);
        }
        tag.put(KEY_UUIDS, list);
        return tag;
    }
}