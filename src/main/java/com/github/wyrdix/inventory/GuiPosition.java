package com.github.wyrdix.inventory;

import com.github.wyrdix.inventory.exceptions.InventoryGuiSectionOutOfFields;
import com.github.wyrdix.inventory.section.GuiSection;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;

public class GuiPosition {

    private final @NonNull GuiSection section;
    private final int x;
    private final int y;
    private int index = -1;

    public GuiPosition(@NonNull GuiSection section, int index, int x, int y) {
        Validate.notNull(section);
        this.section = section;
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public GuiPosition(@NonNull GuiSection section, int index) throws InventoryGuiSectionOutOfFields {
        Validate.notNull(section);
        @NonNull List<GuiPosition> fields = section.getFields();
        if (fields.size() <= index) throw new InventoryGuiSectionOutOfFields(fields.size(), index);

        this.section = section;
        this.index = index;

        GuiPosition position = fields.get(index);

        this.x = position.x;
        this.y = position.y;
    }

    public GuiPosition(@NonNull GuiSection section, int x, int y) throws InventoryGuiSectionOutOfFields {
        Validate.notNull(section);
        @NonNull List<GuiPosition> fields = section.getFields();

        for (int i = 0; i < fields.size(); i++) {
            GuiPosition field = fields.get(i);
            if (field.x == x && field.y == y) {
                this.index = i;
                break;
            }
        }

        if (this.index == -1) {
            throw new InventoryGuiSectionOutOfFields();
        }

        this.section = section;
        this.x = x;
        this.y = y;
    }

    public UnsafeGuiPosition toUnsafe() {
        return new UnsafeGuiPosition(index, x, y);
    }

    public @NonNull GuiSection getSection() {
        return section;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public @Nullable ItemStack getItem(InventoryGui.@NonNull GuiInstance<?> instance) {
        return section.getItem(this, instance);
    }

    public int getIndex() {
        return index;
    }

    public GuiPosition project(GuiSection gui) {
        Validate.notNull(gui);

        if (gui == getSection()) return this;

        List<GuiPosition> parentFields = getSection().getParentFields();
        if (parentFields == null || parentFields.size() < index)
            throw new IllegalArgumentException("Trouble with projecting gui position");
        return parentFields.get(index).project(gui);
    }

    @Override
    public String toString() {
        return "GuiPosition{" +
                "x=" + x +
                ", y=" + y +
                ", index=" + index +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuiPosition position = (GuiPosition) o;
        return x == position.x && y == position.y && index == position.index && section.equals(position.section);
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, x, y, index);
    }

    public record UnsafeGuiPosition(int index, int x, int y) {

        public GuiPosition toGuiPosition(@NonNull InventoryGui gui) {
            Validate.notNull(gui);
            return new GuiPosition(gui, index, x, y);
        }

        @Override
        public String toString() {
            return "UnsafeGuiPosition{" +
                    "x=" + x +
                    ", y=" + y +
                    ", index=" + index +
                    '}';
        }
    }
}
