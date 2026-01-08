package me.artel.minichat.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;

import lombok.Getter;
import me.artel.minichat.MiniChatPlugin;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.files.FileManager;
import me.artel.minichat.util.MiniParser;
import me.artel.minichat.util.MiniUtil;

@Getter
public class MOTD {
    @Getter
    private static final ArrayList<MOTD> motds = new ArrayList<>();

    private String identifier;
    private int delay;
    private String content;
    private List<Predicate<Player>> conditions;
    private int conditionCount;

    // TODO: These messages are quite vague, try to improve them
    private static final String unknownConditionWarning = """
        Error creating MOTD: %s
        Condition: %s is not a valid condition!
        """;
    private static final String predicateValidationWarning = """
        Error creating MOTD: %s
        Predicate: %s could not be validated! This is probably a syntax error.
        """;
    private static final String parsingException = """
        Error creating MOTD: %s
        Something went wrong while parsing the conditions!
        Info: %s
        """;

    public MOTD(ConfigurationNode motdNode, List<Predicate<Player>> conditions) {
        // Create the identifier for the MOTD
        this.identifier = motdNode.node("id").getString("");

        // Create the MOTD
        this.delay = MiniUtil.clampMin(motdNode.node("delay").getInt(0), 0);
        // Make the delay operate in milliseconds by dividing the value by 50
        this.delay = (delay > 0) ? (delay / 50) : 0;
        this.content = MiniUtil.getStringFromNodeObject(motdNode.node("content"));
        this.conditions = conditions;
        this.conditionCount = this.conditions.size();

        // Add this MOTD to the list of enabled MOTDs
        motds.add(this);
    }

    /**
     * Method to remove all existing MOTDs and repopulate the list of MOTDs with up-to-date values from the MOTD file.
     */
    public static void repopulate() {
        // Clear any existing MOTDs so we don't add duplicates
        motds.clear();

        // We don't need to do anything at all if MOTDs are not enabled
        if (!FileAccessor.MOTD_ENABLED) {
            return;
        }

        // Iterate over anything in the list and try to create a MOTD from it
        FileManager.getMOTD().node("list")
            .childrenList()
            .forEach(motdNode -> {
                // We don't need to do anything if the MOTD itself isn't enabled
                if (!motdNode.node("enabled").getBoolean(false)) {
                    return;
                }

                var id = motdNode.node("id").getString("unspecified");

                // Make sure there's no malformed conditions
                List<Predicate<Player>> conditions = validateConditions(motdNode, id);
                if (conditions == null) {
                    return;
                }

                // Create the MOTD
                new MOTD(motdNode, conditions);
            });
    }

    private static List<Predicate<Player>> validateConditions(ConfigurationNode motdNode, String identifier) {
        var conditionsNode = motdNode.node("conditions");

        // No conditions specified, handle as unconditional
        if (conditionsNode.empty()) {
            return List.of();
        }

        List<Predicate<Player>> valid = new ArrayList<>();

        // Iterate over the conditional entries as a map
        for (var entry : conditionsNode.childrenMap().entrySet()) {
            var condition = Conditional.getPlayerConditions().get(entry.getKey());

            // We found a condition we don't recognize
            if (condition == null) {
                MiniChatPlugin.getInstance().getLogger()
                    .warning(unknownConditionWarning.formatted(identifier, entry.getKey()));
                return null;
            }

            try {
                // Create a predicate from the specified condition
                var predicate = condition.fromNode(entry.getValue());

                // The predicate returned null, likely from an unexpected value
                if (predicate == null) {
                    MiniChatPlugin.getInstance().getLogger()
                        .warning(predicateValidationWarning.formatted(identifier, entry.getKey()));
                    return null;
                }

                // Add this predicate to the valid list
                valid.add(predicate);
            } catch (Exception e) {
                MiniChatPlugin.getInstance().getLogger()
                    .warning(parsingException.formatted(identifier, e.getMessage()));
                return null;
            }
        }

        // Everything validated successfully, return the valid list
        return valid;
    }

    private boolean meetsConditions(Player player) {
        // We don't need to iterate over empty lists
        if (conditions.isEmpty()) {
            return true;
        }

        // Iterate over the conditions
        for (var condition : conditions) {
            // Return false if one of them isn't met
            if (!condition.test(player)) {
                return false;
            }
        }

        // The player meets all conditions
        return true;
    }

    private void sendTo(Player player) {
        // We don't need to send an empty message
        if (content == null || content.isBlank()) {
            return;
        }

        if (delay > 0) {
            // Create a reference to the player's UUID for later
            var uuid = player.getUniqueId();

            // Send the content after the specified delay
            Bukkit.getScheduler().runTaskLater(MiniChatPlugin.getInstance(),
                () -> {
                    // Retrieve the player from the stored UUID
                    Player p = Bukkit.getPlayer(uuid);

                    // Make sure the player still exists
                    if (p != null && p.isOnline()) {
                        // Send the content
                        p.sendMessage(MiniParser.parseAll(content, p));
                    }
                },
                delay
            );
        } else {
            // Send the content immediately
            player.sendMessage(MiniParser.parseAll(content, player));
        }
    }

    private void sendTo(CommandSender sender) {
        sender.sendMessage(MiniParser.parsePlugin(content));
    }

    /**
     * Method to send a random MOTD to a {@link Player} without conditions
     *
     * @param player - The {@link Player} who will receive the MOTD
     */
    public static void send(Player player) {
        // We don't need to iterate over empty lists
        if (motds.isEmpty()) {
            return;
        }

        // Grab a random MOTD from the list of MOTDs
        var randomized = motds.get(ThreadLocalRandom.current().nextInt(motds.size()));

        // Make sure it isn't null
        if (randomized != null) {
            // Send it to the player
            randomized.sendTo(player);
        }
    }

    /**
     * Method to send a random MOTD to a {@link CommandSender} without conditions
     *
     * @param sender - The {@link CommandSender} who will receive the MOTD
     */
    public static void send(CommandSender sender) {
        // We don't need to iterate over empty lists
        if (motds.isEmpty()) {
            return;
        }

        // Grab a random MOTD from the list of MOTDs
        var randomized = motds.get(ThreadLocalRandom.current().nextInt(motds.size()));

        // Make sure it isn't null
        if (randomized != null) {
            // Send it to the player
            randomized.sendTo(sender);
        }
    }

    /**
     * Method to send an MOTD to a {@link Player} by identifier
     *
     * @param player - The {@link Player} who will receive the MOTD
     * @param identifier - The identifier of the desired MOTD
     */
    public static void send(Player player, String identifier) {
        // Iterate over the MOTDs
        for (MOTD motd : motds) {
            // Make sure the identifier matches the specified one
            if (motd.identifier.equalsIgnoreCase(identifier)) {
                // Send the first match and return
                motd.sendTo(player);
                return;
            }
        }
    }

    /**
     * Method to send an MOTD to a {@link CommandSender} by identifier
     *
     * @param sender - The {@link CommandSender} who will receive the MOTD
     * @param identifier - The identifier of the desired MOTD
     */
    public static void send(CommandSender sender, String identifier) {
        // Iterate over the MOTDs
        for (MOTD motd : motds) {
            // Make sure the identifier matches the specified one
            if (motd.identifier.equalsIgnoreCase(identifier)) {
                // Send the first match and return
                motd.sendTo(sender);
                return;
            }
        }
    }

    /**
     * Method to send a random MOTD to a player
     *
     * @param player - The {@link Player} who will receive the MOTD
     */
    public static void sendRandom(Player player) {
        // The current selection for a random MOTD
        // Favors higher specificity via. condition count
        MOTD randomized = null;
        // The amount of MOTDs with the same number of conditions
        // Used for random pool selection of equally specific MOTDs by condition count
        int ties = 0;

        // Iterate over the MOTDs
        for (MOTD motd : motds) {
            // Skip this MOTD if the player doesn't meet the conditions for it
            if (!motd.meetsConditions(player)) {
                continue;
            }

            // The amount of conditions this MOTD has
            int currentConditionCount = motd.getConditionCount();

            // Check if the randomized MOTD is null or this MOTD has more conditions than the current randomized selection
            if (randomized == null || currentConditionCount > randomized.getConditionCount()) {
                // Replace the current randomized selection
                randomized = motd;
                // Reset the tie counter
                ties = 1;
            // This MOTD has the same number of conditions as the randomized selection, increment the ties and pick a random entry
            } else if (currentConditionCount == randomized.getConditionCount() && ThreadLocalRandom.current().nextInt(++ties) == 0) {
                // Update the randomized MOTD with this MOTD
                randomized = motd;
            }
        }

        // Make sure there's an MOTD to send
        if (randomized != null) {
            // Send it to the player
            randomized.sendTo(player);
        }
    }
}