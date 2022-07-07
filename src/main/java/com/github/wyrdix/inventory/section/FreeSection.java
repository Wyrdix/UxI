package com.github.wyrdix.inventory.section;

import com.github.wyrdix.inventory.GuiPosition;
import com.github.wyrdix.inventory.InventoryGui;
import com.github.wyrdix.inventory.event.InventoryGuiCloseEvent;

public interface FreeSection extends GuiSection {


    static boolean isFree(GuiSection gui, GuiPosition position) {
        if(gui instanceof FreeSection) return true;
        for (GuiSection section : gui.getSectionsContaining(position)) {
            if(isFree(section, position.project(section))) return true;
        }

        return false;
    }

    void onClose(InventoryGuiCloseEvent event);
}
