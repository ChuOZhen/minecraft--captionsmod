package com.subtitlescreen;

import com.subtitlescreen.registry.ModBlockEntities;
import com.subtitlescreen.registry.ModBlocks;
import com.subtitlescreen.registry.ModItemGroups;
import com.subtitlescreen.registry.ModPayloads;
import com.subtitlescreen.trigger.SubtitleTriggerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;

public class SubtitleScreenMod implements ModInitializer {
    public static final String MOD_ID = "subtitlescreen";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModItemGroups.register();
        ModPayloads.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            SubtitleTriggerManager.scheduleJoinSubtitles(player);
        });
        ServerTickEvents.END_SERVER_TICK.register(SubtitleTriggerManager::tick);

        System.out.println("字幕方块模组已加载！（Minecraft 26.3）");
    }
}
