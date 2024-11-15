package me.artel.minichat.util;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import me.artel.minichat.MiniChatPlugin;

public class MiniUtil {
    private static final String objectSerializationException = """
        Failed to serialize node at path '%s' as an Object!
        We are expecting a String or String List!
        Info: %s
        """;

    /**
     * Method to return elapsed nanoseconds since the specified start time
     *
     * @param startTime - The start time in nanoseconds
     * @param timeUnit - The {@link TimeUnit} to be converted to
     * @return - Elapsed time from nanoseconds
     */
    public static long elapsedTime(long startTime, TimeUnit timeUnit) {
        // Subtract the start time from the current time
        long elapsedTime = System.nanoTime() - startTime;

        return TimeUnit.NANOSECONDS.convert(elapsedTime, timeUnit);
    }

    /**
     * Method to obtain a String from a generic Object return type from a {@link ConfigurationNode}
     *
     * @param node The node to obtain the Object from
     * @return The parsed String
     */
    public static String getStringFromNodeObject(ConfigurationNode node) {
        try {
            return MiniUtil.getStringFromObject(node.get(Object.class));
        } catch (SerializationException e) {
            MiniChatPlugin.getInstance().getLogger().warning(
                objectSerializationException.formatted(node.path(), e.getMessage())
            );
            return "ERROR";
        }
    }

    /**
     * Method to parse an Object as a String
     *
     * @param object The Object to parse
     * @return The parsed String
     */
    public static String getStringFromObject(Object object) {
        StringBuilder stringBuilder = new StringBuilder();

        // Check if the object is a list
        if (object instanceof List<?> list) {
            // Stream the contents
            list.stream()
                    // We only want the strings
                    .filter(String.class::isInstance)
                    // Append said strings
                    .forEach(stringBuilder::append);
        } else if (object instanceof String) {
            // It's a standard string, add it
            stringBuilder.append(object);
        } else {
            // Impressive!
            stringBuilder.append("ERROR");
            MiniChatPlugin.getInstance().getLogger().warning("Could not parse as String: " + object);
        }

        return stringBuilder.toString();
    }
}