package com.xulai.animalflee.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public class FishFeedingSavedData extends SavedData {

    private static final String DATA_NAME = "animalflee_fish_feeding";

    private final List<Entry> entries = new ArrayList<>();

    public static FishFeedingSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(FishFeedingSavedData::new, FishFeedingSavedData::load),
                DATA_NAME);
    }

    public void addEntry(BlockPos pos, long startDayTime, int targetDelta, int count) {
        entries.add(new Entry(pos, startDayTime, targetDelta, count));
        setDirty();
    }

    public void tick(ServerLevel level) {
        if (entries.isEmpty()) return;
        long dayTime = level.getDayTime();

        List<Entry> due = new ArrayList<>();
        for (Entry entry : entries) {
            long delta = dayTime - entry.startDayTime;
            if (delta >= entry.targetDelta) {
                due.add(entry);
            }
        }
        for (Entry entry : due) {
            FishPopulationHandler.spawnFish(level, entry.pos, entry.count);
            entries.remove(entry);
            setDirty();
        }
    }

    public static FishFeedingSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        FishFeedingSavedData data = new FishFeedingSavedData();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            BlockPos pos = new BlockPos(entryTag.getInt("X"), entryTag.getInt("Y"), entryTag.getInt("Z"));
            long startDayTime = entryTag.getLong("StartDayTime");
            int targetDelta = entryTag.getInt("TargetDelta");
            int count = entryTag.getInt("Count");
            data.entries.add(new Entry(pos, startDayTime, targetDelta, count));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("X", entry.pos.getX());
            entryTag.putInt("Y", entry.pos.getY());
            entryTag.putInt("Z", entry.pos.getZ());
            entryTag.putLong("StartDayTime", entry.startDayTime);
            entryTag.putInt("TargetDelta", entry.targetDelta);
            entryTag.putInt("Count", entry.count);
            list.add(entryTag);
        }
        tag.put("Entries", list);
        return tag;
    }

    private record Entry(BlockPos pos, long startDayTime, int targetDelta, int count) {
    }
}