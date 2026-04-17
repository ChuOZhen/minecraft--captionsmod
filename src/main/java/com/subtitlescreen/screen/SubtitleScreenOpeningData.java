package com.subtitlescreen.screen;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;

public record SubtitleScreenOpeningData(BlockPos pos) {
    public static final PacketCodec<RegistryByteBuf, SubtitleScreenOpeningData> PACKET_CODEC =
            PacketCodec.of(SubtitleScreenOpeningData::write, SubtitleScreenOpeningData::new);

    private SubtitleScreenOpeningData(PacketByteBuf buf) {
        this(buf.readBlockPos());
    }

    private void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}
