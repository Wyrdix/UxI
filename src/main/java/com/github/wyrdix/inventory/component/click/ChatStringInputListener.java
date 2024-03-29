package com.github.wyrdix.inventory.component.click;

import com.github.wyrdix.UxiPlugin;
import com.github.wyrdix.inventory.InventoryGui;
import com.github.wyrdix.inventory.event.InventoryGuiCloseEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatStringInputListener implements Listener {

    private static final ChatStringInputListener INSTANCE = new ChatStringInputListener();
    final Map<UUID, ChatStringInputClickingRunnable> runnableMap = new HashMap<>();

    public ChatStringInputListener() {
    }

    public static ChatStringInputListener getInstance() {
        return INSTANCE;
    }

    @EventHandler
    public void onGuiClose(InventoryGuiCloseEvent event) {
        if (runnableMap.containsKey(event.getPlayer().getUniqueId())) event.setInstanceRemoved(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        ChatStringInputClickingRunnable runnable = runnableMap.get(uuid);
        if (runnable == null) return;
        runnable.currentlyTyping.remove(uuid);
        runnableMap.remove(uuid);
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {

        ChatStringInputClickingRunnable runnable = runnableMap.get(event.getPlayer().getUniqueId());

        if (runnable == null) return;

        InventoryGui gui = runnable.currentlyTyping.get(event.getPlayer().getUniqueId());

        if (gui == null) return;

        gui.close(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMessage(AsyncPlayerChatEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();
        ChatStringInputClickingRunnable clickingRunnable = runnableMap.get(uuid);

        if (clickingRunnable == null) return;

        InventoryGui gui = clickingRunnable.currentlyTyping.get(uuid);

        if (gui == null) return;

        if (clickingRunnable.check(event.getPlayer(), event.getMessage())) {

            clickingRunnable.onStringInput(event.getPlayer(), event.getMessage());

            runnableMap.remove(uuid);
            clickingRunnable.currentlyTyping.remove(uuid);

            Bukkit.getScheduler().runTask(UxiPlugin.getInstance(), () -> {
                InventoryGui.GuiInstance<?> instance = gui.getInstance(uuid).orElse(null);
                if (instance == null) return;
                clickingRunnable.onStringInputted(instance, event.getPlayer());
            });

            event.setCancelled(true);
        }

    }

}
