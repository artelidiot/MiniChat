package me.artel.minichat.commands.minichat.subcommands;

import org.bukkit.Bukkit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.util.MiniParser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public class ClearSubCommand {
    private static final Component clear = Component.text("\n".repeat(128));

    @Getter
    private static final LiteralArgumentBuilder<CommandSourceStack>
        command = Commands.literal("clear")
            .requires(sender -> sender.getSender().hasPermission(FileAccessor.PERMISSION_COMMAND_CLEAR))
            .executes(ctx -> clearChat(ctx, false))
                .then(Commands.argument("silent", BoolArgumentType.bool())
                    .executes(ctx -> clearChat(ctx, ctx.getArgument("silent", boolean.class)))
                );

    private static int clearChat(CommandContext<CommandSourceStack> ctx, Boolean silent) {
        Audience.audience(
            // Iterate over online players
            Bukkit.getOnlinePlayers().stream()
                // Filter out the players who don't have permission to bypass chat clearing
                .filter(player -> !player.hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_CLEAR))
                // Make a new list out of the filtered players
                .toList()
            )
            // Clear the chat of said players
            .sendMessage(clear);

        // Broadcast the chat was cleared if it wasn't silent (or obvious enough)
        if (!silent) {
            Audience.audience(Bukkit.getServer())
                .sendMessage(MiniParser.parsePlugin(FileAccessor.LOCALE_CHAT_CLEARED));
        }

        // Let the command executor know the deed has been done
        ctx.getSource().getSender()
            .sendMessage(MiniParser.parsePlugin(FileAccessor.LOCALE_COMMAND_CLEAR_SUCCESSFUL));

        // Make Brigadier happy
        return Command.SINGLE_SUCCESS;
    }
}