package me.artel.minichat.checks;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.logic.Check;
import me.artel.minichat.util.MiniParser;
import me.artel.minichat.util.MiniUtil;

public class SimilarityCheck extends Check {
    @Getter
    private static final HashMap<UUID, String>
        chatHistoryMap = new HashMap<>(),
        commandHistoryMap = new HashMap<>();

    @Override
    public boolean chat(AsyncChatEvent e) {
        if (e.getPlayer().hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_SIMILARITY)) {
            return false;
        }

        return similar(e.getPlayer(), MiniParser.serializeToPlainText(e.message()), Action.CHAT);
    }

    @Override
    public boolean command(PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_SIMILARITY)) {
            return false;
        }

        return similar(e.getPlayer(), e.getMessage(), Action.COMMAND);
    }

    @Override
    public void handle(AsyncChatEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void handle(PlayerCommandPreprocessEvent e) {
        e.setCancelled(true);
    }

    // TODO: Figure out why uppercase violations are causing this not to flag
    private static boolean similar(Player player, String input, Action action) {
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

        // Check if the input is blank or input's length doesn't meet the threshold
        if (input.isBlank() || input.length() < threshold) {
            return false;
        }

        // Check if the input's similarity to the previous data exceeds the threshold
        if ((MiniUtil.getJaroWinkler().similarity(input, historyContent) * 100) >= similarity) {
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