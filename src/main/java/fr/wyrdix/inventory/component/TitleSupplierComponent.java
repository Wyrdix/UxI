package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.InventoryGui;

import java.util.function.Function;

public class TitleSupplierComponent implements Component, Function<InventoryGui.GuiInstance<?>, String> {

    @Override
    public String apply(InventoryGui.GuiInstance<?> instance) {
        return null;
    }
}
