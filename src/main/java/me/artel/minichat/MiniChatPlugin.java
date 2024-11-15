package me.artel.minichat;

import static me.artel.minichat.commands.CommandManager.Stage.DISABLE;
import static me.artel.minichat.commands.CommandManager.Stage.ENABLE;
import static me.artel.minichat.commands.CommandManager.Stage.LOAD;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.artel.minichat.commands.CommandManager;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.files.FileManager;
import me.artel.minichat.hooks.HookManager;
import me.artel.minichat.listeners.PlayerListeners;
import me.artel.minichat.logic.Formatter;
import me.artel.minichat.logic.Rule;

public class MiniChatPlugin extends JavaPlugin {
    @Getter
    private static MiniChatPlugin instance;

    @Override
    public void onLoad() {
        instance = this;

        CommandManager.handle(LOAD);
    }

    @Override
    public void onEnable() {
        // Hook initialization
        HookManager.handle();

        // Handle file, format, MOTD, and rule initialization
        reload();

        // Command initialization
        CommandManager.handle(ENABLE);

        // Listener initialization
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), this);
    }

    @Override
    public void onDisable() {
        CommandManager.handle(DISABLE);
    }

    /**
     * Method to reload the plugin's configuration files and cached values
     */
    public static void reload() {
        FileManager.reloadFiles();
        FileAccessor.update();

        Formatter.update();
        // MOTD.update();
        Rule.repopulate();
    }
}