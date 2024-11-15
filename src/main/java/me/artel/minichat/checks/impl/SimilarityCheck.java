package me.artel.minichat.checks.impl;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import info.debatty.java.stringsimilarity.JaroWinkler;
import me.artel.minichat.checks.MiniCheck;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.util.MiniParser;
import net.kyori.adventure.text.Component;

public class SimilarityCheck implements MiniCheck {
    private static final JaroWinkler jaroWinkler = new JaroWinkler();

    private static final HashMap<UUID, String> chatHistoryMap = new HashMap<>();
    private static final HashMap<UUID, String> commandHistoryMap = new HashMap<>();

    public static boolean chat(Player player, Component input) {
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_SIMILARITY)) {
            return false;
        }

        return similar(player, MiniParser.serializeToPlainText(input), Action.CHAT);
    }

    public static boolean command(Player player, String input) {
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_SIMILARITY)) {
            return false;
        }

        return similar(player, input, Action.COMMAND);
    }

    public static void handle(Player player, Cancellable e) {
        // TODO: Send message
        e.setCancelled(true);
        player.sendMessage("similarity");
    }

    private static boolean similar(Player player, String input, Action action) {
        // If the input is blank, do nothing
        if (input.isBlank()) {
            return false;
        }

        var similarity = action.equals(Action.CHAT)
                ? FileAccessor.OPTIONS_CHAT_SIMILARITY
                : FileAccessor.OPTIONS_COMMAND_SIMILARITY;

        // This check is not enabled, do nothing
        if (similarity < 1) {
            return false;
        }

        var threshold = action.equals(Action.CHAT)
                ? FileAccessor.OPTIONS_CHAT_SIMILARITY_THRESHOLD
                : FileAccessor.OPTIONS_COMMAND_SIMILARITY_THRESHOLD;

        // Check if the input's length exceeds the threshold for this check
        if (input.length() < threshold) {
            return false;
        }

        var historyMap = action.equals(Action.CHAT)
                ? chatHistoryMap
                : commandHistoryMap;

        // If there's no data to compare to, we cannot continue
        if (!historyMap.containsKey(player.getUniqueId())) {
            // Speaking of data, add this input to the map for future checks
            historyMap.put(player.getUniqueId(), input);
            return false;
        }

        // Finally, check if the input's similarity exceeds the mapped data
        return jaroWinkler.similarity(historyMap.get(player.getUniqueId()), input) > similarity;
    }
}