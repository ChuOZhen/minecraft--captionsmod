package com.subtitlescreen.screen;

import com.subtitlescreen.registry.ModBlocks;
import com.subtitlescreen.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SubtitleScreenHandler extends ScreenHandler {
    
    private final ScreenHandlerContext context;
    @Nullable
    private final BlockPos pos;

    public SubtitleScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, null);
    }

    public SubtitleScreenHandler(int syncId, PlayerInventory playerInventory, SubtitleScreenOpeningData data) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, data.pos());
    }
    
    public SubtitleScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        this(syncId, playerInventory, context, null);
    }

    public SubtitleScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context,
                                 @Nullable BlockPos pos) {
        super(ModScreenHandlers.SUBTITLE_SCREEN_HANDLER, syncId);
        this.context = context;
        this.pos = pos;
    }
    
    @Override
    public boolean canUse(PlayerEntity player) {
        return ScreenHandler.canUse(context, player, ModBlocks.SUBTITLE_BLOCK);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }
    
    public ScreenHandlerContext getContext() {
        return context;
    }

    @Nullable
    public BlockPos getPos() {
        return pos;
    }
}
