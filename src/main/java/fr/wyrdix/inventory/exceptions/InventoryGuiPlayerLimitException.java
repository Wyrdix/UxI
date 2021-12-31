package fr.wyrdix.inventory.exceptions;

public class InventoryGuiPlayerLimitException extends Exception {
    public InventoryGuiPlayerLimitException() {
        super("An fr.wyrdix.inventory is trying to add a viewer but it would excess its limit");
    }
}
