package com.subtitlescreen.network;

import com.subtitlescreen.SubtitleScreenMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SubtitleTriggerPayload(String text, String fontType, int duration, String textColorHex,
                                     String playerNameColorHex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SubtitleTriggerPayload> ID =
            new CustomPacketPayload.Type<>(SubtitleScreenMod.id("subtitle_trigger"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SubtitleTriggerPayload> CODEC =
            StreamCodec.of(SubtitleTriggerPayload::write, SubtitleTriggerPayload::read);

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(text);
        buf.writeUtf(fontType);
        buf.writeInt(duration);
        buf.writeUtf(textColorHex);
        buf.writeUtf(playerNameColorHex);
    }

    public static SubtitleTriggerPayload read(RegistryFriendlyByteBuf buf) {
        return new SubtitleTriggerPayload(
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
