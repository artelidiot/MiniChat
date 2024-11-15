package me.artel.minichat.commands.minichat;

import com.google.common.collect.ImmutableList;

import dev.jorel.commandapi.CommandAPICommand;
import lombok.Getter;
import me.artel.minichat.commands.minichat.subcommands.ReloadSubCommand;
import me.artel.minichat.files.FileAccessor;

public class MiniChatCommand {

    private static final ImmutableList<CommandAPICommand> subCommands = ImmutableList.of(
            ReloadSubCommand.getInstance()
    );

    @Getter
    public static CommandAPICommand instance = new CommandAPICommand("minichat")
            .withPermission(FileAccessor.PERMISSION_COMMAND)
            .withSubcommands(subCommands.toArray(new CommandAPICommand[0]))
            .executes((sender, arguments) -> {
                sender.sendMessage("no help here");
            });
}