package com.subtitlescreen.block.entity;

import com.subtitlescreen.network.SubtitleUpdatePayload;
import com.subtitlescreen.registry.ModBlockEntities;
import com.subtitlescreen.registry.ModPayloads;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SubtitleBlockEntity extends BlockEntity {

    private static final String DEFAULT_SUBTITLE_TEXT = "欢迎, id:\"player\"";
    private static final String DEFAULT_FONT_TYPE = "微软雅黑";
    private static final String DEFAULT_TRIGGER_MODE = "on_join";
    private static final int DEFAULT_DURATION = 200;
    private static final String DEFAULT_TEXT_COLOR_HEX = "FFFFFF";
    private static final String DEFAULT_PLAYER_NAME_COLOR_HEX = "00FF00";

    private String subtitleText = DEFAULT_SUBTITLE_TEXT;
    private String fontType = DEFAULT_FONT_TYPE;
    private String triggerMode = DEFAULT_TRIGGER_MODE;
    private int duration = DEFAULT_DURATION;
    private String textColorHex = DEFAULT_TEXT_COLOR_HEX;
    private String playerNameColorHex = DEFAULT_PLAYER_NAME_COLOR_HEX;
    private boolean lastRedstonePowered = false;
    private long lastTriggerGameTime = -100L;

    public SubtitleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUBTITLE_BLOCK_ENTITY, pos, state);
    }

    // ------------------------------------------------------------------
    // 26.3 的 NBT 读写改用 ValueOutput / ValueInput 容器
    // （初版是 1.21.1 的 writeNbt(CompoundTag, HolderLookup) / readNbt）
    // ------------------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("subtitleText", subtitleText);
        output.putString("fontType", fontType);
        output.putString("triggerMode", triggerMode);
        output.putInt("duration", duration);
        output.putString("textColorHex", textColorHex);
        output.putString("playerNameColorHex", playerNameColorHex);
        output.putBoolean("lastRedstonePowered", lastRedstonePowered);
        output.putLong("lastTriggerGameTime", lastTriggerGameTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        subtitleText = input.getString("subtitleText").orElse(DEFAULT_SUBTITLE_TEXT);
        fontType = input.getString("fontType").orElse(DEFAULT_FONT_TYPE);
        triggerMode = input.getString("triggerMode").orElse(DEFAULT_TRIGGER_MODE);
        duration = clampDuration(input.getIntOr("duration", DEFAULT_DURATION));
        textColorHex = input.getString("textColorHex").orElse(DEFAULT_TEXT_COLOR_HEX);
        playerNameColorHex = input.getString("playerNameColorHex").orElse(DEFAULT_PLAYER_NAME_COLOR_HEX);
        lastRedstonePowered = input.getBooleanOr("lastRedstonePowered", false);
        lastTriggerGameTime = input.getLongOr("lastTriggerGameTime", -100L);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // 界面数据通过 SubtitleUpdatePayload / 打开界面请求单独取，这里不需要同步完整 NBT
        return new CompoundTag();
    }

    // ------------------------------------------------------------------

    public void updateFromPayload(SubtitleUpdatePayload payload) {
        this.subtitleText = payload.subtitleText();
        this.fontType = payload.fontType();
        this.triggerMode = payload.triggerMode();
        this.duration = clampDuration(payload.duration());
        this.textColorHex = payload.textColorHex();
        this.playerNameColorHex = payload.playerNameColorHex();
        markDirtyAndSync();
    }

    public void triggerForPlayer(ServerPlayer player) {
        if (level instanceof ServerLevel) {
            String processedText = subtitleText.replace("id:\"player\"", player.getName().getString());
            ModPayloads.sendSubtitle(player, processedText, fontType, duration, textColorHex, playerNameColorHex);
        }
    }

    public void triggerForAllPlayers() {
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                String processedText = subtitleText.replace("id:\"player\"", player.getName().getString());
                ModPayloads.sendSubtitle(player, processedText, fontType, duration, textColorHex, playerNameColorHex);
            }
        }
    }

    public void triggerFromRedstone() {
        if ("redstone".equals(triggerMode)) {
            triggerForAllPlayers();
        }
    }

    // Getters and setters

    public String getSubtitleText() {
        return subtitleText;
    }

    public void setSubtitleText(String subtitleText) {
        this.subtitleText = subtitleText;
        markDirtyAndSync();
    }

    public String getFontType() {
        return fontType;
    }

    public void setFontType(String fontType) {
        this.fontType = fontType;
        markDirtyAndSync();
    }

    public String getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(String triggerMode) {
        this.triggerMode = triggerMode;
        markDirtyAndSync();
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = clampDuration(duration);
        markDirtyAndSync();
    }

    public String getTextColorHex() {
        return textColorHex;
    }

    public void setTextColorHex(String textColorHex) {
        this.textColorHex = textColorHex;
        markDirtyAndSync();
    }

    public String getPlayerNameColorHex() {
        return playerNameColorHex;
    }

    public void setPlayerNameColorHex(String playerNameColorHex) {
        this.playerNameColorHex = playerNameColorHex;
        markDirtyAndSync();
    }

    public boolean isLastRedstonePowered() {
        return lastRedstonePowered;
    }

    public void setLastRedstonePowered(boolean lastRedstonePowered) {
        if (this.lastRedstonePowered == lastRedstonePowered) {
            return;
        }
        this.lastRedstonePowered = lastRedstonePowered;
        markDirtyAndSync();
    }

    public long getLastTriggerGameTime() {
        return lastTriggerGameTime;
    }

    public void setLastTriggerGameTime(long lastTriggerGameTime) {
        if (this.lastTriggerGameTime == lastTriggerGameTime) {
            return;
        }
        this.lastTriggerGameTime = lastTriggerGameTime;
        markDirtyAndSync();
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().blockChanged(getBlockPos());
        }
    }

    private static int clampDuration(int duration) {
        return Math.max(20, Math.min(200, duration));
    }
}
