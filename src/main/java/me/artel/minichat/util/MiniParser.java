package me.artel.minichat.util;

import org.bukkit.entity.Player;

import me.artel.minichat.files.FileAccessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class MiniParser {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer plainText = PlainTextComponentSerializer.plainText();

    /**
     * Method to parse a String for MiniMessage syntax
     *
     * @param input - The String to parse
     * @param resolvers - Any placeholders to be parsed
     * @return - The parsed Component
     */
    public static Component deserialize(String input, TagResolver... resolvers) {
        return miniMessage.deserialize(input, resolvers);
    }

    /**
     * Method to serialize a Component into plain text
     *
     * @param input - The Component to serialize
     * @return - The plain text of the Component
     */
    public static String serializeToPlainText(Component input) {
        return plainText.serialize(input);
    }

    /**
     * Method to parse a String for MiniMessage syntax, with placeholders for a Player's username and display name
     *
     * @param input - The String to parse
     * @param resolvers - Any additional placeholders to be parsed
     * @return - The parsed Component
     */
    public static Component parsePlayer(String input, Player player, TagResolver... resolvers) {
        return deserialize(input,
                TagResolver.resolver(
                        Placeholder.component("player-name", player.name()),
                        Placeholder.component("player-display-name", player.displayName())
                ),
                TagResolver.resolver(resolvers)
        );
    }

    /**
     * Method to parse a String for MiniMessage syntax, with placeholders for the plugin's prefix and version.
     *
     * @param input - The String to parse
     * @param resolvers - Any additional placeholders to be parsed
     * @return - The parsed Component
     */
    public static Component parsePlugin(String input, TagResolver... resolvers) {
        return deserialize(input,
                TagResolver.resolver(
                        Placeholder.parsed("prefix", FileAccessor.LOCALE_PREFIX),
                        Placeholder.parsed("version", FileAccessor.LOCALE_VERSION)
                ),
                TagResolver.resolver(resolvers)
        );
    }

    /**
     * Method to parse a String for MiniMessage syntax, with placeholders for a Player's username and display name, and placeholders for the plugin's prefix and version.
     *
     * @param input - The String to parse
     * @param resolvers - Any additional placeholders to be parsed
     * @return - The parsed Component
     */
    public static Component parseAll(String input, Player player, TagResolver... resolvers) {
        return parsePlayer(input, player,
                TagResolver.resolver(
                        Placeholder.parsed("prefix", FileAccessor.LOCALE_PREFIX),
                        Placeholder.parsed("version", FileAccessor.LOCALE_VERSION)
                ),
                TagResolver.resolver(resolvers)
        );
    }
}