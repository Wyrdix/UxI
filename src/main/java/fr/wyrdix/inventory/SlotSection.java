package fr.wyrdix.inventory;

import com.google.common.collect.ImmutableSet;
import fr.wyrdix.inventory.component.Component;
import fr.wyrdix.inventory.exceptions.InventoryGuiSectionOutOfFields;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

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
    public @Nullable ItemStack getItem(@NotNull GuiPosition position, @NotNull Player player) {
        return parent.getItem(getParentFields().get(position.getIndex()), player);
    }

    @Override
    public void setItem(@NotNull GuiPosition position, @NotNull Player player, @NotNull ItemStack item) {
        parent.setItem(getParentFields().get(position.getIndex()), player, item);
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
