package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.InventoryGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ItemClickReactionComponent extends ClickReactionComponent {

    @Override
    public void onClick(InventoryGui gui, GuiPosition position, Player player) {
        onClick(gui, position, gui.getItem(position, player), player);
    }

    protected abstract void onClick(InventoryGui gui, GuiPosition position, ItemStack item, Player player);
}
