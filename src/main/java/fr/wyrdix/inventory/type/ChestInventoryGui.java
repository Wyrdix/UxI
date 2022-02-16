package fr.wyrdix.inventory.type;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.InventoryGui;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ChestInventoryGui extends InventoryGui {

    public ChestInventoryGui(@NonNull String title, int size) {
        super(title, create(size));
    }

    private static @NonNull List<GuiPosition.UnsafeGuiPosition> create(int size) {
        List<GuiPosition.UnsafeGuiPosition> set = new ArrayList<>();
        int rows = size / 9;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < 9; x++) {
                set.add(new GuiPosition.UnsafeGuiPosition(y * 9 + x, x, y));
            }
        }

        return set;
    }

    @Override
    protected InventoryGui.GuiInstance<?> createInstance(UUID uuid) {
        return new GuiInstance(this, uuid);
    }

    private final static class GuiInstance extends InventoryGui.GuiInstance<ChestInventoryGui> {

        public GuiInstance(@NonNull ChestInventoryGui gui, @NonNull UUID owner) {
            super(gui, owner);
        }

        @Override
        protected Inventory createInventory(String title) {
            //noinspection deprecation
            return Bukkit.createInventory(null, getGui().getSize(), title);
        }
    }
}
