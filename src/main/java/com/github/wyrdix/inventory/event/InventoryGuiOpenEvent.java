package com.github.wyrdix.inventory.event;

import com.github.wyrdix.inventory.InventoryGui;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

public class InventoryGuiOpenEvent extends GuiEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;

    private boolean cancelled = false;

    public InventoryGuiOpenEvent(@NonNull InventoryGui gui, InventoryGui.@NonNull GuiInstance<?> instance,  @NonNull Player player) {
        super(gui, instance);
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Player getPlayer() {
        return player;
    }
}
