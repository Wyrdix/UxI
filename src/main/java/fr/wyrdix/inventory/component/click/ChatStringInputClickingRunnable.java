package fr.wyrdix.inventory.component.click;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.InventoryGui;
import fr.wyrdix.inventory.component.ItemComponent;
import fr.wyrdix.inventory.event.InventoryGuiClickEvent;
import fr.wyrdix.inventory.section.GuiSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ChatStringInputClickingRunnable implements ItemComponent.ClickingRunnable, Listener {

    final Map<UUID, InventoryGui> currentlyTyping = new HashMap<>();

    public ChatStringInputClickingRunnable() {

    }

    @Override
    public void onClick(@NonNull InventoryGuiClickEvent event, @NonNull GuiSection section, @NonNull Player player, @NonNull GuiPosition position) {
        currentlyTyping.put(player.getUniqueId(), event.getGui());
        infoMessage(player);
        ChatStringInputListener.getInstance().runnableMap.put(player.getUniqueId(), this);
        player.closeInventory();
    }


    public abstract void infoMessage(@NonNull Player player);

    public abstract boolean check(@NonNull Player player, @NonNull String input);

    public abstract void onStringInput(@NonNull Player player, @NonNull String input);
}
