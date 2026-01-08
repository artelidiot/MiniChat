package me.artel.minichat.logic;

import java.util.function.Predicate;

import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;

public class Conditional {

    @FunctionalInterface
    public interface Condition {
        Predicate<Player> fromNode(ConfigurationNode node);
    }

    @Getter
    private static final ImmutableMap<String, Condition> playerConditions = ImmutableMap.<String, Condition>builder()
        .put("has-permission", node -> {
            String expected = node.getString();

            return p -> p.hasPermission(expected);
        })
        .put("has-played-before", node -> {
            boolean expected = node.getBoolean();

            return p -> p.hasPlayedBefore() == expected;
        })
        .put("in-dimension", node -> {
            String expected = node.getString();

            return p -> p.getWorld().getEnvironment().toString().equalsIgnoreCase(expected)
                || p.getWorld().getEnvironment().name().equalsIgnoreCase(expected);
        })
        .put("in-world", node -> {
            String expected = node.getString();

            return p -> p.getWorld().getName().equalsIgnoreCase(expected);
        })
        .put("is-blocking", node -> {
            boolean expected = node.getBoolean();

            return p -> p.isBlocking() == expected;
        })
        .put("is-climbing", node -> {
            boolean expected = node.getBoolean();

            return p -> p.isClimbing() == expected;
        })
        .put("is-flying", node -> {
            boolean expected = node.getBoolean();

            return p -> p.isFlying() == expected;
        })
        .put("is-gliding", node -> {
            boolean expected = node.getBoolean();

            return p -> p.isGliding() == expected;
        })
        .put("is-jumping", node -> {
            boolean expected = node.getBoolean();

            return p -> p.isJumping() == expected;
        })
        .put("is-riptiding", node -> {
            boolean expected = node.getBoolean();

            return p -> p.isRiptiding() == expected;
        })
        .put("is-sneaking", node -> {
            boolean expected = node.getBoolean();

            return p -> p.isSneaking() == expected;
        })
        .put("is-sprinting", node -> {
            boolean expected = node.getBoolean();

            return p -> p.isSprinting() == expected;
        })
        .put("is-swimming", node -> {
            boolean expected = node.getBoolean();

            return p -> p.isSwimming() == expected;
        })
        .build();

    // TODO
    enum Evaluation {
        LESS_THAN("<"), LESS_THAN_EQUAL_TO("<="),
        EQUAL_TO("="),
        GREATER_THAN(">"), GREATER_THAN_EQUAL_TO(">=");

        @Getter
        private final String key;

        Evaluation(String key) {
            this.key = key;
        }
    }
}