package com.subtitlescreen.network;

import com.subtitlescreen.SubtitleScreenMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.BlockPos;

public record SubtitleUpdatePayload(BlockPos pos, String subtitleText, String fontType, String triggerMode,
                                    int duration, String textColorHex, String playerNameColorHex)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SubtitleUpdatePayload> ID =
            new CustomPacketPayload.Type<>(SubtitleScreenMod.id("subtitle_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SubtitleUpdatePayload> CODEC =
            StreamCodec.of(SubtitleUpdatePayload::write, SubtitleUpdatePayload::read);

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(subtitleText);
        buf.writeUtf(fontType);
        buf.writeUtf(triggerMode);
        buf.writeInt(duration);
        buf.writeUtf(textColorHex);
        buf.writeUtf(playerNameColorHex);
    }

    public static SubtitleUpdatePayload read(RegistryFriendlyByteBuf buf) {
        return new SubtitleUpdatePayload(
            buf.readBlockPos(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readInt(),
            buf.readUtf(),
            buf.readUtf()
        );
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> getId() {
        return ID;
    }
}
