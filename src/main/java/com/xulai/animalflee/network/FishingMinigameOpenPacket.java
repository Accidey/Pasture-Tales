package com.xulai.animalflee.network;

import com.xulai.animalflee.AnimalFleeMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FishingMinigameOpenPacket(float catchWindow, float critShare, float speedMultiplier, int seconds) implements CustomPacketPayload {

    public static final Type<FishingMinigameOpenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AnimalFleeMod.MODID, "fishing_minigame_open"));

    public static final StreamCodec<FriendlyByteBuf, FishingMinigameOpenPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            FishingMinigameOpenPacket::catchWindow,
            ByteBufCodecs.FLOAT,
            FishingMinigameOpenPacket::critShare,
            ByteBufCodecs.FLOAT,
            FishingMinigameOpenPacket::speedMultiplier,
            ByteBufCodecs.INT,
            FishingMinigameOpenPacket::seconds,
            FishingMinigameOpenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
