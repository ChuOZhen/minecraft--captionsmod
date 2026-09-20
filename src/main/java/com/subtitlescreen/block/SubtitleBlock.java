package com.subtitlescreen.block;

import com.mojang.serialization.MapCodec;
import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import com.subtitlescreen.screen.SubtitleScreenHandler;
import com.subtitlescreen.screen.SubtitleScreenOpeningData;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.level.level.block.Block;
import net.minecraft.level.level.block.BaseEntityBlock;
import net.minecraft.level.level.block.Blocks;
import net.minecraft.level.level.block.EntityBlock;
import net.minecraft.level.level.block.RenderShape;
import net.minecraft.level.level.block.state.BlockState;
import net.minecraft.level.level.block.state.BlockBehaviour;
import net.minecraft.level.level.block.entity.BlockEntity;
import net.minecraft.level.level.block.entity.BlockEntityTicker;
import net.minecraft.level.level.block.entity.BlockEntityType;
import net.minecraft.level.entity.LivingEntity;
import net.minecraft.level.entity.player.Player;
import net.minecraft.level.item.ItemStack;
import net.minecraft.level.MenuProvider;
import net.minecraft.level.inventory.ContainerLevelAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.level.InteractionResult;
import net.minecraft.level.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.level.level.ChunkPos;
import net.minecraft.level.level.Level;
import net.minecraft.level.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public class SubtitleBlock extends BaseEntityBlock {
    public static final MapCodec<SubtitleBlock> CODEC = createCodec(SubtitleBlock::new);
    private static final long REDSTONE_COOLDOWN_TICKS = 5L;
	    
    public SubtitleBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SubtitleBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClient) {
            if (canConfigure(player, world)) {
                MenuProvider screenHandlerFactory = createScreenHandlerFactory(world, pos);
                player.openMenu(screenHandlerFactory);
                return InteractionResult.SUCCESS;
            }

            player.sendSystemMessage(Component.translatable("subtitlescreen.message.no_permission"), false);
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onBlockAdded(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.getBlock() != oldState.getBlock() && world instanceof ServerLevel serverWorld) {
            forceChunk(serverWorld, pos);
            initializeRedstoneState(serverWorld, pos);
        }
    }

    @Override
    public void onPlaced(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world instanceof ServerLevel serverWorld) {
            forceChunk(serverWorld, pos);
            initializeRedstoneState(serverWorld, pos);

            if (placer instanceof Player player) {
                player.sendSystemMessage(Component.translatable("subtitlescreen.message.chunk_forced"), false);
            }
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos,
                                  boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

        if (world instanceof ServerLevel serverWorld
                && serverWorld.getBlockEntity(pos) instanceof SubtitleBlockEntity blockEntity) {
            handleRedstoneUpdate(serverWorld, pos, blockEntity);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient && world instanceof ServerLevel serverWorld && state.getBlock() != newState.getBlock()) {
            ChunkPos chunkPos = new ChunkPos(pos);
            if (!hasOtherSubtitleBlockInChunk(serverWorld, pos)) {
                serverWorld.setChunkForced(chunkPos.x, chunkPos.z, false);

                if (serverWorld.getServer().getPlayerList().getPlayerList().size() > 0) {
                    var player = serverWorld.getServer().getPlayerList().getPlayerList().get(0);
                    player.sendSystemMessage(Component.translatable("subtitlescreen.message.chunk_unforced"), false);
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public RenderShape getRenderType(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return false;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    private static boolean canConfigure(Player player, Level world) {
        return player.hasPermissionLevel(2) || world.getServer() != null && !world.getServer().isDedicatedServer();
    }

    private static MenuProvider createScreenHandlerFactory(Level world, BlockPos pos) {
        return new ExtendedMenuProvider<SubtitleScreenOpeningData>() {
            @Override
            public SubtitleScreenOpeningData getScreenOpeningData(ServerPlayer player) {
                return new SubtitleScreenOpeningData(pos);
            }

            @Override
            public Text getDisplayName() {
                return Component.translatable("screen.subtitlescreen.subtitle_screen");
            }

            @Override
            public SubtitleScreenHandler createMenu(int syncId, net.minecraft.level.entity.player.Inventory inventory,
                                                    Player player) {
                return new SubtitleScreenHandler(syncId, inventory, ContainerLevelAccess.create(world, pos), pos);
            }
        };
    }

    private static void forceChunk(ServerLevel world, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        world.setChunkForced(chunkPos.x, chunkPos.z, true);
    }

    private static void initializeRedstoneState(ServerLevel world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof SubtitleBlockEntity blockEntity) {
            blockEntity.setLastRedstonePowered(world.hasNeighborSignal(pos));
        }
    }

    private static void handleRedstoneUpdate(ServerLevel world, BlockPos pos, SubtitleBlockEntity blockEntity) {
        boolean powered = world.hasNeighborSignal(pos);
        boolean wasPowered = blockEntity.isLastRedstonePowered();
        blockEntity.setLastRedstonePowered(powered);

        if (!powered || wasPowered || !"redstone".equals(blockEntity.getTriggerMode())) {
            return;
        }

        long currentTime = world.getTime();
        if (currentTime - blockEntity.getLastTriggerGameTime() < REDSTONE_COOLDOWN_TICKS) {
            return;
        }

        blockEntity.setLastTriggerGameTime(currentTime);
        blockEntity.triggerFromRedstone();
    }

    private static boolean hasOtherSubtitleBlockInChunk(ServerLevel world, BlockPos excludedPos) {
        LevelChunk chunk = world.getWorldChunk(excludedPos);

        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof SubtitleBlockEntity && !blockEntity.getPos().equals(excludedPos)) {
                return true;
            }
        }

        return false;
    }
}
