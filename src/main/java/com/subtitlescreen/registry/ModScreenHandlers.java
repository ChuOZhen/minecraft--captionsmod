package com.subtitlescreen.registry;

import com.subtitlescreen.SubtitleScreenMod;
import com.subtitlescreen.screen.SubtitleScreenHandler;
import com.subtitlescreen.screen.SubtitleScreenOpeningData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public final class ModScreenHandlers {
    public static final ScreenHandlerType<SubtitleScreenHandler> SUBTITLE_SCREEN_HANDLER =
            new ExtendedScreenHandlerType<>(SubtitleScreenHandler::new, SubtitleScreenOpeningData.PACKET_CODEC);

    private ModScreenHandlers() {
    }

    public static void register() {
        Registry.register(Registries.SCREEN_HANDLER, SubtitleScreenMod.id("subtitle_screen"),
                SUBTITLE_SCREEN_HANDLER);
    }
}
