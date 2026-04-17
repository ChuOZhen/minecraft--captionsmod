package com.subtitlescreen.block.entity;

import com.subtitlescreen.network.SubtitleUpdatePayload;
import com.subtitlescreen.registry.ModBlockEntities;
import com.subtitlescreen.registry.ModPayloads;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

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
    
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("subtitleText", subtitleText);
        nbt.putString("fontType", fontType);
        nbt.putString("triggerMode", triggerMode);
        nbt.putInt("duration", duration);
        nbt.putString("textColorHex", textColorHex);
        nbt.putString("playerNameColorHex", playerNameColorHex);
        nbt.putBoolean("lastRedstonePowered", lastRedstonePowered);
        nbt.putLong("lastTriggerGameTime", lastTriggerGameTime);
    }
    
    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        subtitleText = nbt.contains("subtitleText") ? nbt.getString("subtitleText") : DEFAULT_SUBTITLE_TEXT;
        fontType = nbt.contains("fontType") ? nbt.getString("fontType") : DEFAULT_FONT_TYPE;
        triggerMode = nbt.contains("triggerMode") ? nbt.getString("triggerMode") : DEFAULT_TRIGGER_MODE;
        duration = nbt.contains("duration") ? clampDuration(nbt.getInt("duration")) : DEFAULT_DURATION;
        textColorHex = nbt.contains("textColorHex") ? nbt.getString("textColorHex") : DEFAULT_TEXT_COLOR_HEX;
        playerNameColorHex = nbt.contains("playerNameColorHex")
                ? nbt.getString("playerNameColorHex")
                : DEFAULT_PLAYER_NAME_COLOR_HEX;
        lastRedstonePowered = nbt.contains("lastRedstonePowered") && nbt.getBoolean("lastRedstonePowered");
        lastTriggerGameTime = nbt.contains("lastTriggerGameTime") ? nbt.getLong("lastTriggerGameTime") : -100L;
    }
    
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    
    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
    
    public void updateFromPayload(SubtitleUpdatePayload payload) {
        this.subtitleText = payload.subtitleText();
        this.fontType = payload.fontType();
        this.triggerMode = payload.triggerMode();
        this.duration = clampDuration(payload.duration());
        this.textColorHex = payload.textColorHex();
        this.playerNameColorHex = payload.playerNameColorHex();
        markDirtyAndSync();
    }
    
    public void triggerForPlayer(ServerPlayerEntity player) {
        if (world instanceof ServerWorld) {
            String processedText = subtitleText.replace("id:\"player\"", player.getName().getString());
            ModPayloads.sendSubtitle(player, processedText, fontType, duration, textColorHex, playerNameColorHex);
        }
    }
    
    public void triggerForAllPlayers() {
        if (world instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity player : serverWorld.getServer().getPlayerManager().getPlayerList()) {
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
    public String getSubtitleText() { return subtitleText; }
    public void setSubtitleText(String subtitleText) { 
        this.subtitleText = subtitleText; 
        markDirtyAndSync();
    }
    
    public String getFontType() { return fontType; }
    public void setFontType(String fontType) { 
        this.fontType = fontType; 
        markDirtyAndSync();
    }
    
    public String getTriggerMode() { return triggerMode; }
    public void setTriggerMode(String triggerMode) { 
        this.triggerMode = triggerMode; 
        markDirtyAndSync();
    }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { 
        this.duration = clampDuration(duration);
        markDirtyAndSync();
    }
    
    public String getTextColorHex() { return textColorHex; }
    public void setTextColorHex(String textColorHex) { 
        this.textColorHex = textColorHex; 
        markDirtyAndSync();
    }
    
    public String getPlayerNameColorHex() { return playerNameColorHex; }
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
        markDirty();
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(pos);
        }
    }

    private static int clampDuration(int duration) {
        return Math.max(20, Math.min(200, duration));
    }
}
