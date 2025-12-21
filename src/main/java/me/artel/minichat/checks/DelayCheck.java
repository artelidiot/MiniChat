package me.artel.minichat.checks;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.logic.Check;
import me.artel.minichat.util.MiniParser;
import me.artel.minichat.util.MiniUtil;

public class DelayCheck extends Check {
    // TODO: Use a Caffeine cache? Might be *slightly* more efficient than a map in this use case thanks to an #expireAfterWrite call
    @Getter
    private static final HashMap<UUID, Long>
        chatDelayMap = new HashMap<>(),
        commandDelayMap = new HashMap<>();

    @Override
    public boolean chat(AsyncChatEvent e) {
        if (e.getPlayer().hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_DELAY)) {
            return false;
        }

        return delay(e.getPlayer(), Action.CHAT);
    }

    @Override
    public boolean command(PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_DELAY)) {
            return false;
        }

        return delay(e.getPlayer(), Action.COMMAND);
    }

    @Override
    public void handle(AsyncChatEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void handle(PlayerCommandPreprocessEvent e) {
        e.setCancelled(true);
    }

    public static boolean delay(Player player, Action action) {
        var delayMap = action.equals(Action.CHAT)
            ? chatDelayMap
            : commandDelayMap;

        // Check if the player has an existing delay
        if (delayMap.containsKey(player.getUniqueId())) {
            var delay = action.equals(Action.CHAT)
                ? FileAccessor.OPTIONS_CHAT_DELAY
                : FileAccessor.OPTIONS_COMMAND_DELAY;

            // Check if the elapsed time is greater than the configured value
            // TODO: Write time handlers (seconds, milliseconds, etc.)
            if (MiniUtil.elapsedTime(delayMap.get(player.getUniqueId()), TimeUnit.MILLISECONDS) >= delay) {
                // The elapsed time has passed the value, remove the player
                delayMap.remove(player.getUniqueId());
                return false;
            } else {
                var delayMessage = action.equals(Action.CHAT)
                    ? FileAccessor.LOCALE_CHAT_DELAY
                    : FileAccessor.LOCALE_COMMAND_DELAY;

                // Notify the player
                player.sendMessage(MiniParser.parseAll(delayMessage, player));
                // The player is still on a delay
                return true;
            }
        } else {
            return false;
        }
    }
}