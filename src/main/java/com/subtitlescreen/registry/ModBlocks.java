package com.subtitlescreen.registry;

import com.subtitlescreen.SubtitleScreenMod;
import com.subtitlescreen.block.SubtitleBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlocks {
    public static final SubtitleBlock SUBTITLE_BLOCK = new SubtitleBlock(AbstractBlock.Settings.copy(Blocks.STONE)
            .strength(3.0f, 6.0f)
            .requiresTool()
            .nonOpaque()
            .pistonBehavior(PistonBehavior.BLOCK));

    private ModBlocks() {
    }

    public static void register() {
        Registry.register(Registries.BLOCK, SubtitleScreenMod.id("subtitle_block"), SUBTITLE_BLOCK);
        Registry.register(Registries.ITEM, SubtitleScreenMod.id("subtitle_block"),
                new BlockItem(SUBTITLE_BLOCK, new Item.Settings()));
    }
}
