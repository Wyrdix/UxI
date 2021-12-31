package fr.wyrdix.inventory;

import com.google.common.collect.ImmutableSet;
import fr.wyrdix.inventory.component.Component;
import fr.wyrdix.inventory.component.ItemComponent;
import fr.wyrdix.inventory.exceptions.InventoryGuiSectionOutOfFields;
import fr.wyrdix.inventory.section.GuiSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SlotSection implements GuiSection {

    private final List<GuiPosition> fields;
    private final List<GuiPosition> parentFields;

    private final Set<Component> components = new HashSet<>();
    private final GuiSection parent;
    private final int slot;

    public SlotSection(GuiSection parent, int slot) {
        List<GuiPosition> tempFields;
        this.parent = parent;
        this.slot = slot;
        try {
            tempFields = Collections.singletonList(new GuiPosition(parent, slot));
        } catch (InventoryGuiSectionOutOfFields e) {
            tempFields = Collections.emptyList();
            e.printStackTrace();
        }
        this.fields = tempFields;
        this.parentFields = Collections.singletonList(tempFields.get(0).project(parent));
    }

    @Override
    public @NonNull List<GuiPosition> getFields() {
        return fields;
    }

    @Override
    public List<GuiPosition> getParentFields() {
        return parentFields;
    }

    @Override
    public @NonNull Set<GuiSection> getSubSections() {
        return Collections.emptySet();
    }

    @Override
    public @NonNull Set<Component> getComponents() {
        return ImmutableSet.copyOf(components);
    }

    @Override
    public @NonNull GuiSection getParent() {
        return parent;
    }

    @Override
    public @Nullable ItemStack getItem(@NonNull GuiPosition position, @NonNull Player player) {
        return parent.getItem(getParentFields().get(position.getIndex()), player);
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

    }

    @Override
    public void removeComponent(@NonNull Component component) {

    }

    public int getSlot() {
        return slot;
    }

    @Override
    public String toString() {
        return "SlotSection{" +
                "fields=" + fields +
                ", parentFields=" + parentFields +
                ", components=" + components +
                ", parent=" + parent +
                ", slot=" + slot +
                '}';
    }
}
