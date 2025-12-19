package me.artel.minichat.commands.minichat.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import me.artel.minichat.util.MiniParser;
import me.artel.minichat.MiniChatPlugin;
import me.artel.minichat.files.FileAccessor;

public class ReloadSubCommand {

    @Getter
    private static final LiteralArgumentBuilder<CommandSourceStack>
        command = Commands.literal("reload")
            .requires(sender -> sender.getSender().hasPermission(FileAccessor.PERMISSION_COMMAND_RELOAD))
            .executes(ctx -> {

                try {
                    MiniChatPlugin.reload();
                    ctx.getSource().getSender().sendMessage(MiniParser.parsePlugin(FileAccessor.LOCALE_COMMAND_RELOAD_SUCCESSFUL));
                } catch (Exception e) {
                    // TODO: Write more descriptive exception handlers
                    e.printStackTrace();
                    ctx.getSource().getSender().sendMessage(MiniParser.parsePlugin(FileAccessor.LOCALE_COMMAND_RELOAD_UNSUCCESSFUL));
                }

                return Command.SINGLE_SUCCESS;
            });
}