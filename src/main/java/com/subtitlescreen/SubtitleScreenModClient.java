package com.subtitlescreen;

import com.subtitlescreen.network.SubtitleTriggerPayload;
import com.subtitlescreen.registry.ModScreenHandlers;
import com.subtitlescreen.render.SubtitleOverlayRenderer;
import com.subtitlescreen.screen.SubtitleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SubtitleScreenModClient implements ClientModInitializer {

    /** 字幕叠加层在 HUD 中的标识。 */
    public static final Identifier SUBTITLE_OVERLAY_ID =
            Identifier.fromNamespaceAndPath(SubtitleScreenMod.MOD_ID, "subtitle_overlay");

    @Override
    public void onInitializeClient() {
        // 打开方块时使用的菜单界面（26.3 中 MenuScreens 位于 net.minecraft.client.gui.screens）
        MenuScreens.register(ModScreenHandlers.SUBTITLE_SCREEN_HANDLER, SubtitleScreen::new);

        // 字幕渲染器
        // 26.3 用 HudElementRegistry 挂载 HudElement 取代了 1.21.x 的 HudRenderCallback
        SubtitleOverlayRenderer renderer = new SubtitleOverlayRenderer();
        HudElementRegistry.addLast(SUBTITLE_OVERLAY_ID, renderer::extractRenderState);

        // 客户端接收字幕触发数据包
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

        System.out.println("字幕方块客户端模组已加载！（Minecraft 26.3）");
    }
}
