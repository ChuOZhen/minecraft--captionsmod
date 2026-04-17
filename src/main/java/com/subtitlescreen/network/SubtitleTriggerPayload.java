package com.subtitlescreen.network;

import com.subtitlescreen.SubtitleScreenMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SubtitleTriggerPayload(String text, String fontType, int duration, String textColorHex,
                                     String playerNameColorHex) implements CustomPayload {
    public static final CustomPayload.Id<SubtitleTriggerPayload> ID =
            new CustomPayload.Id<>(SubtitleScreenMod.id("subtitle_trigger"));
    public static final PacketCodec<RegistryByteBuf, SubtitleTriggerPayload> CODEC =
            CustomPayload.codecOf(SubtitleTriggerPayload::write, SubtitleTriggerPayload::read);

    public void write(PacketByteBuf buf) {
        buf.writeString(text);
        buf.writeString(fontType);
        buf.writeInt(duration);
        buf.writeString(textColorHex);
        buf.writeString(playerNameColorHex);
    }

    public static SubtitleTriggerPayload read(PacketByteBuf buf) {
        return new SubtitleTriggerPayload(
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
