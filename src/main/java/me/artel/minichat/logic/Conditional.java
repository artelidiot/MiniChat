package me.artel.minichat.logic;

import lombok.Getter;

public class Conditional {

    enum PlayerLocation {
        X, Y, Z,
        WORLD, DIMENSION;
    }

    enum PlayerState {
        BLOCKING, CLIMBING, FLYING,
		GLIDING, JUMPING, RIPTIDING,
        SNEAKING, SPRINTING, SWIMMING
    }

    enum PlayerHistory {
        PLAYED_BEFORE
    }

    enum Evaluation {
        LESS_THAN("<"), LESS_THAN_EQUAL_TO("<="),
        EQUAL_TO("="),
        GREATER_THAN(">"), GREATER_THAN_EQUAL_TO(">=");

        @Getter
        private final String sign;

        Evaluation(String sign) {
            this.sign = sign;
        }
    }
}