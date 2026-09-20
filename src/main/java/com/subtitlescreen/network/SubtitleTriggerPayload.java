package com.subtitlescreen.network;

import com.subtitlescreen.SubtitleScreenMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** 服务端 -> 客户端：显示一条字幕。 */
public record SubtitleTriggerPayload(String text, String fontType, int duration, String textColorHex,
                                     String playerNameColorHex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SubtitleTriggerPayload> TYPE =
            new CustomPacketPayload.Type<>(SubtitleScreenMod.id("subtitle_trigger"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubtitleTriggerPayload> CODEC =
            StreamCodec.of(SubtitleTriggerPayload::write, SubtitleTriggerPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, SubtitleTriggerPayload payload) {
        buf.writeUtf(payload.text());
        buf.writeUtf(payload.fontType());
        buf.writeInt(payload.duration());
        buf.writeUtf(payload.textColorHex());
        buf.writeUtf(payload.playerNameColorHex());
    }

    private static SubtitleTriggerPayload read(RegistryFriendlyByteBuf buf) {
        return new SubtitleTriggerPayload(
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
