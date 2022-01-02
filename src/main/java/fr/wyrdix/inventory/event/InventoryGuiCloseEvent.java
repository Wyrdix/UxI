package fr.wyrdix.inventory.event;

import fr.wyrdix.inventory.InventoryGui;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

public class InventoryGuiCloseEvent extends GuiEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled = false;
    private boolean instanceRemoved;

    public InventoryGuiCloseEvent(@NonNull InventoryGui gui, @NonNull Player player, boolean instanceRemoved) {
        super(gui, player);
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
}
