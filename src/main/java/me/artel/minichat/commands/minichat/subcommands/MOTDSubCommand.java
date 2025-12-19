package me.artel.minichat.commands.minichat.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import me.artel.minichat.files.FileAccessor;

public class MOTDSubCommand {

    @Getter
    private static final LiteralArgumentBuilder<CommandSourceStack>
        command = Commands.literal("motd")
            .requires(sender -> sender.getSender().hasPermission(FileAccessor.PERMISSION_COMMAND_MOTD))
            .executes(ctx -> {
                // TODO
                return Command.SINGLE_SUCCESS;
            });
}