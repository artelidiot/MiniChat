package me.artel.minichat.logic;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.artel.minichat.MiniChatPlugin;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.files.FileManager;
import me.artel.minichat.util.MiniParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

@Getter @Accessors(fluent = true)
public class Rule {
    @Getter
    private static final ArrayList<Rule> rules = new ArrayList<>();
    private static final Pattern diacriticPattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+", Pattern.CASE_INSENSITIVE);
    private static final String patternException = """
            Error creating rule: %s
            Pattern: '%s' is invalid!
            Info: %s
            """;
    private static final String malformedCommandException = """
            Error creating rule: %s
            All commands must be a String, surrounded by "quotations" or 'apostrophes'!
            Info: %s
            """;

    private static Rule instance;
    private String identifier;
    private boolean enabled;
    private boolean checkAnvils, checkBooks, checkChat, checkCommands, checkSigns;
    private boolean cancel, replace;
    private String replacement;
    private boolean regex;
    private String trigger, response;
    private Pattern triggerPattern;
    private List<String> commands;

    public Rule(ConfigurationNode ruleNode) {
        // We don't need to do anything at all if rules are not enabled
        if (!FileAccessor.RULES_ENABLED) {
            return;
        }

        // We don't need to do anything if the rule itself isn't enabled
        if (!ruleNode.node("enabled").getBoolean(false)) {
            return;
        }

        // Create the identifier for the rule
        // TODO: Support multiple aliases for creating an identifier?
        this.identifier = ruleNode.node("id").getString();

        // Create the rule
        this.checkAnvils = ruleNode.node("check-anvils", true).getBoolean(true);
        this.checkBooks = ruleNode.node("check-books").getBoolean(true);
        this.checkChat = ruleNode.node("check-chat").getBoolean(true);
        this.checkCommands = ruleNode.node("check-commands").getBoolean(true);
        this.checkSigns = ruleNode.node("check-signs").getBoolean(true);

        this.cancel = ruleNode.node("cancel").getBoolean(false);
        this.replace = ruleNode.node("replace").getBoolean(false);
        this.replacement = ruleNode.node("replacement").getString("");
        this.regex = ruleNode.node("regex").getBoolean(false);
        this.trigger = ruleNode.node("trigger").getString("");
        this.response = ruleNode.node("response").getString("");

        try {
            if (regex) {
                this.triggerPattern = Pattern.compile(trigger, Pattern.CASE_INSENSITIVE);
            }
        } catch (PatternSyntaxException e) {
            this.enabled = false;
            MiniChatPlugin.getInstance().getLogger().warning(
                    patternException.formatted(identifier, trigger, e.getMessage())
            );
            return;
        }

        try {
            this.commands = ruleNode.node("commands").getList(String.class, Collections.emptyList());
        } catch (SerializationException e) {
            this.enabled = false;
            MiniChatPlugin.getInstance().getLogger().warning(
                    malformedCommandException.formatted(identifier, e.getMessage())
            );
            return;
        }

        // Create an instance of the rule for static access
        instance = this;

        // Add this rule to the list of enabled rules
        rules.add(instance);
    }

    /**
     * Method to remove all existing rules and repopulate the list of rules with up-to-date values from the rules file.
     */
    public static void repopulate() {
        // Clear any existing rules so we don't add duplicates
        rules.clear();

        // Iterate over anything in the list and try to create a rule from it
        FileManager.getRules().node("list").childrenList().forEach(Rule::new);
    }

    /**
     * Method to check if a String matches the Rule
     *
     * @param player - The {@link Player} who performed the action
     * @param input - The {@link String} from the action
     * @return - {@link Boolean} of whether the input matches
     */
    public boolean matcher(Player player, String input) {
        // We don't need to do anything if the input is nothing
        if (input.isBlank()) {
            return false;
        }

        // If the player bypasses this rule, we don't need to do anything
        if (player.hasPermission(FileAccessor.PERMISSION_BYPASS_RULE.formatted(identifier))) {
            return false;
        }

        // Check if stripping diacritical marks is desired
        if (FileAccessor.RULES_STRIP_DIACRITICAL_MARKS) {
            // Replace all diacritical marks with nothing
            input = Normalizer.normalize(input, Normalizer.Form.NFD)
                    .replaceAll(diacriticPattern.pattern(), "");
        }

        return regex
                // Match to a proper RegEx if it is enabled
                ? triggerPattern.matcher(input).find()
                // Match to a standard string check otherwise
                : input.toLowerCase(Locale.ROOT).contains(trigger.toLowerCase(Locale.ROOT));
    }

    /**
     * Method to check if a Component's contents matches the Rule
     *
     * @param player - The {@link Player} who performed the action
     * @param input - The {@link Component} from the action
     * @return - {@link Boolean} of whether the Component's content matches
     */
    public boolean matcher(Player player, Component input) {
        return matcher(player, MiniParser.serializeToPlainText(input));
    }

    /**
     * Method to handle a violation of the Rule
     *
     * @param player - The {@link Player} who performed the action
     * @param input - The {@link Component} from the action
     * @param event - The action itself
     * @return - The replaced input (if the action wasn't cancelled)
     */
    public Component catcher(Player player, Component input, Cancellable event) {
        // We don't need to do anything if the event is already cancelled for some reason
        if (event.isCancelled()) {
            return input;
        }

        // We don't need to do anything if the input doesn't match the rule
        if (!matcher(player, input)) {
            return input;
        }

        // Check if replacement is desired
        if (replace) {
            // Replace the matches with the corresponding trigger
            if (regex) {
                input = input.replaceText(
                        TextReplacementConfig.builder()
                                .match(triggerPattern)
                                .replacement(replacement)
                                .build()
                );
            } else {
                input = input.replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral(trigger)
                                .replacement(replacement)
                                .build()
                );
            }
        } else if (cancel) {
            // Replacement was not desired, cancel the event
            event.setCancelled(true);
        }

        runCommands(player);
        sendResponse(player);

        return input;
    }

    /**
     * Method to handle a violation of the Rule
     *
     * @param player - The {@link Player} who performed the action
     * @param input - The {@link String} from the action
     * @param event - The action itself
     * @return - The replaced input (if the action wasn't cancelled)
     */
    public String catcher(Player player, String input, Cancellable event) {
        // We don't need to do anything if the event is already cancelled for some reason
        if (event.isCancelled()) {
            return input;
        }

        // We don't need to do anything if the input doesn't match the rule
        if (!matcher(player, input)) {
            return input;
        }

        // Check if replacements are desired
        if (replace) {
            // Apply the replacements to the corresponding trigger
            input = input.replaceAll(regex ? triggerPattern.pattern() : trigger, replacement);
        } else if (cancel) {
            // Replacement was not desired, cancel the event
            event.setCancelled(true);
        }

        runCommands(player);
        sendResponse(player);

        return input;
    }

    /**
     * Method to execute the Rule's commands when a violation occurs
     *
     * @param player - The {@link Player} who performed the action
     */
    public void runCommands(Player player) {
        // We don't need to do anything if no commands are found
        if (commands == null || commands.isEmpty()) {
            return;
        }

        for (var command : commands) {
            // Commands executed from console always have a slash prefix, remove it if present
            if (command.startsWith("/")) {
                command = command.substring(1);
            }

            // Create a variable to finalize the command for the lambda statement
            // TODO: Make sure components can be used to parse player placeholders; or check if Paper has their own methods
            String finalCommand = command;
            // Execute the command explicitly on sync; errors are thrown if async
            Bukkit.getScheduler().callSyncMethod(MiniChatPlugin.getInstance(),
                    () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand));
        }
    }

    /**
     * Method to send a Rule's response when a violation occurs
     *
     * @param player - The {@link Player} who performed the action
     */
    public void sendResponse(Player player) {
        player.sendMessage(MiniParser.parseAll(response, player));
    }
}