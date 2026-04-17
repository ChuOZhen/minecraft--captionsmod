package com.subtitlescreen.block;

import com.mojang.serialization.MapCodec;
import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import com.subtitlescreen.screen.SubtitleScreenHandler;
import com.subtitlescreen.screen.SubtitleScreenOpeningData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class SubtitleBlock extends BlockWithEntity {
    public static final MapCodec<SubtitleBlock> CODEC = createCodec(SubtitleBlock::new);
    private static final long REDSTONE_COOLDOWN_TICKS = 5L;
	    
    public SubtitleBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SubtitleBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            if (canConfigure(player, world)) {
                NamedScreenHandlerFactory screenHandlerFactory = createScreenHandlerFactory(world, pos);
                player.openHandledScreen(screenHandlerFactory);
                return ActionResult.SUCCESS;
            }

            player.sendMessage(Text.translatable("subtitlescreen.message.no_permission"), false);
            return ActionResult.FAIL;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.getBlock() != oldState.getBlock() && world instanceof ServerWorld serverWorld) {
            forceChunk(serverWorld, pos);
            initializeRedstoneState(serverWorld, pos);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world instanceof ServerWorld serverWorld) {
            forceChunk(serverWorld, pos);
            initializeRedstoneState(serverWorld, pos);

            if (placer instanceof PlayerEntity player) {
                player.sendMessage(Text.translatable("subtitlescreen.message.chunk_forced"), false);
            }
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos,
                                  boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

        if (world instanceof ServerWorld serverWorld
                && serverWorld.getBlockEntity(pos) instanceof SubtitleBlockEntity blockEntity) {
            handleRedstoneUpdate(serverWorld, pos, blockEntity);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient && world instanceof ServerWorld serverWorld && state.getBlock() != newState.getBlock()) {
            ChunkPos chunkPos = new ChunkPos(pos);
            if (!hasOtherSubtitleBlockInChunk(serverWorld, pos)) {
                serverWorld.setChunkForced(chunkPos.x, chunkPos.z, false);

                if (serverWorld.getServer().getPlayerManager().getPlayerList().size() > 0) {
                    var player = serverWorld.getServer().getPlayerManager().getPlayerList().get(0);
                    player.sendMessage(Text.translatable("subtitlescreen.message.chunk_unforced"), false);
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return false;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    private static boolean canConfigure(PlayerEntity player, World world) {
        return player.hasPermissionLevel(2) || world.getServer() != null && !world.getServer().isDedicated();
    }

    private static NamedScreenHandlerFactory createScreenHandlerFactory(World world, BlockPos pos) {
        return new ExtendedScreenHandlerFactory<SubtitleScreenOpeningData>() {
            @Override
            public SubtitleScreenOpeningData getScreenOpeningData(ServerPlayerEntity player) {
                return new SubtitleScreenOpeningData(pos);
            }

            @Override
            public Text getDisplayName() {
                return Text.translatable("screen.subtitlescreen.subtitle_screen");
            }

            @Override
            public SubtitleScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory inventory,
                                                    PlayerEntity player) {
                return new SubtitleScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos), pos);
            }
        };
    }

    private static void forceChunk(ServerWorld world, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        world.setChunkForced(chunkPos.x, chunkPos.z, true);
    }

    private static void initializeRedstoneState(ServerWorld world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof SubtitleBlockEntity blockEntity) {
            blockEntity.setLastRedstonePowered(world.isReceivingRedstonePower(pos));
        }
    }

    private static void handleRedstoneUpdate(ServerWorld world, BlockPos pos, SubtitleBlockEntity blockEntity) {
        boolean powered = world.isReceivingRedstonePower(pos);
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

    private static boolean hasOtherSubtitleBlockInChunk(ServerWorld world, BlockPos excludedPos) {
        WorldChunk chunk = world.getWorldChunk(excludedPos);

        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof SubtitleBlockEntity && !blockEntity.getPos().equals(excludedPos)) {
                return true;
            }
        }

        return false;
    }
}
