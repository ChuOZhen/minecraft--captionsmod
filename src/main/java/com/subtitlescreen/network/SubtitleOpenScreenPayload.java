package com.subtitlescreen.network;

import com.subtitlescreen.SubtitleScreenMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 服务端 -> 客户端：请求打开该坐标方块对应的字幕设置界面。
 *
 * <p>26.3 把 MenuScreens.register 改成了 private，模组无法再把自己的 MenuType 绑定到客户端界面。
 * 因此不再走「打开 Menu」的路径，改由服务端在权限校验通过后主动通知客户端打开普通 Screen。</p>
 */
public record SubtitleOpenScreenPayload(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SubtitleOpenScreenPayload> TYPE =
            new CustomPacketPayload.Type<>(SubtitleScreenMod.id("open_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubtitleOpenScreenPayload> CODEC =
            StreamCodec.of(SubtitleOpenScreenPayload::write, SubtitleOpenScreenPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, SubtitleOpenScreenPayload payload) {
        buf.writeBlockPos(payload.pos());
    }

    private static SubtitleOpenScreenPayload read(RegistryFriendlyByteBuf buf) {
        return new SubtitleOpenScreenPayload(buf.readBlockPos());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
