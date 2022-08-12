package com.github.wyrdix.inventory.component;

import com.github.wyrdix.inventory.GuiPosition;
import com.github.wyrdix.inventory.InventoryGui;
import org.bukkit.entity.Player;

public abstract class ClickReactionComponent implements Component {

    public abstract void onClick(InventoryGui gui,
                                 GuiPosition position,
                                 InventoryGui.GuiInstance<?> instance,
                                 Player player);

}
