package me.artel.minichat.logic;

import java.util.Map;

import org.bukkit.entity.Player;

import lombok.Getter;

public class Conditional {

    @FunctionalInterface
    public interface Requirement {
        boolean test(Player player, Object object);
    }

    @Getter
    private static Map<String, Requirement> playerRequirements = Map.ofEntries(
        // TODO: X, Y, Z + Evaluations
        Map.entry("world", (p, v) -> p.getWorld().getName().equalsIgnoreCase((String) v)),
        Map.entry("dimension", (p, v) -> p.getWorld().getEnvironment().name().equalsIgnoreCase((String) v)),
        Map.entry("has-permission", (p, v) -> p.hasPermission((String) v)),
        Map.entry("is-blocking", (p, v) -> p.isBlocking() == (Boolean) v),
        Map.entry("is-climbing", (p, v) -> p.isClimbing() == (Boolean) v),
        Map.entry("is-flying", (p, v) -> p.isFlying() == (Boolean) v),
        Map.entry("is-gliding", (p, v) -> p.isGliding() == (Boolean) v),
        Map.entry("is-jumping", (p, v) -> p.isJumping() == (Boolean) v),
        Map.entry("is-riptiding", (p, v) -> p.isRiptiding() == (Boolean) v),
        Map.entry("is-sneaking", (p, v) -> p.isSneaking() == (Boolean) v),
        Map.entry("is-sprinting", (p, v) -> p.isSprinting() == (Boolean) v),
        Map.entry("is-swimming", (p, v) -> p.isSwimming() == (Boolean) v),
        Map.entry("has-played-before", (p, v) -> p.hasPlayedBefore() == (Boolean) v)
    );

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