package me.artel.minichat.commands.minichat.subcommands;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.logic.MOTD;

public class MOTDSubCommand {

    @Getter
    private static final LiteralArgumentBuilder<CommandSourceStack>
        command = Commands.literal("motd")
            .requires(sender -> sender.getSender().hasPermission(FileAccessor.PERMISSION_COMMAND_MOTD))
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    MOTD.send(player);
                } else {
                    MOTD.send(ctx.getSource().getSender());
                }

                return Command.SINGLE_SUCCESS;
            })
                .then(Commands.argument("identifier", StringArgumentType.word())
                    .executes(ctx -> {
                        var identifier = ctx.getArgument("identifier", String.class);

                        if (ctx.getSource().getSender() instanceof Player player) {
                            MOTD.send(player, identifier);
                        } else {
                            MOTD.send(ctx.getSource().getSender(), identifier);
                        }

                        return Command.SINGLE_SUCCESS;
                    })
                );
}