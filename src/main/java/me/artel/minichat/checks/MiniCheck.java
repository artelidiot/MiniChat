package me.artel.minichat.checks;

import lombok.Getter;

public interface MiniCheck {

    @Getter
    enum Action {
        CHAT("chat"), COMMAND("command");

        final String actionName;

        Action(String actionName) {
            this.actionName = actionName;
        }
    }
}