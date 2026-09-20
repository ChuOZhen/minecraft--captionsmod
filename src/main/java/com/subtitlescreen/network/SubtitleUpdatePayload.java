package com.subtitlescreen.network;

import com.subtitlescreen.SubtitleScreenMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** 客户端 -> 服务端：保存某个字幕方块的设置。 */
public record SubtitleUpdatePayload(BlockPos pos, String subtitleText, String fontType, String triggerMode,
                                    int duration, String textColorHex, String playerNameColorHex)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SubtitleUpdatePayload> TYPE =
            new CustomPacketPayload.Type<>(SubtitleScreenMod.id("subtitle_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubtitleUpdatePayload> CODEC =
            StreamCodec.of(SubtitleUpdatePayload::write, SubtitleUpdatePayload::read);

    private static void write(RegistryFriendlyByteBuf buf, SubtitleUpdatePayload payload) {
        buf.writeBlockPos(payload.pos());
        buf.writeUtf(payload.subtitleText());
        buf.writeUtf(payload.fontType());
        buf.writeUtf(payload.triggerMode());
        buf.writeInt(payload.duration());
        buf.writeUtf(payload.textColorHex());
        buf.writeUtf(payload.playerNameColorHex());
    }

    private static SubtitleUpdatePayload read(RegistryFriendlyByteBuf buf) {
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
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
