package com.github.wyrdix.inventory.section;

import com.github.wyrdix.inventory.GuiPosition;
import com.github.wyrdix.inventory.InventoryGui;
import com.github.wyrdix.inventory.component.Component;
import com.github.wyrdix.inventory.component.ItemComponent;
import com.github.wyrdix.inventory.event.InventoryGuiClickEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public abstract class PaginatedSection extends SimpleGuiSection {

    public PaginatedSection(GuiSection parent) {
        super(parent);

        for (int i = 0; i < parent.getFields().size(); i++) {
            GuiPosition position = parent.getFields().get(i);
            GuiPosition thisPosition = new GuiPosition(this, i, position.getX(), position.getY());
            this.fields.add(thisPosition);
            this.parentFields.add(position);

            int elements = i;

            super.addComponent(new ItemComponent(thisPosition, null,
                    (event, section, instance, player, pos) ->
                            onClick(event, section, instance, player, pos,
                                    elements + getPage(instance) * getFields().size()),
                    (gui, instance, stack) ->
                            getItem(gui, position, instance, elements + getPage(instance)  * getFields().size())));
        }
    }

    @Override
    public String toString() {
        return "PaginatedSection{" +
                "fields=" + fields +
                ", parentFields=" + parentFields +
                ", sections=" + getSubSections() +
                ", components=" + getComponents() +
                ", parent=" + getParent() +
                '}';
    }

    protected ItemStack getItem(InventoryGui gui, GuiPosition position, InventoryGui.GuiInstance<?> instance, int index) {

        if (index >= getSize(instance)) return new ItemStack(Material.AIR);

        ItemComponent item = getItem(instance, position, index);

        return item.getItem(gui, instance);
    }

    protected void onClick(InventoryGuiClickEvent event, GuiSection section, InventoryGui.GuiInstance<?> instance, Player player, GuiPosition pos, int index) {

        if (index >= getSize(instance)) return;

        ItemComponent item = getItem(instance, pos, index);
        item.onClick(event, section, instance, player);
    }

    public abstract int getSize(InventoryGui.GuiInstance<?> instance);

    public abstract @NonNull ItemComponent getItem(InventoryGui.GuiInstance<?> instance, GuiPosition position, int index);

    public int getPage(InventoryGui.@NotNull GuiInstance<?> instance) {
        return instance.<Integer>get(this, "page").orElse(0);
    }

    public void setPage(InventoryGui.@NotNull GuiInstance<?> instance, int newPage) {
        instance.set(this, "page", newPage);
    }

    public int nextPage(InventoryGui.GuiInstance<?> instance) {
        int newPage = getPage(instance) + 1;
        setPage(instance, newPage);
        return newPage;
    }

    public int previousPage(InventoryGui.GuiInstance<?> instance) {
        int newPage = getPage(instance) - 1;
        setPage(instance, newPage);
        return newPage;
    }

    public int getMaxPage(InventoryGui.GuiInstance<?> instance) {
        int nbElements = getFields().size();
        return getSize(instance) / nbElements;
    }

    public boolean hasPreviousPage(InventoryGui.GuiInstance<?> instance) {
        return getPage(instance) > 0;
    }

    public boolean hasNextPage(InventoryGui.GuiInstance<?> instance) {
        return getPage(instance) < getMaxPage(instance);
    }

    public boolean isLastPage(InventoryGui.GuiInstance<?> instance) {
        return !hasNextPage(instance);
    }

    public boolean isFirstPage(InventoryGui.GuiInstance<?> instance) {
        return !hasPreviousPage(instance);
    }

    @Override
    public boolean addComponent(@NonNull Component component) {

        if (component instanceof ItemComponent)
            throw new IllegalArgumentException("No item should be added manually to a paginated section");

        return super.addComponent(component);
    }

    @Override
    public boolean removeComponent(@NonNull Component component) {

        if (component instanceof ItemComponent)
            throw new IllegalArgumentException("No item should be removed manually to a paginated section");

        return super.removeComponent(component);
    }

    public static class PreviousPageNavigatorItem extends ItemComponent {

        private PaginatedSection paginatedSection;

        public PreviousPageNavigatorItem(@NonNull PaginatedSection paginatedSection, @NonNull GuiPosition position) {
            super(position, new ItemStack(Material.AIR));
            this.paginatedSection = paginatedSection;
        }

        public PreviousPageNavigatorItem(PaginatedSection paginatedSection, @NonNull GuiPosition position, @Nullable ItemStack stack, @NonNull CreateRunnable createRunnable) {
            super(position, stack, createRunnable);
            this.paginatedSection = paginatedSection;
        }

        public PreviousPageNavigatorItem(PaginatedSection paginatedSection, @NonNull GuiPosition position, @Nullable ItemStack stack, @NonNull ClickingRunnable clickingRunnable) {
            super(position, stack, clickingRunnable);
            this.paginatedSection = paginatedSection;
        }

        public PreviousPageNavigatorItem(@NonNull PaginatedSection paginatedSection, @NonNull GuiPosition position, @NonNull ClickingRunnable clickingRunnable, @NonNull CreateRunnable createRunnable) {
            super(position, new ItemStack(Material.AIR), clickingRunnable, createRunnable);
            this.paginatedSection = paginatedSection;
        }

        @Override
        public ItemStack getItem(InventoryGui gui, InventoryGui.GuiInstance<?> instance) {
            //noinspection OptionalGetWithoutIsPresent
            if (paginatedSection.isFirstPage(instance)) {
                return null;
            }

            return super.getItem(gui, instance);
        }

        @Override
        public void onClick(InventoryGuiClickEvent event, GuiSection section, InventoryGui.GuiInstance<?> instance, Player player) {
            if (paginatedSection.isFirstPage(instance)) return;

            super.onClick(event, section, instance, player);

            paginatedSection.previousPage(instance);

            instance.updateInventory();
        }
    }

    public static class NextPageNavigatorItem extends ItemComponent {

        private PaginatedSection paginatedSection;

        public NextPageNavigatorItem(@NonNull PaginatedSection paginatedSection, @NonNull GuiPosition position) {
            super(position, new ItemStack(Material.AIR));
            this.paginatedSection = paginatedSection;
        }

        public NextPageNavigatorItem(PaginatedSection paginatedSection, @NonNull GuiPosition position, @Nullable ItemStack stack, @NonNull CreateRunnable createRunnable) {
            super(position, stack, createRunnable);
            this.paginatedSection = paginatedSection;
        }

        public NextPageNavigatorItem(PaginatedSection paginatedSection, @NonNull GuiPosition position, @Nullable ItemStack stack, @NonNull ClickingRunnable clickingRunnable) {
            super(position, stack, clickingRunnable);
            this.paginatedSection = paginatedSection;
        }

        public NextPageNavigatorItem(@NonNull PaginatedSection paginatedSection, @NonNull GuiPosition position, @NonNull ClickingRunnable clickingRunnable, @NonNull CreateRunnable createRunnable) {
            super(position, new ItemStack(Material.AIR), clickingRunnable, createRunnable);
            this.paginatedSection = paginatedSection;
        }

        @Override
        public ItemStack getItem(InventoryGui gui, InventoryGui.GuiInstance<?> instance) {
            if (paginatedSection.isLastPage(instance)) {
                return null;
            }

            return super.getItem(gui, instance);
        }

        @Override
        public void onClick(InventoryGuiClickEvent event, GuiSection section, InventoryGui.GuiInstance<?> instance, Player player) {
            if (paginatedSection.isLastPage(instance)) return;

            super.onClick(event, section, instance, player);

            paginatedSection.nextPage(instance);

            instance.updateInventory();
        }
    }
}
