package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.InventoryGui;
import fr.wyrdix.inventory.event.InventoryGuiClickEvent;
import fr.wyrdix.inventory.section.GuiSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ItemComponent implements PositionalComponent {

    private final GuiPosition position;
    private final ItemStack stack;
    private final ClickingRunnable clickingRunnable;
    private final CreateRunnable createRunnable;

    public ItemComponent(GuiPosition position, ItemStack stack) {
        this(position, stack, (event, section, player, pos) -> {
        }, (gui, player, itemStack) -> itemStack);
    }

    public ItemComponent(GuiPosition position, ItemStack stack, ClickingRunnable clickingRunnable, CreateRunnable createRunnable) {
        this.position = position;
        this.stack = stack;
        this.clickingRunnable = clickingRunnable;
        this.createRunnable = createRunnable;
    }

    public ItemStack getItem(InventoryGui gui, Player player) {
        return createRunnable.onCreate(gui, player, stack);
    }

    @SuppressWarnings("unused")
    public void onClick(InventoryGuiClickEvent event, GuiSection section, Player player) {
        clickingRunnable.onClick(event, section, player, position);
    }

    @Override
    public @NonNull GuiPosition getPosition() {
        return position;
    }

    public ItemComponent clone(GuiPosition position) {
        ItemStack cloneItem = stack == null ? null : stack.clone();
        return new ItemComponent(position, cloneItem, clickingRunnable, createRunnable);
    }

    public interface ClickingRunnable {
        void onClick(InventoryGuiClickEvent event, GuiSection section, Player player, GuiPosition position);
    }

    public interface CreateRunnable {
        ItemStack onCreate(InventoryGui gui, Player player, ItemStack stack);
    }
}
