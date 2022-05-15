package com.github.wyrdix.inventory.exceptions;

public class InventoryGuiPlayerLimitException extends Exception {
    public InventoryGuiPlayerLimitException() {
        super("An com.github.wyrdix.inventory is trying to add a viewer but it would excess its limit");
    }
}
