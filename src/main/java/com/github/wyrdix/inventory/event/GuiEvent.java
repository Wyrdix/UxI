package com.github.wyrdix.inventory.event;

import com.github.wyrdix.inventory.InventoryGui;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class GuiEvent extends Event {
    private final @NonNull InventoryGui gui;
    private final InventoryGui.@NonNull GuiInstance<?> instance;

    public GuiEvent(@NonNull InventoryGui gui, InventoryGui.GuiInstance<?> instance) {
        Validate.notNull(gui);
        Validate.notNull(instance);
        this.gui = gui;
        this.instance = instance;
    }

    public @NonNull InventoryGui getGui() {
        return gui;
    }

    public InventoryGui.@NonNull GuiInstance<?> getInstance() {
        return instance;
    }
}
