package com.github.wyrdix.inventory.event;

import com.github.wyrdix.inventory.GuiPosition;
import com.github.wyrdix.inventory.InventoryGui;
import com.github.wyrdix.inventory.section.GuiSection;
import com.github.wyrdix.inventory.section.SlotSection;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class InventoryGuiClickEvent extends GuiEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final @NonNull GuiSection section;
    private final @NonNull ItemStack item;
    private final InventoryClickEvent nativeEvent;
    private final GuiPosition position;
    private final Player player;

    private boolean cancelled = false;

    public InventoryGuiClickEvent(@NonNull InventoryClickEvent event, @NonNull InventoryGui gui, InventoryGui.@NonNull GuiInstance<?> instance, @NonNull GuiSection section, @NonNull GuiPosition position, Player player) {
        super(gui, instance);
        this.player = player;
        Validate.notNull(event);
        Validate.notNull(gui);
        Validate.notNull(section);
        Validate.notNull(position);
        this.nativeEvent = event;
        this.position = position;
        this.section = section;

        ItemStack item = getSection().getItem(position, instance);
        if (item == null) item = new ItemStack(Material.AIR);
        this.item = item;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public static boolean generateEvent(@NonNull InventoryClickEvent nativeEvent, @NonNull InventoryGui gui, @NonNull GuiSection guiSection, InventoryGui.@NonNull GuiInstance<?> instance, Player player, int slot, boolean cancelled) {
        Validate.notNull(guiSection);
        Validate.notNull(instance);
        Validate.isTrue(slot >= 0 && slot < gui.getFields().size(), slot + " isn't between 0 and " + (gui.getFields().size() - 1));

        SlotSection slotSection = new SlotSection(guiSection, slot);

        InventoryGuiClickEvent event = new InventoryGuiClickEvent(nativeEvent, gui, instance, guiSection, guiSection.getFields().get(slot), player);

        if (guiSection.isFree()) cancelled = false;

        nativeEvent.setCancelled(cancelled);

        Bukkit.getPluginManager().callEvent(event);

        cancelled = nativeEvent.isCancelled();

        List<GuiPosition> common;

        for (GuiSection section : guiSection.getSubSections()) {

            common = section.getCommonPositions(slotSection);
            if (!common.isEmpty()) { //There can only be one or zero values there

                int newSlot = section.getFields().indexOf(common.get(0));

                cancelled = generateEvent(nativeEvent, gui, section, instance, player, newSlot, cancelled);
            }
        }

        return cancelled;
    }

    public @NonNull ItemStack getItem() {
        return item;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public @NonNull GuiSection getSection() {
        return section;
    }

    public GuiPosition getPosition() {
        return position;
    }

    public InventoryClickEvent getNativeEvent() {
        return nativeEvent;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "InventoryGuiClickEvent{" +
               "section=" + section +
               ", item=" + item +
               ", position=" + position +
               ", player=" + player +
               ", cancelled=" + cancelled +
               '}';
    }
}
