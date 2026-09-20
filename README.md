# 字幕方块模组 (SubtitleScreen Mod)

一个 Fabric 模组：放置「字幕方块」，用大屏显示自定义字幕，支持**玩家进入世界触发**与**红石触发**。

---

## ⚠️ 来源声明

> 本项目的**初版由旧模型 v3.2 生成**，面向 Minecraft **1.21.1**（Yarn 映射）。
> 仓库里已存在 1.21 ~ 1.21.11 共 12 个构建产物，但源码与构建脚本停留在 1.21.1 时代。
>
> **2.0.0 由 Reasonix 依据《经验.md》升级到 Minecraft 26.3**：
> 26.3 对渲染、菜单、方块、网络四套体系都有结构性改动，本次升级不是简单改名，
> 具体差异见下方「升级说明」。升级后已通过 `./gradlew build` 实测验证
> （编译 + 项目自带的两个测试任务均通过）。

---

## 功能特性

- **字幕方块**：可显示多行自定义字幕，支持玩家名占位
- **两种触发方式**：玩家进入世界（`on_join`）或红石信号（`redstone`）
- **16 种预设颜色** + **彩虹文字**效果
- **3 种字体选项**（微软雅黑 / 宋体 / 楷体）
- **淡入淡出**：字幕出现与消失时自动渐变
- **权限控制**：仅 OP（等级 2）或单人/局域网环境可配置
- **区块强加载**：放置后自动强加载所在区块，保证触发可靠
- **中英双语界面**（翻译键内置）

## 使用方法

1. 从创造模式物品栏的**「功能方块」**标签页获取「字幕方块」
2. 放置方块后**右键**打开设置面板
3. 配置字幕内容、颜色、字体、持续时间、触发方式
4. 点击「保存」即可生效

## 字幕语法

- 在字幕文本中写入 `id:"player"`，显示时会替换为触发玩家的名称
- 示例：`欢迎, id:"player"` → `欢迎, Steve`
- 支持多行（每行一个输入框，最多 4 行）

## 环境要求

| 组件 | 版本 |
|---|---|
| Minecraft | **26.3** |
| Fabric Loader | **0.19.5+** |
| Fabric API | 需要（声明了 `fabric-api-base`、`fabric-lifecycle-events-v1`、`fabric-networking-api-v1`、`fabric-object-builder-api-v1`、`fabric-creative-tab-api-v1`、`fabric-rendering-v1`） |
| Java | **25+** |
| 安装位置 | 客户端与服务端**都需要**（`environment: "*"`） |


## 构建

```bash
./gradlew build          # 产物：build/libs/subtitlescreen-2.0.0.jar
./gradlew runClient      # 启动开发环境客户端
```

项目自带两个轻量测试任务（挂在 `check` 上，`build` 时会运行）：

```bash
./gradlew runSubtitleRenderStateTest      # 渲染状态断言
./gradlew runSubtitleJoinSchedulerTest    # 进服触发调度断言
```

> 注意：这两个测试是带 `main` 方法的独立类，不是 JUnit 测试，因此 `build.gradle` 中关闭了
> Gradle 默认的 `test` 任务，改用上述自定义 `JavaExec` 任务。

---

## 升级说明：1.21.1 → 26.3

### 构建工具链

| 项 | 初版（v3.2） | 现在 |
|---|---|---|
| Minecraft | 1.21.1 | **26.3** |
| 映射 | Yarn `1.21.1+build.3` | **恒等映射**（26.3 无可用 Yarn） |
| Loader | 0.15.11 | **0.19.5** |
| Fabric API | 0.110.0+1.21.1（maven） | **0.161.0+26.3**（本地模块 jar） |
| Loom | `1.7-SNAPSHOT` | **1.18.2**（要求 Gradle ≥ 9.7.0） |
| Gradle | wrapper jar 缺失 | **9.7.1**（wrapper 补齐，首次可构建） |
| Java | 21 | **25** |

初版的 `.gitignore` 用 `*.jar` 把 `gradle/wrapper/gradle-wrapper.jar` 一并忽略掉了，
导致仓库里的 `gradlew` 根本无法运行——本次已修正并加入白名单。

### 26.3 的结构性改动（本次升级的实际工作）

| 层 | 1.21.1 的写法 | 26.3 的实际 API |
|---|---|---|
| **HUD 渲染** | `HudRenderCallback` + `DrawContext.drawText…` | `HudElementRegistry.addLast(id, HudElement)`；`HudElement#extractRenderState(GuiGraphicsExtractor, DeltaTracker)`；文字用 `extractor.text(font,…)` / `centeredText(…)`；变换用 `extractor.pose()`（`Matrix3x2fStack`）；尺寸用 `extractor.guiWidth()/guiHeight()` |
| **菜单界面** | `ScreenHandler` + `ScreenHandlerType` + `MenuScreens.register` | `MenuScreens.register` 已是 **private**，改为：服务端发 `SubtitleOpenScreenPayload` → 客户端 `Minecraft.setScreenAndShow(new SubtitleScreen(pos))`（`setScreen` 也改名为 `setScreenAndShow`） |
| **方块/物品属性** | `AbstractBlock.Settings.copy(...)` | **必须在 `Properties` 上 `setId(ResourceKey)`**：否则构造方块时抛 `NullPointerException: Block id not set`（`BlockBehaviour$Properties.effectiveDrops`）。注意 `ofFullCopy` 会把原方块的 id 一起复制过来，必须覆盖 |
| **方块** | `Block.onUse/createBlockEntity/onBlockAdded/onStateReplaced/getRenderType` | 都在 `BlockBehaviour`：`useWithoutItem`、`EntityBlock.newBlockEntity`、`onPlace`、`affectNeighborsAfterRemoval`、`getRenderShape`；`neighborChanged` **多一个 `Orientation` 参数** |
| **方块实体** | `writeNbt/readNbt(CompoundTag, HolderLookup)` | `saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)`；`markDirty` → `setChanged` |
| **权限** | `player.hasPermissionLevel(2)` | `player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)` |
| **网络** | `CustomPayload.Id` / `codecOf` / `PayloadTypeRegistry.playC2S()` | `CustomPacketPayload.Type` / `StreamCodec.of` / `serverboundPlay()`·`clientboundPlay()`；`getId()` → `type()` |
| **创造物品栏** | `FabricItemGroup.builder()` + `ItemGroup` | 模块改名为 `fabric-creative-tab-api-v1`；`FabricCreativeModeTab` + `CreativeModeTabEvents` |
| **注册表** | `Registries.*` | `BuiltInRegistries.*`（`ITEM_GROUP` → `CREATIVE_MODE_TAB`） |
| **其它** | — | `Properties.copy`→`ofFullCopy`、`requiresTool`→`requiresCorrectToolForDrops`、`nonOpaque`→`noOcclusion`、`pistonBehavior`→`pushReaction`、`PushReaction.BLOCK`→`IMMOVEABLE`、`ChunkPos` 变 record（`containing()`/`x()`/`z()`）、`Identifier.of`→`Identifier.fromNamespaceAndPath` |

### 因 26.3 限制而调整的设计

1. **不再自建创造标签页**：`CreativeModeTab.Output` 在 26.3 是 `protected` 嵌套类型，
   模组无法实现 `DisplayItemsGenerator`。改为用 `CreativeModeTabEvents` 把方块加入原版
   **「功能方块」**标签页（用资源键 `minecraft:functional_blocks` 引用，因为标签页常量也已是 private）。
2. **不再走 Menu 打开界面**：见上表「菜单界面」。界面数据仍通过 `SubtitleUpdatePayload` 回传服务端，
   权限校验仍在服务端完成。
3. **进服触发改为扫描附近已加载区块**：26.3 的 `ServerLevel` 不再暴露「已强制加载区块」的查询接口
   （只剩 `setChunkForced`），因此改为以玩家所在区块为中心扫描半径 8 的已加载区块
   （`getChunkNow` 不会生成新区块）。
4. **移除方块 codec**：26.3 的 `Block`/`BaseEntityBlock` 与原版方块都不再有 `CODEC` 字段。

---

## 目录结构

```
.
├── build.gradle / settings.gradle / gradle.properties
├── gradlew / gradlew.bat            Gradle 9.7.1 wrapper（初版缺失 jar）
├── gradle/identity-mappings.jar     ★ 26.3 恒等映射
├── libs/fabric-api-modules/         ★ Fabric API 模块 jar（编译依赖）
└── src/main/
    ├── java/com/subtitlescreen/
    │   ├── SubtitleScreenMod.java            服务端入口
    │   ├── SubtitleScreenModClient.java      客户端入口（HUD + 网络接收）
    │   ├── block/SubtitleBlock.java          字幕方块
    │   ├── block/entity/SubtitleBlockEntity.java
    │   ├── network/                          3 个数据包（更新 / 触发 / 打开界面）
    │   ├── registry/                         方块、方块实体、标签页、数据包注册
    │   ├── render/                           字幕渲染（HUD 叠加层）
    │   ├── screen/                           设置界面
    │   └── trigger/                          进服触发调度
    └── resources/
        ├── fabric.mod.json
        ├── subtitlescreen.mixins.json        （当前为空，无实际注入）
        └── assets/subtitlescreen/            模型、方块状态、纹理、中英翻译
```

---

## 已知限制

- **字体选项暂未生效**：界面上的字体选择会保存，但 26.3 下的实际绘制仍统一使用默认字体
  （初版依赖的字体渲染路径在 26.3 已变，需要单独的字体资源方案）。
- **进服触发只扫描玩家附近半径 8 的区块**（原因见「因 26.3 限制而调整的设计」第 3 条）。
- **未在真实客户端/服务端实测**：本次升级完成的是「构建验证」（编译 + 自带测试通过），
  游戏内的实际显示效果、红石触发、跨维度等运行时行为建议自行开服验证一次。
- 字幕文本上限 4 行 × 256 字符。

## 许可证

MIT License
