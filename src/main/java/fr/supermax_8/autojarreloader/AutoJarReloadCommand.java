package fr.supermax_8.autojarreloader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AutoJarReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!commandSender.hasPermission("autojarreload")) return false;
        AutoJarReloader.getInstance().loadPl();
        return true;
    }

}
