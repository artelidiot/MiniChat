package me.artel.minichat.checks;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.logic.Check;
import me.artel.minichat.util.MiniParser;

public class MovementCheck extends Check {
    private static final HashMap<UUID, Location> movementRequiredMap = new HashMap<>();

    @Override
    public boolean chat(AsyncChatEvent e) {
        if (e.getPlayer().hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_MOVEMENT)) {
            return false;
        }

        return movement(e.getPlayer(), Action.CHAT);
    }

    @Override
    public boolean command(PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_MOVEMENT)) {
            return false;
        }

        return movement(e.getPlayer(), Action.COMMAND);
    }

    @Override
    public void handle(AsyncChatEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void handle(PlayerCommandPreprocessEvent e) {
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

    /**
     * Add a player to the movement requirement map
     *
     * @param player - The player to add
     */
    public static void addPlayer(Player player) {
        // If the player has both bypass permissions, ignore them
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_MOVEMENT) && player.hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_MOVEMENT)) {
            return;
        }

        movementRequiredMap.put(player.getUniqueId(), player.getLocation());
    }
}