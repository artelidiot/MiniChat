package me.artel.minichat.util;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import info.debatty.java.stringsimilarity.JaroWinkler;
import me.artel.minichat.MiniChatPlugin;

public class MiniUtil {
    private static final JaroWinkler jaroWinkler = new JaroWinkler();

    private static final String stringListSerializationException = """
        Failed to serialize node at path '%s' as a String List!
        All entries must be a String, surrounded by "quotations" or 'apostrophes'!
        Info: %s
        """;
    private static final String objectSerializationException = """
        Failed to serialize node at path '%s' as an Object!
        We are expecting a String or String List!
        Info: %s
        """;

    /**
     * Method to get an instance of JaroWinkler's similarity check
     *
     * @return - An instance of JaroWinkler
     */
    public static JaroWinkler getJaroWinkler() {
        return jaroWinkler;
    }

    /**
     * Method to clamp the minimum value of an {@link Integer}
     *
     * @param value - The {@link Integer} to clamp
     * @param minimum - The minimum value for the final {@link Integer}
     * @return - The clamped value
     */
    public static int clampMin(int value, int minimum) {
        return (value < minimum) ? minimum : value;
    }

    /**
     * Method to clamp an {@link Integer} within a given range
     *
     * @param value - The {@link Integer} to clamp
     * @param minimum - The minimum value for the final {@link Integer}
     * @param maximum - The maximum value for the final {@link Integer}
     * @return - The clamped value
     */
    public static int clamp(int value, int minimum, int maximum) {
        return (value < minimum) ? minimum : (value > maximum ? maximum : value);
    }

    /**
     * Method to return elapsed time since the specified start time
     *
     * @param startTime - The start time in nanoseconds
     * @param timeUnit - The {@link TimeUnit} to convert to
     * @return - Elapsed time in the specified {@link TimeUnit}
     */
    public static long elapsedTime(long startTime, TimeUnit timeUnit) {
        // Subtract the start time from the current time
        long elapsedTime = System.nanoTime() - startTime;

        return timeUnit.convert(elapsedTime, TimeUnit.NANOSECONDS);
    }

    /**
     * Method to retrieve a List<String> from a {@link ConfigurationNode}
     *
     * @param node - The node to obtain the {@link List} from
     * @return - The List
     */
    public static List<String> getStringListFromNode(ConfigurationNode node) {
        try {
            return node.getList(String.class, List.of());
        } catch (SerializationException e) {
            MiniChatPlugin.getInstance().getLogger().warning(
                stringListSerializationException.formatted(node.path(), e.getMessage())
            );
            return List.of();
        }
    }

    /**
     * Method to obtain a String from a generic {@link Object} return type from a {@link ConfigurationNode}
     *
     * @param node - The node to obtain the {@link Object} from
     * @return - The parsed String
     */
    public static String getStringFromNodeObject(ConfigurationNode node) {
        try {
            return MiniUtil.getStringFromObject(node.get(Object.class));
        } catch (SerializationException e) {
            MiniChatPlugin.getInstance().getLogger().warning(
                objectSerializationException.formatted(node.path(), e.getMessage())
            );
            return "";
        }
    }

    /**
     * Method to parse an Object as a String
     *
     * @param object - The {@link Object} to parse
     * @return - The parsed String
     */
    public static String getStringFromObject(Object object) {
        // Return an empty String if the Object is null as this may be desired behavior
        if (object == null) {
            return "";
        }

        // This makes our job simple
        if (object instanceof String string) {
            return string;
        }

        // Convert any Iterable to a single String split by line breaks
        if (object instanceof Iterable<?> iterable) {
            return StreamSupport.stream(iterable.spliterator(), false)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.joining("\n"));
        }

        // Impressive!
        MiniChatPlugin.getInstance().getLogger().warning("Could not parse as String: " + object);
        return "";
    }
}