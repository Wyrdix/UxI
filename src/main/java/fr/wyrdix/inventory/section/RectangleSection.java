package fr.wyrdix.inventory.section;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.component.Component;
import fr.wyrdix.inventory.component.ItemComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RectangleSection implements GuiSection {

    private final List<GuiPosition> fields = new ArrayList<>();
    private final List<GuiPosition> parentFields = new ArrayList<>();

    private final Set<GuiSection> sections = new HashSet<>();
    private final Set<Component> components = new HashSet<>();
    private final GuiSection parent;

    public RectangleSection(@NonNull GuiSection parent, @NonNull GuiPosition corner1, @NonNull GuiPosition corner2) {
        Validate.notNull(parent);
        Validate.notNull(corner1);
        Validate.notNull(corner2);

        Validate.isTrue(corner1.getX() <= corner2.getX());
        Validate.isTrue(corner1.getY() <= corner2.getY());

        this.parent = parent;

        int size = parent.getFields().size();
        for (int i = 0; i < size; i++) {
            GuiPosition position = parent.getFields().get(i);

            if (position.getX() < corner1.getX() || position.getX() > corner2.getX()) continue;
            if (position.getY() < corner1.getY() || position.getY() > corner2.getY()) continue;

            parentFields.add(position);
            fields.add(new GuiPosition(this, fields.size(), position.getX() - corner1.getX(), position.getY() - corner1.getY()));
        }
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
        return null;
    }

    @Override
    public void setItem(@NonNull GuiPosition position, @NonNull Player player, @NonNull ItemStack item) {
        parent.setItem(getParentFields().get(position.getIndex()), player, item);
    }

    @Override
    public void setItem(@NonNull ItemComponent component) {
        parent.setItem(component.clone(component.getPosition().project(parent)));
    }

    @Override
    public void addComponent(@NonNull Component component) {
        components.add(component);
    }

    @Override
    public void removeComponent(@NonNull Component component) {
        components.remove(component);
    }
}
