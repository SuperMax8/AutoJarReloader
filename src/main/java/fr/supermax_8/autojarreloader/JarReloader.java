package fr.supermax_8.autojarreloader;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class JarReloader {

    private final File pluginJar;
    private long lastModified;
    private final String pluginName;
    private long lastReload = 0;

    private long lastSize = -1;
    private long stableSince = -1;

    private State state = State.TICKING;

    public JarReloader(String pluginName, File pluginJar) {
        this.pluginName = pluginName;
        this.pluginJar = pluginJar;
        this.lastModified = pluginJar.lastModified();
    }

    public void tick() {
        if (state == State.RELOADING)
            return;
        if (state == State.STABILIZING) {
            checkFile();
            return;
        }
        long currentModified = pluginJar.lastModified();
        if (currentModified != lastModified && (System.currentTimeMillis() - lastReload) > 10000) {
            lastModified = currentModified;
            state = State.STABILIZING;
            checkFile();
        }
    }

    public void checkFile() {
        long currentSize = pluginJar.length();
        long now = System.currentTimeMillis();

        if (currentSize != lastSize) {
            lastSize = currentSize;
            stableSince = now;
            return; // still changing
        }

        if (now - stableSince > 3500) {
            // stable
            AutoJarReloader.log("Jar stabilized in " + (now - stableSince) + " ms");
            reload();
        }
    }

    private void reload() {
        state = State.RELOADING;
        Bukkit.getScheduler().runTaskLater(AutoJarReloader.getInstance(), () -> {
            AutoJarReloader.log("Detected change in " + pluginJar.getName() + ", reloading...");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "plugman reload " + pluginName);
            lastReload = System.currentTimeMillis();
            lastModified = pluginJar.lastModified();
            state = State.TICKING;
        }, 3);
    }

    enum State {
        TICKING,
        STABILIZING,
        RELOADING
    }

}