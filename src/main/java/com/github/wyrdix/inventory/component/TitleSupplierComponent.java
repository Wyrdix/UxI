package com.github.wyrdix.inventory.component;

import com.github.wyrdix.inventory.InventoryGui;

import java.util.function.Function;

public class TitleSupplierComponent implements Component, Function<InventoryGui.GuiInstance<?>, String> {

    @Override
    public String apply(InventoryGui.GuiInstance<?> instance) {
        return null;
    }
}
