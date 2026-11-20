package com.xulai.animalflee.network;

import com.xulai.animalflee.AnimalFleeMod;
import com.xulai.animalflee.logic.FishingMinigameResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FishingMinigameResultPacket(FishingMinigameResult result) implements CustomPacketPayload {

    public static final Type<FishingMinigameResultPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AnimalFleeMod.MODID, "fishing_minigame_result"));

    public static final StreamCodec<FriendlyByteBuf, FishingMinigameResultPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            packet -> packet.result().ordinal(),
            ordinal -> new FishingMinigameResultPacket(decode(ordinal))
    );

    private static FishingMinigameResult decode(int ordinal) {
        FishingMinigameResult[] values = FishingMinigameResult.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FishingMinigameResult.FAIL;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
