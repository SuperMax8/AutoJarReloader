package fr.supermax_8.autojarreloader;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class AutoJarReloader extends JavaPlugin {

    @Getter
    private static AutoJarReloader instance;

    private final List<JarReloader> reloaders = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;
        loadPl();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            synchronized (reloaders) {
                for (JarReloader reloader : reloaders) reloader.tick();
            }
        }, 0, 0);
        getCommand("autojarreload").setExecutor(new AutoJarReloadCommand());
        new Metrics(this, 27081);
    }

    public void loadPl() {
        saveDefaultConfig();
        reloadConfig();

        if (!getConfig().getBoolean("working")) {
            log("§6§lPlugin off! Production mode");
            synchronized (reloaders) {
                reloaders.clear();
            }
            return;
        }
        int i = 0;
        synchronized (reloaders) {
            reloaders.clear();
            for (String name : getConfig().getStringList("plugins")) {
                JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(name);
                if (plugin == null) continue;
                reloaders.add(new JarReloader(plugin.getName(), getPluginFile(plugin)));
                i++;
            }
        }
        log("Plugin loaded with §6§l" + i + " §7plugin reloaders!");
    }

    @Override
    public void onDisable() {
    }

    public static void log(String message) {
        String s = "§8[§e§lAutoJarReloader§8] §7" + message;
        Bukkit.getConsoleSender().sendMessage(s);
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("autojarreload")) player.sendMessage(s);
        });
    }

    public static File getPluginFile(JavaPlugin plugin) {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true); // override protected
            return (File) method.invoke(plugin);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access getFile() via reflection", e);
        }
    }

}
