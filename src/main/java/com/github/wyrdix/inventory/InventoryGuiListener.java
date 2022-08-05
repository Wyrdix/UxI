package com.github.wyrdix.inventory;

import com.github.wyrdix.inventory.component.*;
import com.github.wyrdix.inventory.event.*;
import com.github.wyrdix.inventory.exceptions.InventoryGuiSectionOutOfFields;
import com.github.wyrdix.inventory.section.FreeSection;
import com.github.wyrdix.inventory.section.GuiSection;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.List;
import java.util.Objects;

public class InventoryGuiListener implements Listener {
    public InventoryGuiListener() {

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) event.setCancelled(true);
        if (event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();

        InventoryGui.getOpenedInventory(player).ifPresent(gui -> {
            if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)){
                event.setCancelled(true);
                event.getWhoClicked().getOpenInventory().setCursor(event.getCursor());
                return;
            }else if (event.getRawSlot() != event.getSlot()) {
                if (event.isShiftClick()) event.setCancelled(true);
            }else{
                boolean cancelled = InventoryGuiClickEvent.generateEvent(event, gui, gui, player, event.getSlot(), false);

                event.setCancelled(!cancelled);
            }

        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event){
        Player player = (Player) event.getWhoClicked();

        InventoryGui.getOpenedInventory(player).ifPresent(gui ->{
            gui.getInstance(player).ifPresent(instance->{
                List<GuiPosition> collect = event.getRawSlots().stream().sorted().map(slot -> {
                    try {
                        return new GuiPosition(gui, slot);
                    } catch (InventoryGuiSectionOutOfFields e) {
                        return null;
                    }
                }).filter(Objects::nonNull).filter(s->!FreeSection.isFree(gui, s)).toList();

                if(!collect.isEmpty()) {
                    event.setCancelled(true);
                    player.setItemOnCursor(event.getOldCursor());
                }
            });
        });
    }


    @EventHandler
    public void onClick(InventoryGuiClickEvent event) {
        for (Component component : event.getSection().getComponents()) {
            if (component instanceof ClickReactionComponent react) {
                react.onClick(event.getGui(), event.getPosition(), event.getPlayer());
            }

            if (component instanceof ItemComponent itemComponent) {
                if (itemComponent.getPosition().equals(event.getPosition())) {
                    itemComponent.onClick(event, event.getSection(), event.getPlayer());
                }
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

            onGuiClose(event, event.getGui());
        }
    }

    private void onGuiClose(InventoryGuiCloseEvent event, GuiSection section) {
        if (section instanceof FreeSection) ((FreeSection) section).onClose(event);

        section.getSubSections().forEach(s -> onGuiClose(event, s));
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOpen(InventoryGuiOpenEvent event) {
        if (event.isCancelled()) return;
        InventoryGui gui = event.getGui();
        if (gui.getOptions().getGuiRefreshRate() > 0) {
            InventoryGuiUpdater.INVENTORY_GUI.put(gui.getId(), -1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getEntity();
        InventoryGui.getOpenedInventory(player).ifPresent(gui -> gui.close(player, true));
    }
}
