package me.artel.minichat.checks.impl;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import lombok.Getter;
import me.artel.minichat.checks.MiniCheck;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.util.MiniParser;
import me.artel.minichat.util.MiniUtil;
import net.kyori.adventure.text.Component;

public class SimilarityCheck implements MiniCheck {
    @Getter
    private static final HashMap<UUID, String>
        chatHistoryMap = new HashMap<>(),
        commandHistoryMap = new HashMap<>();

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
        // TODO: Send message here?
        e.setCancelled(true);
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

        var historyMap = action.equals(Action.CHAT)
            ? chatHistoryMap
            : commandHistoryMap;

        // If there's no data to compare to, we cannot continue
        if (!historyMap.containsKey(player.getUniqueId())) {
            return false;
        }

        var ignoreUsernames = action.equals(Action.CHAT)
            ? FileAccessor.OPTIONS_CHAT_SIMILARITY_IGNORE_USERNAMES
            : FileAccessor.OPTIONS_COMMAND_SIMILARITY_IGNORE_USERNAMES;

        var historyContent = historyMap.get(player.getUniqueId());

        // Check if we should ignore usernames
        if (ignoreUsernames) {
            // Iterate over online players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                // Replace their usernames
                input = input.replace(onlinePlayer.getName(), "");
                historyContent = historyContent.replace(onlinePlayer.getName(), "");
            }
        }

        var ignoreList = action.equals(Action.CHAT)
            ? FileAccessor.OPTIONS_CHAT_SIMILARITY_IGNORE_LIST
            : FileAccessor.OPTIONS_COMMAND_SIMILARITY_IGNORE_LIST;

        // Check if the ignore list is empty
        if (!ignoreList.isEmpty()) {
            // Build a RegEx pattern for the ignore list
            Pattern ignorePattern = Pattern.compile(
                // Stream all entries
                ignoreList.stream()
                    // Quote it
                    .map(Pattern::quote)
                    // Compile them using an OR delimiter
                    .collect(Collectors.joining("|")),
                // The ignore list should be case-insensitive
                Pattern.CASE_INSENSITIVE
            );

            // Run the replacements
            input = ignorePattern.matcher(input).replaceAll("");
            historyContent = ignorePattern.matcher(historyContent).replaceAll("");
        }

        var threshold = action.equals(Action.CHAT)
            ? FileAccessor.OPTIONS_CHAT_SIMILARITY_THRESHOLD
            : FileAccessor.OPTIONS_COMMAND_SIMILARITY_THRESHOLD;

        // Check if the input's length exceeds the threshold for this check
        if (input.length() < threshold) {
            return false;
        }

        // Check if the input's similarity to the previous data exceeds the threshold
        if ((MiniUtil.getJaroWinkler().similarity(input, historyContent) * 100) > similarity) {
            var similarityMessage = action.equals(Action.CHAT)
                ? FileAccessor.LOCALE_CHAT_SIMILARITY
                : FileAccessor.LOCALE_COMMAND_SIMILARITY;

            // Notify the player
            player.sendMessage(MiniParser.parseAll(similarityMessage, player));
            // Their message is too similar, cancel it
            return true;
        } else {
            return false;
        }
    }
}