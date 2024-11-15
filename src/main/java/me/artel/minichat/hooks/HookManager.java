package me.artel.minichat.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.milkbowl.vault.chat.Chat;

@Accessors(fluent = true)
public class HookManager {
    @Getter
    private static boolean vault, placeholderAPI;
    @Getter
    private static Chat vaultChat = null;

    public static void handle() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault") && chatProvider()) {
            vault = true;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPI = true;
        }
    }

    public static boolean chatProvider() {
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return false;
        }
        vaultChat = rsp.getProvider();
        return true;
    }
}