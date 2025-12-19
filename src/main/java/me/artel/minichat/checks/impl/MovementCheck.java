package me.artel.minichat.checks.impl;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import me.artel.minichat.checks.MiniCheck;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.util.MiniParser;

public class MovementCheck implements MiniCheck {
    private static final HashMap<UUID, Location> movementRequiredMap = new HashMap<>();

    public static boolean chat(Player player) {
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_MOVEMENT)) {
            return false;
        }

        return movement(player, Action.CHAT);
    }

    public static boolean command(Player player) {
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_MOVEMENT)) {
            return false;
        }

        return movement(player, Action.COMMAND);
    }

    public static void handle(Player player, Cancellable e) {
        // TODO: Send message here?
        e.setCancelled(true);
    }

    private static boolean movement(Player player, Action action) {
        var movementRequired = action.equals(Action.CHAT)
            ? FileAccessor.OPTIONS_MOVEMENT_REQUIRED_CHAT
            : FileAccessor.OPTIONS_MOVEMENT_REQUIRED_COMMAND;

        // Run some checks to make sure this feature is enabled
        if (!movementRequired) {
            return false;
        }

        // We don't need to do anything if the player isn't on the list
        if (!movementRequiredMap.containsKey(player.getUniqueId())) {
            return false;
        }

        // Check if the player has moved since logging in
        if (distanceMoved(player) > 1) {
            // They have moved, remove them from the list
            movementRequiredMap.remove(player.getUniqueId());
            return false;
        } else {
            var movementMessage = action.equals(Action.CHAT)
                ? FileAccessor.LOCALE_CHAT_MOVEMENT
                : FileAccessor.LOCALE_COMMAND_MOVEMENT;

            // Notify the player
            player.sendMessage(MiniParser.parseAll(movementMessage, player));
            // They haven't moved, prevent their action
            return true;
        }
    }

    private static double distanceMoved(Player player) {
        // Check the distance (in blocks) to the player's login location
        return player.getLocation().distance(movementRequiredMap.get(player.getUniqueId()));
    }
}