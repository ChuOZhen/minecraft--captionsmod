package com.subtitlescreen.registry;

import com.subtitlescreen.SubtitleScreenMod;
import com.subtitlescreen.block.SubtitleBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {

    /**
     * 方块与物品的资源键。
     *
     * <p>26.3 起 BlockBehaviour.Properties / Item.Properties 必须携带自己的 id
     * （用于掉落物与描述名推导）：否则构造方块时会抛
     * {@code NullPointerException: Block id not set}（BlockBehaviour$Properties.effectiveDrops）。
     * 另外 ofFullCopy(Blocks.STONE) 会把 minecraft:stone 的 id 一并复制过来，必须覆盖。</p>
     */
    public static final ResourceKey<Block> SUBTITLE_BLOCK_KEY =
            ResourceKey.create(Registries.BLOCK, SubtitleScreenMod.id("subtitle_block"));

    public static final ResourceKey<Item> SUBTITLE_BLOCK_ITEM_KEY =
            ResourceKey.create(Registries.ITEM, SubtitleScreenMod.id("subtitle_block"));

    public static final SubtitleBlock SUBTITLE_BLOCK = new SubtitleBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .pushReaction(PushReaction.IMMOVEABLE)
                    .setId(SUBTITLE_BLOCK_KEY));

    private ModBlocks() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, SubtitleScreenMod.id("subtitle_block"), SUBTITLE_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, SubtitleScreenMod.id("subtitle_block"),
                new BlockItem(SUBTITLE_BLOCK, new Item.Properties().setId(SUBTITLE_BLOCK_ITEM_KEY)));
    }
}
