package com.subtitlescreen.trigger;

import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public final class SubtitleTriggerManager {

    private static final int JOIN_TRIGGER_DELAY_TICKS = 40;
    /** 玩家进服时扫描的区块半径（以玩家所在区块为中心）。 */
    private static final int SCAN_CHUNK_RADIUS = 8;
    private static final SubtitleJoinScheduler JOIN_SCHEDULER = new SubtitleJoinScheduler();

    private SubtitleTriggerManager() {
    }

    public static void scheduleJoinSubtitles(ServerPlayer player) {
        JOIN_SCHEDULER.schedule(player.getUUID(), JOIN_TRIGGER_DELAY_TICKS);
    }

    public static void tick(MinecraftServer server) {
        for (var playerId : JOIN_SCHEDULER.tick()) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
                triggerJoinSubtitles(player);
            }
        }
    }

    /**
     * 玩家进服时触发附近所有 on_join 字幕。
     *
     * <p>26.3 的 ServerLevel 不再暴露「已强制加载区块」的查询接口（只剩 setChunkForced），
     * 因此改为扫描玩家附近已经加载的区块：getChunkNow 只返回内存中已有的区块，
     * 不会因为这次扫描而生成新区块。</p>
     */
    public static void triggerJoinSubtitles(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        int centerChunkX = player.getBlockX() >> 4;
        int centerChunkZ = player.getBlockZ() >> 4;
        var chunkSource = level.getChunkSource();

        for (int dx = -SCAN_CHUNK_RADIUS; dx <= SCAN_CHUNK_RADIUS; dx++) {
            for (int dz = -SCAN_CHUNK_RADIUS; dz <= SCAN_CHUNK_RADIUS; dz++) {
                LevelChunk chunk = chunkSource.getChunkNow(centerChunkX + dx, centerChunkZ + dz);
                if (chunk == null) {
                    continue;
                }

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof SubtitleBlockEntity subtitleBlockEntity
                            && "on_join".equals(subtitleBlockEntity.getTriggerMode())) {
                        subtitleBlockEntity.triggerForPlayer(player);
                    }
                }
            }
        }
    }
}
