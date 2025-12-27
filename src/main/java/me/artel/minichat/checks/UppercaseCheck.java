package me.artel.minichat.checks;

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.logic.Check;
import me.artel.minichat.util.MiniParser;
import net.kyori.adventure.text.TextReplacementConfig;

public class UppercaseCheck extends Check {
    private static final Pattern uppercasePattern = Pattern.compile("\\p{Lu}");
    private static final TextReplacementConfig uppercaseReplaceConfig = TextReplacementConfig.builder()
        .match(uppercasePattern)
        .replacement((match, builder) ->
            builder.content(match.group().toLowerCase(Locale.ROOT))
        )
        .build();

    @Override
    public boolean chat(AsyncChatEvent e) {
        if (e.getPlayer().hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_UPPERCASE)) {
            return false;
        }

        return uppercase(e.getPlayer(), MiniParser.serializeToPlainText(e.message()), Action.CHAT);
    }

    @Override
    public boolean command(PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_UPPERCASE)) {
            return false;
        }

        return uppercase(e.getPlayer(), e.getMessage(), Action.COMMAND);
    }

    @Override
    public void handle(AsyncChatEvent e) {
        if (isBlocking(Action.CHAT)) {
            e.setCancelled(true);
        } else {
            e.message(e.message().replaceText(uppercaseReplaceConfig));
        }
    }

    @Override
    public void handle(PlayerCommandPreprocessEvent e) {
        if (isBlocking(Action.COMMAND)) {
            e.setCancelled(true);
        } else {
            e.setMessage(e.getMessage().toLowerCase(Locale.ROOT));
        }
    }

    private static boolean uppercase(Player player, String input, Action action) {
        var minimumPercentage = action.equals(Action.CHAT)
            ? FileAccessor.OPTIONS_CHAT_UPPERCASE
            : FileAccessor.OPTIONS_COMMAND_UPPERCASE;

        // This check is not enabled, do nothing
        if (minimumPercentage < 1) {
            return false;
        }

        var processed = input;

        // Check if the input is a command
        if (processed.startsWith("/")) {
            // Remove the command itself from the input
            processed = processed.substring(processed.split(" ")[0].length());
        }

        var ignoreUsernames = action.equals(Action.CHAT)
            ? FileAccessor.OPTIONS_CHAT_UPPERCASE_IGNORE_USERNAMES
            : FileAccessor.OPTIONS_COMMAND_UPPERCASE_IGNORE_USERNAMES;

        // Check if we should ignore usernames
        if (ignoreUsernames) {
            // Iterate over online players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                // Replace their usernames
                processed = processed.replace(onlinePlayer.getName(), "");
            }
        }

        var ignoreList = action.equals(Action.CHAT)
            ? FileAccessor.OPTIONS_CHAT_UPPERCASE_IGNORE_LIST
            : FileAccessor.OPTIONS_COMMAND_UPPERCASE_IGNORE_LIST;

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
            processed = ignorePattern.matcher(processed).replaceAll("");
        }

        var minimumThreshold = action.equals(Action.CHAT)
            ? FileAccessor.OPTIONS_CHAT_UPPERCASE_THRESHOLD
            : FileAccessor.OPTIONS_COMMAND_UPPERCASE_THRESHOLD;

        // Check if the input is now blank, or if the amount of letters is less than the minimum threshold
        if (processed.isBlank() || processed.chars().filter(Character::isLetter).count() < minimumThreshold) {
            return false;
        }

        // TODO: Figure out why an uppercase pattern's #matcher(String)#results()#count() wasn't working
        // Stream the characters
        double uppercasePercentage = (processed.chars()
            // Filter out everything besides letters
            .filter(Character::isLetter)
            // Map the characters out, 1 if uppercase, 0 if lowercase
            .map(entry -> Character.isUpperCase(entry) ? 1 : 0)
            // Summarize
            .summaryStatistics()
            // Get the average of ones vs. zeros, then multiply by 100 to make it a 0-100 scale
            .getAverage()) * 100;

        if (uppercasePercentage >= minimumPercentage) {
            var uppercaseMessage = action.equals(Action.CHAT)
                ? FileAccessor.LOCALE_CHAT_UPPERCASE
                : FileAccessor.LOCALE_COMMAND_UPPERCASE;

            // Notify the player
            player.sendMessage(MiniParser.parseAll(uppercaseMessage, player));
            // The action contains too much uppercase
            return true;
        } else {
            return false;
        }
    }

    private static boolean isBlocking(Action action) {
        var uppercaseAction = action.equals(Action.CHAT)
            ? FileAccessor.OPTIONS_CHAT_UPPERCASE_ACTION
            : FileAccessor.OPTIONS_COMMAND_UPPERCASE_ACTION;

        if (uppercaseAction.equalsIgnoreCase("block")) {
            return true;
        } else if (uppercaseAction.equalsIgnoreCase("normalize")) {
            return false;
        } else {
            // Assume normalize if all else fails
            return false;
        }
    }
}