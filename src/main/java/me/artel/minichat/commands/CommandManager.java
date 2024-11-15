package me.artel.minichat.commands;

import com.google.common.collect.ImmutableList;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import me.artel.minichat.MiniChatPlugin;
import me.artel.minichat.commands.minichat.MiniChatCommand;

public class CommandManager {

    private static final ImmutableList<CommandAPICommand> commands = ImmutableList.of(
            MiniChatCommand.getInstance()
    );

    public static void handle(Stage stage) {
        switch (stage) {
            case LOAD -> CommandAPI.onLoad(
                    new CommandAPIBukkitConfig(MiniChatPlugin.getInstance())
                            .usePluginNamespace()
                            .silentLogs(true)
                            .verboseOutput(false)
            );
            case ENABLE -> {
                CommandAPI.onEnable();
                commands.forEach(CommandAPICommand::register);
            }
            case DISABLE -> commands.forEach(command -> CommandAPI.unregister(command.getName()));
        }
    }

    public enum Stage {
        LOAD, ENABLE, DISABLE
    }
}