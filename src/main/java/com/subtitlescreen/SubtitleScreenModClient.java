package com.subtitlescreen;

import com.subtitlescreen.network.SubtitleOpenScreenPayload;
import com.subtitlescreen.network.SubtitleTriggerPayload;
import com.subtitlescreen.render.SubtitleOverlayRenderer;
import com.subtitlescreen.screen.SubtitleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SubtitleScreenModClient implements ClientModInitializer {

    /** 字幕叠加层在 HUD 中的标识。 */
    public static final Identifier SUBTITLE_OVERLAY_ID =
            Identifier.fromNamespaceAndPath(SubtitleScreenMod.MOD_ID, "subtitle_overlay");

    @Override
    public void onInitializeClient() {
        // 字幕渲染器（26.3：HudElementRegistry 取代 1.21.x 的 HudRenderCallback）
        SubtitleOverlayRenderer renderer = new SubtitleOverlayRenderer();
        HudElementRegistry.addLast(SUBTITLE_OVERLAY_ID, renderer::extractRenderState);

        // 服务端 -> 客户端：显示字幕
        ClientPlayNetworking.registerGlobalReceiver(SubtitleTriggerPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> renderer.showSubtitle(
                    payload.text(),
                    payload.fontType(),
                    payload.duration(),
                    payload.textColorHex(),
                    payload.playerNameColorHex()
            ));
        });

        // 服务端 -> 客户端：打开设置界面
        // 26.3 无法注册菜单界面，改由服务端发请求、客户端自行打开普通 Screen
        ClientPlayNetworking.registerGlobalReceiver(SubtitleOpenScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() ->
                    Minecraft.getInstance().setScreenAndShow(new SubtitleScreen(payload.pos())));
        });

        System.out.println("字幕方块客户端模组已加载！（Minecraft 26.3）");
    }
}
