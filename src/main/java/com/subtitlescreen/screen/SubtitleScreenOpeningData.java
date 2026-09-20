package com.subtitlescreen.screen;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.BlockPos;

public record SubtitleScreenOpeningData(BlockPos pos) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SubtitleScreenOpeningData> PACKET_CODEC =
            StreamCodec.of(SubtitleScreenOpeningData::write, SubtitleScreenOpeningData::new);

    private SubtitleScreenOpeningData(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}
