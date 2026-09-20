package com.subtitlescreen.screen;

import com.subtitlescreen.registry.ModBlocks;
import com.subtitlescreen.registry.ModScreenHandlers;
import net.minecraft.level.entity.player.Player;
import net.minecraft.level.entity.player.Inventory;
import net.minecraft.level.item.ItemStack;
import net.minecraft.level.inventory.AbstractContainerMenu;
import net.minecraft.level.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SubtitleScreenHandler extends AbstractContainerMenu {
    
    private final ContainerLevelAccess context;
    @Nullable
    private final BlockPos pos;

    public SubtitleScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.EMPTY, null);
    }

    public SubtitleScreenHandler(int syncId, Inventory playerInventory, SubtitleScreenOpeningData data) {
        this(syncId, playerInventory, ContainerLevelAccess.EMPTY, data.pos());
    }
    
    public SubtitleScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        this(syncId, playerInventory, context, null);
    }

    public SubtitleScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context,
                                 @Nullable BlockPos pos) {
        super(ModScreenHandlers.SUBTITLE_SCREEN_HANDLER, syncId);
        this.context = context;
        this.pos = pos;
    }
    
    @Override
    public boolean canUse(Player player) {
        return AbstractContainerMenu.canUse(context, player, ModBlocks.SUBTITLE_BLOCK);
    }

    @Override
    public ItemStack quickMove(Player player, int slot) {
        return ItemStack.EMPTY;
    }
    
    public ContainerLevelAccess getContext() {
        return context;
    }

    @Nullable
    public BlockPos getPos() {
        return pos;
    }
}
