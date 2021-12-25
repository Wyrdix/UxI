package fr.wyrdix;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class UxiPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        getLogger().info("Uxi is now loaded");
    }

    @Override
    public void onEnable() {
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage("                §e§l_____");
        sender.sendMessage("  §e|    |  §7\\  /  §e  |   §eUser Extra Interface");
        sender.sendMessage("  §e|    |  §7 \\/   §e  |   §r§7v."+getDescription().getVersion());
        sender.sendMessage("  §e§l|____|§r  §7 /\\   §e§l__|__ ");
        sender.sendMessage(" ");


        getLogger().info("Uxi is now enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Uxi is now disabled");
    }
}
