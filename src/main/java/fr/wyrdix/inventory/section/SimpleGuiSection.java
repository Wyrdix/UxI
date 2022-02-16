package fr.wyrdix.inventory.section;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.InventoryGui;
import fr.wyrdix.inventory.component.Component;
import fr.wyrdix.inventory.component.ItemComponent;
import fr.wyrdix.inventory.component.PersonalItemComponent;
import fr.wyrdix.inventory.event.InventoryGuiComponentAddEvent;
import fr.wyrdix.inventory.event.InventoryGuiComponentRemoveEvent;
import fr.wyrdix.inventory.exceptions.InventoryGuiSectionOutOfFields;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class SimpleGuiSection implements GuiSection {

    protected final List<GuiPosition> fields = new ArrayList<>();
    protected final List<GuiPosition> parentFields = new ArrayList<>();

    private final Set<GuiSection> sections = new HashSet<>();
    private final Set<Component> components = new HashSet<>();
    private final GuiSection parent;

    public SimpleGuiSection(GuiSection parent) {
        this.parent = parent;
    }

    @Override
    public @NonNull List<GuiPosition> getParentFields() {
        return parentFields;
    }

    @Override
    public @NonNull List<GuiPosition> getFields() {
        return fields;
    }

    @Override
    public @NonNull Set<GuiSection> getSubSections() {
        return sections;
    }

    @Override
    public @NonNull Set<Component> getComponents() {
        return components;
    }

    @Override
    public @Nullable GuiSection getParent() {
        return parent;
    }

    @Override
    public @Nullable ItemStack getItem(@NonNull GuiPosition position, @NonNull Player player) {
        return getRoot().getItem(position.project(getRoot()), player);
    }

    @Override
    public void setItem(@NonNull GuiPosition position, @NonNull Player player, @NonNull ItemStack item) {
        Validate.notNull(position);
        Validate.notNull(player);

        getInstance(player.getUniqueId()).ifPresent(instance -> {
            if (addComponent(new PersonalItemComponent(position, item, player))) {
                instance.setItem(position, item);
            }
        });
    }

    @Override
    public void setItem(@NonNull ItemComponent component) {
        Validate.notNull(component);
        addComponent(component.clone(component.getPosition().project(this)));
    }

    public boolean addComponent(@NonNull Component component) {
        Validate.notNull(component);

        InventoryGuiComponentAddEvent event = new InventoryGuiComponentAddEvent(this, component);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) components.add(component);
        return !event.isCancelled();
    }

    public boolean removeComponent(@NonNull Component component) {
        Validate.notNull(component);

        InventoryGuiComponentRemoveEvent event = new InventoryGuiComponentRemoveEvent(this, component);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) components.remove(component);
        return !event.isCancelled();
    }

    @Override
    public Optional<InventoryGui.GuiInstance<?>> getInstance(UUID uuid) {
        return getRoot().getInstance(uuid);
    }

    public void addSection(@NonNull GuiSection section) throws InventoryGuiSectionOutOfFields {
        Validate.notNull(section);

        if (getCommonPositions(section).isEmpty()) throw new InventoryGuiSectionOutOfFields();

        sections.add(section);
    }
}
