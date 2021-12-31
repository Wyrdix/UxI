package fr.wyrdix.inventory.exceptions;

public class InventoryGuiSectionOutOfFields extends Exception {
    public InventoryGuiSectionOutOfFields() {
        super("A position is trying to be created or used outside of GuiSection border");
    }
}