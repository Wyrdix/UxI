package fr.wyrdix.inventory;

import com.google.common.collect.ImmutableList;
import fr.wyrdix.inventory.component.*;
import fr.wyrdix.inventory.event.InventoryGuiClickEvent;
import fr.wyrdix.inventory.event.InventoryGuiCloseEvent;
import fr.wyrdix.inventory.event.InventoryGuiComponentAddEvent;
import fr.wyrdix.inventory.event.InventoryGuiComponentRemoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryGuiListener implements Listener {
    public InventoryGuiListener() {

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getRawSlot() != event.getSlot()) return;

        Player player = (Player) event.getWhoClicked();

        InventoryGui.getOpenedInventory(player).ifPresent(gui -> {
            boolean cancelled = InventoryGuiClickEvent.generateEvent(event, gui, gui, player, event.getSlot(), false);

            event.setCancelled(!cancelled);
        });

    }

    @EventHandler
    public void onClick(InventoryGuiClickEvent event) {
        for (Component component : event.getSection().getComponents()) {
            if (component instanceof ClickReactionComponent react) {
                react.onClick(event.getGui(), event.getPosition(), event.getPlayer());
            }

            if (component instanceof ItemComponent itemComponent && itemComponent.getPosition().equals(event.getPosition())) {
                itemComponent.onClick(event, event.getSection(), event.getPlayer());
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getReason() == InventoryCloseEvent.Reason.PLAYER) {

            InventoryGui.getOpenedInventory(player).ifPresent(gui -> gui.close(player, true));

        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuiClose(InventoryGuiCloseEvent event) {
        Player player = event.getPlayer();
        InventoryGui gui = event.getGui();

        if (gui.getOptions().isGuiCleanup()) {
            for (Component component : ImmutableList.copyOf(gui.getComponents())) {
                if (component instanceof PersonalComponent personalComponent && personalComponent.getPlayer().equals(player)) {
                    gui.removeComponent(component);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onComponentAddition(InventoryGuiComponentAddEvent event) {
        event.getComponent().onAddition(event.getSection());

        if (event.getComponent() instanceof ItemComponent itemComponent) {
            ItemPanelComponent panelComponent = event.getSection().getFromComponent(ItemPanelComponent.class).orElseGet(() -> {
                ItemPanelComponent component = new ItemPanelComponent(event.getSection());
                event.getSection().addComponent(component);
                return component;
            });

            panelComponent.setItem(itemComponent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onComponentDeletion(InventoryGuiComponentRemoveEvent event) {
        event.getComponent().onDeletion(event.getSection());

        if (event.getComponent() instanceof ItemComponent itemComponent) {
            ItemPanelComponent panelComponent = event.getSection().getFromComponent(ItemPanelComponent.class).orElseGet(() -> {
                ItemPanelComponent component = new ItemPanelComponent(event.getSection());
                event.getSection().addComponent(component);
                return component;
            });

            panelComponent.setItem(new ItemComponent(itemComponent.getPosition(), null));
        }
    }
}
