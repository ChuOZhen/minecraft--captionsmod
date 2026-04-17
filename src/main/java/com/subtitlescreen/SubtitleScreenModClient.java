package com.subtitlescreen;

import com.subtitlescreen.network.SubtitleTriggerPayload;
import com.subtitlescreen.registry.ModScreenHandlers;
import com.subtitlescreen.render.SubtitleOverlayRenderer;
import com.subtitlescreen.screen.SubtitleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class SubtitleScreenModClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        // 注册屏幕
        HandledScreens.register(ModScreenHandlers.SUBTITLE_SCREEN_HANDLER, SubtitleScreen::new);
        
        // 注册字幕渲染器
        SubtitleOverlayRenderer renderer = new SubtitleOverlayRenderer();
        HudRenderCallback.EVENT.register(renderer::onHudRender);
        
        // 注册客户端接收字幕触发数据包
        ClientPlayNetworking.registerGlobalReceiver(SubtitleTriggerPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                renderer.showSubtitle(
                        payload.text(),
                        payload.fontType(),
                        payload.duration(),
                        payload.textColorHex(),
                        payload.playerNameColorHex()
                );
            });
        });
        
        System.out.println("字幕方块客户端模组已加载！");
    }
}
