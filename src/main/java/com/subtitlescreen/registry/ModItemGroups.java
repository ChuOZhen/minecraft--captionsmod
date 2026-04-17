package com.subtitlescreen.registry;

import com.subtitlescreen.SubtitleScreenMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
    public static final ItemGroup SUBTITLE_ITEM_GROUP = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.subtitlescreen.subtitle_group"))
            .icon(() -> new ItemStack(ModBlocks.SUBTITLE_BLOCK))
            .entries((displayContext, entries) -> entries.add(ModBlocks.SUBTITLE_BLOCK))
            .build();

    private ModItemGroups() {
    }

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, SubtitleScreenMod.id("subtitle_group"), SUBTITLE_ITEM_GROUP);
    }
}
