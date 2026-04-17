package com.subtitlescreen.network;

import com.subtitlescreen.SubtitleScreenMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SubtitleUpdatePayload(BlockPos pos, String subtitleText, String fontType, String triggerMode,
                                    int duration, String textColorHex, String playerNameColorHex)
        implements CustomPayload {
    public static final CustomPayload.Id<SubtitleUpdatePayload> ID =
            new CustomPayload.Id<>(SubtitleScreenMod.id("subtitle_update"));
    public static final PacketCodec<RegistryByteBuf, SubtitleUpdatePayload> CODEC =
            CustomPayload.codecOf(SubtitleUpdatePayload::write, SubtitleUpdatePayload::read);

    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeString(subtitleText);
        buf.writeString(fontType);
        buf.writeString(triggerMode);
        buf.writeInt(duration);
        buf.writeString(textColorHex);
        buf.writeString(playerNameColorHex);
    }

    public static SubtitleUpdatePayload read(PacketByteBuf buf) {
        return new SubtitleUpdatePayload(
            buf.readBlockPos(),
            buf.readString(),
            buf.readString(),
            buf.readString(),
            buf.readInt(),
            buf.readString(),
            buf.readString()
        );
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
