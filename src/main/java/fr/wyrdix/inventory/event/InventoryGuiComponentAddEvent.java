package fr.wyrdix.inventory.event;

import fr.wyrdix.inventory.GuiSection;
import fr.wyrdix.inventory.component.Component;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class InventoryGuiComponentAddEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final GuiSection section;
    private final Component component;

    private boolean cancelled = false;

    public InventoryGuiComponentAddEvent(@NonNull GuiSection section, @NonNull Component component) {
        this.section = section;
        this.component = component;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
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

    public Component getComponent() {
        return component;
    }

    public GuiSection getSection() {
        return section;
    }
}
