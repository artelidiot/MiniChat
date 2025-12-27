package me.artel.minichat.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
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
    private boolean enabled;
    private int delay;
    private String content;
    private List<Predicate<Player>> conditions;

    public MOTD(ConfigurationNode motdNode) {
        // We don't need to do anything at all if MOTDs are not enabled
        if (!FileAccessor.MOTD_ENABLED) {
            return;
        }

        // We don't need to do anything if the MOTD itself isn't enabled
        if (!motdNode.node("enabled").getBoolean(false)) {
            return;
        }

        // Create the identifier for the MOTD
        this.identifier = motdNode.node("id").getString("");

        // Create the MOTD
        // TODO: Add time syntax to the delay
        this.delay = MiniUtil.clampMin(motdNode.node("delay").getInt(0), 0);
        // Make the delay operate in milliseconds by dividing the value by 50
        this.delay = (delay > 0) ? (delay / 50) : 0;
        this.content = MiniUtil.getStringFromNodeObject(motdNode.node("content"));
        this.conditions = motdNode.node("conditions")
            .childrenMap().entrySet().stream()
            .flatMap(e -> {
                var conditions = Conditional.getPlayerRequirements().get(e.getKey().toString());

                return conditions == null
                    ? Stream.empty()
                    : Stream.of((Predicate<Player>) (p -> conditions.test(p, e.getValue().raw())));
            })
            .toList();

        // Add this MOTD to the list of enabled MOTDs
        motds.add(this);
    }

    /**
     * Method to remove all existing MOTDs and repopulate the list of MOTDs with up-to-date values from the MOTD file.
     */
    public static void repopulate() {
        // Clear any existing MOTDs so we don't add duplicates
        motds.clear();

        // Iterate over anything in the list and try to create a MOTD from it
        FileManager.getMOTD().node("list").childrenList().forEach(MOTD::new);
    }

    /**
     * Method to send the contents of a specified MOTD by identifier
     *
     * @param player - The {@link Player} to send the content to
     * @param identifier - The identifier of the desired {@link MOTD}
     * @apiNote - Ignores conditions of the MOTD
     */
    public static void send(Player player, String identifier) {
        motds.stream()
            .filter(motd -> motd.getIdentifier().equalsIgnoreCase(identifier))
            .findAny()
            .ifPresent(motd -> player.sendMessage(MiniParser.parseAll(motd.getContent(), player)));
    }

    /**
     * Method to send the contents of a random MOTD
     *
     * @param player - The {@link Player} to send the content to
     * @apiNote - Filters out MOTDs with unmet conditions
     */
    // TODO: Reduce streams?
    public static void sendRandom(Player player) {
        var eligible = motds.stream()
            .filter(motd -> motd.getConditions().stream().allMatch(predicate -> predicate.test(player)))
            .toList();

        if (eligible.isEmpty()) {
            return;
        }

        int maxConditions = eligible.stream()
            .mapToInt(motd -> motd.getConditions().size())
            .max()
            .orElse(0);

        var priority = eligible.stream()
            .filter(motd -> motd.getConditions().size() == maxConditions)
            .toList();

        var randomMOTD = priority.get(ThreadLocalRandom.current().nextInt(priority.size()));

        if (randomMOTD.getDelay() > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(MiniChatPlugin.getInstance(),
                () -> player.sendMessage(MiniParser.parseAll(randomMOTD.getContent(), player)),
                randomMOTD.getDelay()
            );
        } else {
            player.sendMessage(MiniParser.parseAll(randomMOTD.getContent(), player));
        }
    }
}