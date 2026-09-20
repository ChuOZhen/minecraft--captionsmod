package com.subtitlescreen.block;

import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import com.subtitlescreen.registry.ModPayloads;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SubtitleBlock extends BaseEntityBlock {

    private static final long REDSTONE_COOLDOWN_TICKS = 5L;

    public SubtitleBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SubtitleBlockEntity(pos, state);
    }

    // ------------------------------------------------------------------
    // 右键打开设置界面
    //
    // 26.3 的 MenuScreens.register 是 private，模组无法再注册自己的菜单界面，因此改为：
    // 服务端校验权限后发送 SubtitleOpenScreenPayload，客户端收到后自行打开 Screen。
    // 这样服务端的类路径上也不会引用任何客户端专用类。
    // ------------------------------------------------------------------
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            // 客户端不处理，等待服务端回包
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (!canConfigure(serverPlayer)) {
            player.sendSystemMessage(Component.translatable("subtitlescreen.message.no_permission"));
            return InteractionResult.FAIL;
        }

        ModPayloads.sendOpenScreen(serverPlayer, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (state.getBlock() != oldState.getBlock() && level instanceof ServerLevel serverLevel) {
            forceChunk(serverLevel, pos);
            initializeRedstoneState(serverLevel, pos);
            announce(serverLevel, "subtitlescreen.message.chunk_forced");
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                   @Nullable Orientation orientation, boolean notify) {
        if (level instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(pos) instanceof SubtitleBlockEntity blockEntity) {
            handleRedstoneUpdate(serverLevel, pos, blockEntity);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!hasOtherSubtitleBlockInChunk(level, pos)) {
            ChunkPos chunkPos = ChunkPos.containing(pos);
            level.setChunkForced(chunkPos.x(), chunkPos.z(), false);
            announce(level, "subtitlescreen.message.chunk_unforced");
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    /** 26.3 的权限体系：GAMEMASTERS 等价于原来的 OP 等级 2。 */
    private static boolean canConfigure(ServerPlayer player) {
        if (player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return true;
        }
        var server = player.level().getServer();
        return server != null && !server.isDedicatedServer();
    }

    private static void forceChunk(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        level.setChunkForced(chunkPos.x(), chunkPos.z(), true);
    }

    private static void initializeRedstoneState(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SubtitleBlockEntity blockEntity) {
            blockEntity.setLastRedstonePowered(level.hasNeighborSignal(pos));
        }
    }

    private static void handleRedstoneUpdate(ServerLevel level, BlockPos pos, SubtitleBlockEntity blockEntity) {
        boolean powered = level.hasNeighborSignal(pos);
        boolean wasPowered = blockEntity.isLastRedstonePowered();
        blockEntity.setLastRedstonePowered(powered);

        if (!powered || wasPowered || !"redstone".equals(blockEntity.getTriggerMode())) {
            return;
        }

        long currentTime = level.getGameTime();
        if (currentTime - blockEntity.getLastTriggerGameTime() < REDSTONE_COOLDOWN_TICKS) {
            return;
        }

        blockEntity.setLastTriggerGameTime(currentTime);
        blockEntity.triggerFromRedstone();
    }

    private static boolean hasOtherSubtitleBlockInChunk(ServerLevel level, BlockPos excludedPos) {
        LevelChunk chunk = level.getChunkAt(excludedPos);

        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof SubtitleBlockEntity && !blockEntity.getBlockPos().equals(excludedPos)) {
                return true;
            }
        }

        return false;
    }

    private static void announce(ServerLevel level, String translationKey) {
        var server = level.getServer();
        if (server == null) {
            return;
        }
        var players = server.getPlayerList().getPlayers();
        if (!players.isEmpty()) {
            players.get(0).sendSystemMessage(Component.translatable(translationKey));
        }
    }
}
