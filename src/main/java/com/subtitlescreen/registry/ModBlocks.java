package com.subtitlescreen.registry;

import com.subtitlescreen.SubtitleScreenMod;
import com.subtitlescreen.block.SubtitleBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

public final class ModBlocks {
    public static final SubtitleBlock SUBTITLE_BLOCK = new SubtitleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .pushReaction(PushReaction.IMMOVEABLE));

    private ModBlocks() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, SubtitleScreenMod.id("subtitle_block"), SUBTITLE_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, SubtitleScreenMod.id("subtitle_block"),
                new BlockItem(SUBTITLE_BLOCK, new Item.Properties()));
    }
}
