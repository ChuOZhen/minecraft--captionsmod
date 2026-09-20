package com.subtitlescreen.registry;

import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import com.subtitlescreen.network.SubtitleTriggerPayload;
import com.subtitlescreen.network.SubtitleUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.Locale;

public final class ModPayloads {
    private static final int MIN_DURATION_TICKS = 20;
    private static final int MAX_DURATION_TICKS = 200;

    private ModPayloads() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(SubtitleUpdatePayload.ID, SubtitleUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SubtitleTriggerPayload.ID, SubtitleTriggerPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SubtitleUpdatePayload.ID, (payload, context) -> {
            var player = context.player();
            var world = player.getLevel();
            var pos = payload.pos();

            if (world.getBlockEntity(pos) instanceof SubtitleBlockEntity blockEntity
                    && canConfigure(player)
                    && world.getBlockState(pos).isOf(ModBlocks.SUBTITLE_BLOCK)) {
                blockEntity.updateFromPayload(sanitize(payload));
                world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            }
        });
    }

    public static void sendSubtitle(ServerPlayer player, String text, String fontType, int duration,
                                    String textColorHex, String playerNameColorHex) {
        if (!ServerPlayNetworking.canSend(player, SubtitleTriggerPayload.ID)) {
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

    private static boolean canConfigure(ServerPlayer player) {
        ServerLevel world = player.getServerWorld();
        return player.hasPermissionLevel(2) || !world.getServer().isDedicatedServer();
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
