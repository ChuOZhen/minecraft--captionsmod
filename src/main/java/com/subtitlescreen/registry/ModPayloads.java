package com.subtitlescreen.registry;

import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import com.subtitlescreen.network.SubtitleOpenScreenPayload;
import com.subtitlescreen.network.SubtitleTriggerPayload;
import com.subtitlescreen.network.SubtitleUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

public final class ModPayloads {

    private static final int MIN_DURATION_TICKS = 20;
    private static final int MAX_DURATION_TICKS = 200;

    private ModPayloads() {
    }

    public static void register() {
        // 26.3 的 Fabric 把 playC2S()/playS2C() 改名为 serverboundPlay()/clientboundPlay()
        PayloadTypeRegistry.serverboundPlay().register(SubtitleUpdatePayload.TYPE, SubtitleUpdatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SubtitleTriggerPayload.TYPE, SubtitleTriggerPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SubtitleOpenScreenPayload.TYPE, SubtitleOpenScreenPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SubtitleUpdatePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = payload.pos();

            if (level.getBlockEntity(pos) instanceof SubtitleBlockEntity blockEntity
                    && canConfigure(player)
                    && level.getBlockState(pos).is(ModBlocks.SUBTITLE_BLOCK)) {
                blockEntity.updateFromPayload(sanitize(payload));
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            }
        });
    }

    /** 服务端 -> 客户端：显示字幕。 */
    public static void sendSubtitle(ServerPlayer player, String text, String fontType, int duration,
                                    String textColorHex, String playerNameColorHex) {
        if (!ServerPlayNetworking.canSend(player, SubtitleTriggerPayload.TYPE)) {
            return;
        }

        ServerPlayNetworking.send(player, new SubtitleTriggerPayload(
                text,
                fontType,
                clampDuration(duration),
                normalizeColor(textColorHex, "FFFFFF"),
                normalizeColor(playerNameColorHex, "00FF00")
        ));
    }

    /** 服务端 -> 客户端：请求打开该方块的设置界面。 */
    public static void sendOpenScreen(ServerPlayer player, BlockPos pos) {
        if (!ServerPlayNetworking.canSend(player, SubtitleOpenScreenPayload.TYPE)) {
            return;
        }

        ServerPlayNetworking.send(player, new SubtitleOpenScreenPayload(pos));
    }

    private static boolean canConfigure(ServerPlayer player) {
        return player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)
                || !player.level().getServer().isDedicatedServer();
    }

    private static SubtitleUpdatePayload sanitize(SubtitleUpdatePayload payload) {
        return new SubtitleUpdatePayload(
                payload.pos(),
                payload.subtitleText(),
                normalizeFont(payload.fontType()),
                normalizeTriggerMode(payload.triggerMode()),
                clampDuration(payload.duration()),
                normalizeColor(payload.textColorHex(), "FFFFFF"),
                normalizeColor(payload.playerNameColorHex(), "00FF00")
        );
    }

    private static String normalizeFont(String fontType) {
        if ("宋体".equals(fontType) || "楷体".equals(fontType) || "微软雅黑".equals(fontType)) {
            return fontType;
        }
        return "微软雅黑";
    }

    private static String normalizeTriggerMode(String triggerMode) {
        return "redstone".equals(triggerMode) ? "redstone" : "on_join";
    }

    private static int clampDuration(int duration) {
        return Math.max(MIN_DURATION_TICKS, Math.min(MAX_DURATION_TICKS, duration));
    }

    private static String normalizeColor(String value, String fallback) {
        if ("rainbow".equalsIgnoreCase(value)) {
            return "rainbow";
        }

        if (value != null && value.matches("(?i)[0-9a-f]{6}")) {
            return value.toUpperCase(Locale.ROOT);
        }

        return fallback;
    }
}
