package me.artel.minichat.commands.minichat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import me.artel.minichat.commands.minichat.subcommands.ClearSubCommand;
import me.artel.minichat.commands.minichat.subcommands.MOTDSubCommand;
import me.artel.minichat.commands.minichat.subcommands.ReloadSubCommand;
import me.artel.minichat.files.FileAccessor;

public class MiniChatCommand {

    private static final LiteralArgumentBuilder<CommandSourceStack>
        commandBuilder = Commands.literal("minichat")
            .requires(sender -> sender.getSender().hasPermission(FileAccessor.PERMISSION_COMMAND))
            .then(ClearSubCommand.getCommand())
            .then(MOTDSubCommand.getCommand())
            .then(ReloadSubCommand.getCommand())
        ;

    @Getter
    private static final LiteralCommandNode<CommandSourceStack> command = commandBuilder.build();
}