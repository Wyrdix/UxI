package fr.wyrdix.inventory;

public class GuiOptions {

    private int playerLimit = -1;
    private boolean guiCleanup = true;


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
}
