package com.subtitlescreen.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public final class ModItemGroups {

    /**
     * 原版「功能方块」标签页的键。
     *
     * <p>26.3 的 CreativeModeTabs / CreativeModeTab 不再提供公开的标签页常量，
     * 因此这里直接用资源键引用它。</p>
     */
    private static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath("minecraft", "functional_blocks"));

    private ModItemGroups() {
    }

    /**
     * 把字幕方块加入原版的「功能方块」创造标签页。
     *
     * <p>初版自建了一个标签页。26.3 的 CreativeModeTab.Output 是 protected 嵌套类型，
     * 模组无法用 lambda 实现 DisplayItemsGenerator（编译报「Output 在 CreativeModeTab 中是
     * protected 访问控制」），因此改为通过 Fabric 的 CreativeModeTabEvents 把物品追加到
     * 原版标签页中——效果同样是「可从创造模式物品栏获取」。</p>
     */
    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(FUNCTIONAL_BLOCKS)
                .register(output -> output.accept(ModBlocks.SUBTITLE_BLOCK));
    }
}
