package me.artel.minichat.checks.impl;

import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import me.artel.minichat.checks.MiniCheck;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.util.MiniParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

public class UppercaseCheck implements MiniCheck {
    private static final Pattern uppercasePattern = Pattern.compile("\\p{Lu}");
    private static final Pattern lowercasePattern = Pattern.compile("\\p{Ll}");

    public static boolean chat(Player player, Component input) {
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_UPPERCASE)) {
            return false;
        }

        return uppercase(player, MiniParser.serializeToPlainText(input), Action.CHAT);
    }

    public static boolean command(Player player, String input) {
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_UPPERCASE)) {
            return false;
        }

        return uppercase(player, input, Action.COMMAND);
    }

    public static Component handle(Player player, Component input, Cancellable e) {
        if (isBlocking(Action.CHAT)) {
            e.setCancelled(true);
        } else {
            input = input.replaceText(TextReplacementConfig.builder()
                    .match(uppercasePattern)
                    .replacement(lowercasePattern.pattern())
                    .build());
        }

        player.sendMessage("DEBUG: Uppercase check handled");
        return input;
    }

    public static String handle(Player player, String input, Cancellable e) {
        if (isBlocking(Action.COMMAND)) {
            e.setCancelled(true);
        } else {
            input = input.toLowerCase();
        }

        player.sendMessage("DEBUG: Uppercase check handled");
        return input;
    }

    private static boolean uppercase(Player player, String input, Action action) {
        var minimumPercentage = action.equals(Action.CHAT)
                ? FileAccessor.OPTIONS_CHAT_UPPERCASE
                : FileAccessor.OPTIONS_COMMAND_UPPERCASE;

        // This check is not enabled, do nothing
        if (minimumPercentage < 1) {
            player.sendMessage("DEBUG: Uppercase check 'minimumPercentage'");
            return false;
        }

        var processed = input;

        // Check if the input is a command
        if (processed.startsWith("/")) {
            // Remove the command itself from the input
            processed = processed.substring(processed.split(" ")[0].length());
        }

        var minimumThreshold = action.equals(Action.CHAT)
                ? FileAccessor.OPTIONS_CHAT_UPPERCASE_THRESHOLD
                : FileAccessor.OPTIONS_COMMAND_UPPERCASE_THRESHOLD;

        // Check if the input is now blank, or if the amount of letters is less than the minimum threshold
        if (processed.isBlank() || processed.chars().filter(Character::isLetter).count() < minimumThreshold) {
            player.sendMessage("DEBUG: Uppercase check blank or 'minimumThreshold'");
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

        // Check the percentage of uppercase
        return uppercasePercentage >= minimumPercentage;
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