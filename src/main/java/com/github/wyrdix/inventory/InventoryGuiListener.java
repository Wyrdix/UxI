package com.github.wyrdix.inventory;

import com.github.wyrdix.inventory.component.*;
import com.github.wyrdix.inventory.event.*;
import com.github.wyrdix.inventory.exceptions.InventoryGuiSectionOutOfFields;
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
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Objects;

public class InventoryGuiListener implements Listener {
    public InventoryGuiListener() {

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();
        InventoryGui.getOpenedInventory(player).ifPresent(gui -> {
            if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
                event.setCancelled(true);
                event.getWhoClicked().getOpenInventory().setCursor(event.getCursor());
            } else if (event.getRawSlot() >= gui.getSize()) {
                if (event.isShiftClick()) event.setCancelled(true);
            } else {
                event.setCancelled(true);
                InventoryGuiClickEvent.generateEvent(event, gui, gui, gui.getInstance(player).orElseThrow(IllegalArgumentException::new), player, event.getRawSlot(), true);
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
                }).filter(Objects::nonNull).filter(s -> !GuiSection.isFree(gui, s)).toList();

                if (!collect.isEmpty()) {
                    event.setCancelled(true);
                }
                if (event.isCancelled()) {
                    player.setItemOnCursor(event.getOldCursor());
                }

                for (GuiPosition position : collect) {
                    for (GuiSection section : instance.getGui().getSectionsContaining(position)) {
                        processSectionDrag(instance, section, section.getFields().get(section.getParentFields().indexOf(position)), event);
                    }
                }


                instance.updateInventory();
            });
        });
    }

    public void processSectionDrag(InventoryGui.GuiInstance<?> instance, GuiSection section, GuiPosition position, InventoryDragEvent event) {
        for (Component component : section.getComponents()) {
            if (!(component instanceof ItemComponent itemComponent)) continue;
            if (!itemComponent.getPosition().equals(position)) return;

            itemComponent.onDrag(instance, position, event);
        }

        for (GuiSection subSection : section.getSubSections()) {
            int index = subSection.getParentFields().indexOf(position);
            if (index == -1) continue;
            GuiPosition child = subSection.getFields().get(index);
            processSectionDrag(instance, subSection, child, event);
        }
    }


    @EventHandler
    public void onClick(InventoryGuiClickEvent event) {
        for (Component component : ImmutableList.copyOf(event.getSection().getComponents())) {
            if (component instanceof ClickReactionComponent react) {
                react.onClick(event.getGui(), event.getPosition(), event.getInstance(), event.getPlayer());
            }

            if (component instanceof ItemComponent itemComponent) {
                if (itemComponent.getPosition().project(event.getGui()).equals(event.getPosition().project(event.getGui()))) {
                    itemComponent.onClick(event, event.getSection(), event.getInstance(), event.getPlayer());
                }
            }

        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        InventoryGui.getOpenedInventory(player).ifPresent(gui -> {
            gui.close(player);
        });

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
        section.onClose(event);
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
