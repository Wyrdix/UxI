package com.github.wyrdix.inventory.section;

import com.github.wyrdix.inventory.GuiPosition;
import com.github.wyrdix.inventory.InventoryGui;
import com.github.wyrdix.inventory.component.Component;
import com.github.wyrdix.inventory.component.ItemComponent;
import com.github.wyrdix.inventory.event.InventoryGuiCloseEvent;
import com.github.wyrdix.inventory.exceptions.InventoryGuiSectionOutOfFields;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Stream;

public interface GuiSection {

    static boolean isFree(GuiSection gui, GuiPosition position) {
        if (gui.isFree()) return true;
        for (GuiSection section : gui.getSectionsContaining(position)) {
            List<GuiPosition> fields = section.getParentFields();
            if (fields == null) continue;
            if (isFree(section, section.getFields().get(fields.indexOf(position)))) return true;
        }

        return false;
    }

    @NonNull List<GuiPosition> getFields();

    @NonNull Set<GuiSection> getSubSections();

    void addSection(GuiSection section) throws InventoryGuiSectionOutOfFields;

    @NonNull Set<Component> getComponents();

    @SuppressWarnings("unchecked")
    default @NonNull <T> Optional<T> getFromComponent(@NonNull Class<T> clazz) {
        for (Component component : getComponents()) {
            if (clazz.isInstance(component)) return Optional.of((T) component);
        }
        return Optional.empty();
    }

    @Nullable GuiSection getParent();

    default @NonNull GuiSection getRoot() {
        if (getParent() == null) return this;
        return getParent().getRoot();
    }

    default boolean isRootSection() {
        return this instanceof InventoryGui;
    }

    default boolean isSlot() {
        return getFields().size() == 1;
    }

    default boolean isEmpty() {
        return getFields().isEmpty();
    }

    default boolean isDistinct(@NonNull GuiSection section) {

        Validate.notNull(section);

        GuiSection root = section.getRoot();
        if (root != getRoot()) return true;

        Stream<GuiPosition> positionStream = Objects.requireNonNull(getFields()).stream().map(s -> s.project(root));
        List<GuiPosition> projectedPositions = Objects.requireNonNull(section.getFields()).stream().map(s -> s.project(root)).toList();

        return positionStream.noneMatch(projectedPositions::contains);
    }

    default @NonNull List<GuiPosition> getCommonPositions(@NonNull GuiSection section) {

        Validate.notNull(section);

        GuiSection root = section.getRoot();
        if (root != getRoot()) return Collections.emptyList();

        Stream<GuiPosition> positionStream = Objects.requireNonNull(getFields()).stream();
        List<GuiPosition> projectedPositions = Objects.requireNonNull(section.getFields()).stream().map(s -> s.project(root)).toList();

        return positionStream.filter(s -> projectedPositions.contains(s.project(root))).toList();
    }

    default @NonNull List<GuiSection> getSectionsContaining(GuiPosition position){
        return getSubSections().stream().filter(s -> s.getParentFields().contains(position)).toList();
    }

    @Nullable ItemStack getItem(@NonNull GuiPosition position, InventoryGui.@NonNull GuiInstance<?> instance);

    void setItem(@NonNull GuiPosition position, @NonNull Player player, @NonNull ItemStack item);

    void setItem(@NonNull ItemComponent component);

    boolean addComponent(@NonNull Component component);

    boolean removeComponent(@NonNull Component component);

    @NonNull List<GuiPosition> getParentFields();

    default boolean isFree() {
        return false;
    }

    Optional<InventoryGui.GuiInstance<?>> getInstance(UUID uuid);

    default Optional<InventoryGui.GuiInstance<?>> getInstance(Player player) {
        if (player == null) return Optional.empty();
        return getInstance(player.getUniqueId());
    }

    default void onClose(InventoryGuiCloseEvent event) {

    }
}
