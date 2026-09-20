package com.subtitlescreen.registry;

import com.subtitlescreen.SubtitleScreenMod;
import com.subtitlescreen.screen.SubtitleScreenHandler;
import com.subtitlescreen.screen.SubtitleScreenOpeningData;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.level.inventory.MenuType;

public final class ModScreenHandlers {
    public static final MenuType<SubtitleScreenHandler> SUBTITLE_SCREEN_HANDLER =
            new ExtendedMenuType<>(SubtitleScreenHandler::new, SubtitleScreenOpeningData.PACKET_CODEC);

    private ModScreenHandlers() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.SCREEN_HANDLER, SubtitleScreenMod.id("subtitle_screen"),
                SUBTITLE_SCREEN_HANDLER);
    }
}
