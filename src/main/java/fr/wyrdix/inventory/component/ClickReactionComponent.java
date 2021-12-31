package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.InventoryGui;
import org.bukkit.entity.Player;

public abstract class ClickReactionComponent implements Component {

    public abstract void onClick(InventoryGui gui, GuiPosition position, Player player);

}
