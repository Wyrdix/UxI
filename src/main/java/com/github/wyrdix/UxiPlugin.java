package com.github.wyrdix;

import com.github.wyrdix.inventory.InventoryGui;
import com.github.wyrdix.inventory.InventoryGuiListener;
import com.github.wyrdix.inventory.InventoryGuiUpdater;
import com.github.wyrdix.inventory.component.click.ChatStringInputListener;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class UxiPlugin extends JavaPlugin implements Listener {

    private static UxiPlugin INSTANCE;

    public UxiPlugin() {
        INSTANCE = this;
    }

    public static UxiPlugin getInstance() {
        return INSTANCE;
    }

    @Override
    public void onLoad() {
        getLogger().info("Uxi is now loaded");
    }

    @Override
    public void onEnable() {
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage("                §e§l_____");
        sender.sendMessage("  §e|    |  §7\\  /  §e  |   §eUser Extra Interface");
        sender.sendMessage("  §e|    |  §7 \\/   §e  |   §r§7v." + getDescription().getVersion());
        sender.sendMessage("  §e§l|____|§r  §7 /\\   §e§l__|__ ");
        sender.sendMessage(" ");

        getLogger().info("Uxi is now enabled");

        //Main Listener
        Bukkit.getPluginManager().registerEvents(new InventoryGuiListener(), this);

        //Extra Listener
        Bukkit.getPluginManager().registerEvents(ChatStringInputListener.getInstance(), this);

        //Task
        Bukkit.getScheduler().runTaskTimer(this, new InventoryGuiUpdater(), 0, 1);
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> InventoryGui.getOpenedInventory(player).ifPresent(s -> player.closeInventory()));


        getLogger().info("Uxi is now disabled");
    }
}
