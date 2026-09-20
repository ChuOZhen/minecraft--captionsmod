package com.subtitlescreen.registry;

import com.subtitlescreen.SubtitleScreenMod;
import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

public final class ModBlockEntities {
    public static final BlockEntityType<SubtitleBlockEntity> SUBTITLE_BLOCK_ENTITY =
            FabricBlockEntityTypeBuilder.create(SubtitleBlockEntity::new, ModBlocks.SUBTITLE_BLOCK).build();

    private ModBlockEntities() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SubtitleScreenMod.id("subtitle_block_entity"),
                SUBTITLE_BLOCK_ENTITY);
    }
}
