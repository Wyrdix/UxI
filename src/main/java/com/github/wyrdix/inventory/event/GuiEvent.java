package com.github.wyrdix.inventory.event;

import com.github.wyrdix.inventory.InventoryGui;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class GuiEvent extends Event {
    private final @NonNull InventoryGui gui;
    private final @NonNull Player player;

    public GuiEvent(@NonNull InventoryGui gui, @NonNull Player player) {
        Validate.notNull(gui);
        Validate.notNull(player);
        this.gui = gui;
        this.player = player;
    }

    public @NonNull InventoryGui getGui() {
        return gui;
    }

    public @NonNull Player getPlayer() {
        return player;
    }
}
