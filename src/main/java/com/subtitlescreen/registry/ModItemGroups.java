package com.subtitlescreen.registry;

import com.subtitlescreen.SubtitleScreenMod;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.level.item.CreativeModeTab;
import net.minecraft.level.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;

public final class ModItemGroups {
    public static final CreativeModeTab SUBTITLE_ITEM_GROUP = FabricCreativeModeTab.builder()
            .displayName(Component.translatable("itemGroup.subtitlescreen.subtitle_group"))
            .icon(() -> new ItemStack(ModBlocks.SUBTITLE_BLOCK))
            .entries((displayContext, entries) -> entries.add(ModBlocks.SUBTITLE_BLOCK))
            .build();

    private ModItemGroups() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM_GROUP, SubtitleScreenMod.id("subtitle_group"), SUBTITLE_ITEM_GROUP);
    }
}
