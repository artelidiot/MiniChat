package me.artel.minichat.checks.impl;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import me.artel.minichat.checks.MiniCheck;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.util.MiniUtil;

public class DelayCheck implements MiniCheck {
    private static final HashMap<UUID, Long> chatDelayMap = new HashMap<>();
    private static final HashMap<UUID, Long> commandDelayMap = new HashMap<>();

    public static boolean chat(Player player) {
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_DELAY)) {
            return false;
        }

        return delay(player, Action.CHAT);
    }

    public static boolean command(Player player) {
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_DELAY)) {
            return false;
        }

        return delay(player, Action.COMMAND);
    }

    public static void handle(Player player, Cancellable e) {
        // TODO: Send message
        e.setCancelled(true);
        player.sendMessage("delay");
    }

    public static boolean delay(Player player, Action action) {
        var delayMap = action.equals(Action.CHAT)
                ? chatDelayMap
                : commandDelayMap;
        var delay = action.equals(Action.CHAT)
                ? FileAccessor.OPTIONS_CHAT_DELAY
                : FileAccessor.OPTIONS_COMMAND_DELAY;

        // Check if the player has an existing delay
        if (delayMap.containsKey(player.getUniqueId())) {
            // Check if the elapsed time is greater than the configured value
            // TODO: Write time handlers (seconds, milliseconds, etc.)
            if (MiniUtil.elapsedTime(delayMap.getOrDefault(player.getUniqueId(), -1L), TimeUnit.MILLISECONDS) >= delay) {
                // The elapsed time has passed the value, remove the player
                delayMap.remove(player.getUniqueId());
                return false;
            } else {
                // The elapsed time has not passed the value
                return true;
            }
        } else {
            // The player had no existing delay, put them on one
            delayMap.put(player.getUniqueId(), System.nanoTime());
            return false;
        }
    }
}