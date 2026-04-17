package com.subtitlescreen.trigger;

import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public final class SubtitleTriggerManager {
    private static final int JOIN_TRIGGER_DELAY_TICKS = 40;
    private static final SubtitleJoinScheduler JOIN_SCHEDULER = new SubtitleJoinScheduler();

    private SubtitleTriggerManager() {
    }

    public static void scheduleJoinSubtitles(ServerPlayerEntity player) {
        JOIN_SCHEDULER.schedule(player.getUuid(), JOIN_TRIGGER_DELAY_TICKS);
    }

    public static void tick(MinecraftServer server) {
        for (var playerId : JOIN_SCHEDULER.tick()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player != null) {
                triggerJoinSubtitles(player);
            }
        }
    }

    public static void triggerJoinSubtitles(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        for (long packedChunkPos : world.getForcedChunks()) {
            WorldChunk chunk = world.getChunk(ChunkPos.getPackedX(packedChunkPos), ChunkPos.getPackedZ(packedChunkPos));

            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof SubtitleBlockEntity subtitleBlockEntity
                        && "on_join".equals(subtitleBlockEntity.getTriggerMode())) {
                    subtitleBlockEntity.triggerForPlayer(player);
                }
            }
        }
    }
}
