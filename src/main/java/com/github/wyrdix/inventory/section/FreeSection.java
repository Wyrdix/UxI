package com.github.wyrdix.inventory.section;

import com.github.wyrdix.inventory.event.InventoryGuiCloseEvent;

public interface FreeSection extends GuiSection {
    void onClose(InventoryGuiCloseEvent event);
}
