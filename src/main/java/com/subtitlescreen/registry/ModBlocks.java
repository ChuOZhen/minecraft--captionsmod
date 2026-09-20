package com.subtitlescreen.registry;

import com.subtitlescreen.SubtitleScreenMod;
import com.subtitlescreen.block.SubtitleBlock;
import net.minecraft.level.level.block.state.BlockBehaviour;
import net.minecraft.level.level.block.Blocks;
import net.minecraft.level.level.material.PushReaction;
import net.minecraft.level.item.BlockItem;
import net.minecraft.level.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

public final class ModBlocks {
    public static final SubtitleBlock SUBTITLE_BLOCK = new SubtitleBlock(BlockBehaviour.Settings.copy(Blocks.STONE)
            .strength(3.0f, 6.0f)
            .requiresTool()
            .nonOpaque()
            .pistonBehavior(PushReaction.BLOCK));

    private ModBlocks() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, SubtitleScreenMod.id("subtitle_block"), SUBTITLE_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, SubtitleScreenMod.id("subtitle_block"),
                new BlockItem(SUBTITLE_BLOCK, new Item.Properties()));
    }
}
