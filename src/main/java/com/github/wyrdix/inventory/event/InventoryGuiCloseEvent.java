package com.github.wyrdix.inventory.event;

import com.github.wyrdix.inventory.InventoryGui;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

public class InventoryGuiCloseEvent extends GuiEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;

    private boolean cancelled = false;
    private boolean instanceRemoved;

    public InventoryGuiCloseEvent(@NonNull InventoryGui gui, InventoryGui.@NonNull  GuiInstance<?> instance, @NonNull Player player, boolean instanceRemoved) {
        super(gui, instance);
        this.player = player;
        this.instanceRemoved = instanceRemoved;
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

    public boolean isInstanceRemoved() {
        return instanceRemoved;
    }

    public void setInstanceRemoved(boolean instanceRemoved) {
        this.instanceRemoved = instanceRemoved;
    }

    public Player getPlayer() {
        return player;
    }
}
