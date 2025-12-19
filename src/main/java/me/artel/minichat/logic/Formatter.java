package me.artel.minichat.logic;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.files.FileManager;
import me.artel.minichat.hooks.HookManager;
import me.artel.minichat.util.MiniParser;
import me.artel.minichat.util.MiniUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class Formatter {
    // The cached map of player formats, PlayerUUID:Format
    private static final HashMap<String, String> playerFormats = new HashMap<>();
    // The cached map of group formats, GroupName:Format
    private static final HashMap<String, String> groupFormats = new HashMap<>();

    // The Caffeine method to obtain the group to display for a player
    private static final LoadingCache<Player, String> format = Caffeine.newBuilder()
        // Refresh after write so the format dynamically updates
        .refreshAfterWrite(1, TimeUnit.SECONDS)
        .build(player -> {
            if (playerFormats.containsKey(player.getUniqueId().toString())) {
                return playerFormats.get(player.getUniqueId().toString());
            } else if (HookManager.vault() && groupFormats.containsKey(HookManager.vaultChat().getPrimaryGroup(player))) {
                return groupFormats.get(HookManager.vaultChat().getPrimaryGroup(player));
            } else {
                return FileAccessor.FORMAT_GLOBAL;
            }
        });

    public static void update() {
        // We don't need to do anything if formatting isn't enabled
        if (!FileAccessor.FORMAT_ENABLED) {
            return;
        }

        // Dump any existing values
        playerFormats.clear();
        // Get the list of player formats
        FileManager.getFormat().node("player-formats")
            // Iterate over the list
            .childrenList()
            // Try to map out each entry
            .forEach(playerFormat -> {
                playerFormats.put(
                    playerFormat.node("player").getString(),
                    MiniUtil.getStringFromNodeObject(playerFormat.node("format"))
                );
            }
        );

        // Dump any existing values
        groupFormats.clear();
        // Get the list of group formats
        FileManager.getFormat().node("group-formats")
            // Iterate over the list
            .childrenList()
            // Try to map out each entry
            .forEach(groupFormat -> {
                groupFormats.put(
                    groupFormat.node("group").getString(),
                    MiniUtil.getStringFromNodeObject(groupFormat.node("format"))
                );
            }
        );
    }

    public static Component get(Player player, Component message) {
        // TODO: Despite this being run async, I don't want to freshly parse it every time if at all possible
        // Maybe cache the format itself and only parse for the message placeholder? (may cause issues with nicknames not updating)
        return MiniParser.parsePlayer(
            format.get(player),
            player,
            TagResolver.resolver(Placeholder.component("message", message))
        );
    }
}