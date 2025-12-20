package me.artel.minichat.checks.impl;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import lombok.Setter;
import me.artel.minichat.checks.MiniCheck;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.util.MiniParser;
import me.artel.minichat.util.MiniUtil;
import net.kyori.adventure.text.Component;

public class ParrotCheck implements MiniCheck {
    @Setter
    private static String latestMessage = null;

    public static boolean chat(Player player, Component input) {
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_PARROT)) {
            return false;
        }

        return parrot(player, MiniParser.serializeToPlainText(input));
    }

    public static void handle(Player player, Cancellable e) {
        // TODO: Send message here?
        e.setCancelled(true);
    }

    private static boolean parrot(Player player, String input) {
        // This check is not enabled, do nothing
        if (FileAccessor.OPTIONS_CHAT_PARROTING < 1) {
            return false;
        }

        // If there's no data to compare to, we cannot continue
        if (latestMessage == null) {
            return false;
        }

        var processedLatestMessage = latestMessage;

        // Check if we should ignore usernames
        if (FileAccessor.OPTIONS_CHAT_PARROTING_IGNORE_USERNAMES) {
            // Iterate over online players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                // Replace their usernames
                input = input.replace(onlinePlayer.getName(), "");
                processedLatestMessage = processedLatestMessage.replace(onlinePlayer.getName(), "");
            }
        }

        // Check if the ignore list is empty
        if (!FileAccessor.OPTIONS_CHAT_PARROTING_IGNORE_LIST.isEmpty()) {
            // Build a RegEx pattern for the ignore list
            Pattern ignorePattern = Pattern.compile(
                // Stream all entries
                FileAccessor.OPTIONS_CHAT_PARROTING_IGNORE_LIST.stream()
                    // Quote it
                    .map(Pattern::quote)
                    // Compile them using an OR delimiter
                    .collect(Collectors.joining("|")),
                // The ignore list should be case-insensitive
                Pattern.CASE_INSENSITIVE
            );

            // Run the replacements
            input = ignorePattern.matcher(input).replaceAll("");
            processedLatestMessage = ignorePattern.matcher(processedLatestMessage).replaceAll("");
        }

        // If either the input or chat history are blank, do nothing
        if (input.isBlank() || processedLatestMessage.isBlank()) {
            return false;
        }

        // Check if the input's length exceeds the threshold for this check
        if (input.length() < FileAccessor.OPTIONS_CHAT_PARROTING_THRESHOLD || processedLatestMessage.length() < FileAccessor.OPTIONS_CHAT_PARROTING_THRESHOLD) {
            return false;
        }

        // Check if the input's similarity to the previous data exceeds the threshold
        if ((MiniUtil.getJaroWinkler().similarity(input, processedLatestMessage) * 100) > FileAccessor.OPTIONS_CHAT_PARROTING) {
            // Notify the player
            player.sendMessage(MiniParser.parseAll(FileAccessor.LOCALE_CHAT_PARROT, player));
            // Their message is too similar, cancel it
            return true;
        } else {
            return false;
        }
    }
}