package com.github.wyrdix.inventory;

import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.TreeMap;

public class InventoryGuiUpdater implements Runnable {

    static final Map<Integer, Long> INVENTORY_GUI = new TreeMap<>();

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for (Integer integer : ImmutableSet.copyOf(INVENTORY_GUI.keySet())) {
            InventoryGui gui = InventoryGui.INVENTORY_GUIS.get(integer);

            long delta = ((INVENTORY_GUI.get(integer) - now) * 20) / 100;

            if (delta >= gui.getOptions().getGuiRefreshRate()) {
                for (Player player : gui.getViewers()) {
                    gui.getInstance(player).ifPresent(InventoryGui.GuiInstance::updateInventory);
                }

                if (gui.getOptions().getGuiRefreshRate() > 0) INVENTORY_GUI.put(integer, now);
                else INVENTORY_GUI.remove(integer);
            }

        }
    }
}
