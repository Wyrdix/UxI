package com.github.wyrdix.inventory;

public class GuiOptions {

    private int playerLimit = -1;
    private boolean guiCleanup = true;
    private int guiRefreshRate = -1;
    private InstanceCreationConfig instanceCreationConfig = InstanceCreationConfig.NEW;
    private boolean isInstanceOwn = true;

    public GuiOptions() {

    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    public boolean isGuiCleanup() {
        return guiCleanup;
    }

    public void setGuiCleanup(boolean guiCleanup) {
        this.guiCleanup = guiCleanup;
    }

    public int getGuiRefreshRate() {
        return guiRefreshRate;
    }

    public void setGuiRefreshRate(int guiRefreshRate) {
        this.guiRefreshRate = guiRefreshRate <= 0 ? -1 : guiRefreshRate;
    }

    public InstanceCreationConfig getInstanceCreationConfig() {
        return instanceCreationConfig;
    }

    public void setInstanceCreationConfig(InstanceCreationConfig instanceCreationConfig) {
        this.instanceCreationConfig = instanceCreationConfig;
    }

    public boolean isInstanceOwn() {
        return isInstanceOwn;
    }

    public void setInstanceOwn(boolean instanceOwn) {
        isInstanceOwn = instanceOwn;
    }

    public enum InstanceCreationConfig {
        NEW,
        WITH_ID;
    }
}
