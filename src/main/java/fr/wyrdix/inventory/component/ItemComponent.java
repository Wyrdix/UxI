package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.GuiSection;
import fr.wyrdix.inventory.InventoryGui;
import fr.wyrdix.inventory.event.InventoryGuiClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ItemComponent implements PositionalComponent {

    private final GuiPosition position;
    private ItemStack stack;

    public ItemComponent(GuiPosition position, ItemStack stack) {
        this.position = position;
    }

    public ItemStack getItem(InventoryGui gui, Player player) {
        return stack;
    }

    @SuppressWarnings("unused")
    public void onClick(InventoryGuiClickEvent event, GuiSection section, Player player) {

    }

    @Override
    public @NonNull GuiPosition getPosition() {
        return position;
    }
}
