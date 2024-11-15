package me.artel.minichat.commands.minichat.subcommands;

import dev.jorel.commandapi.CommandAPICommand;
import lombok.Getter;
import me.artel.minichat.MiniChatPlugin;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.util.MiniParser;

public class ReloadSubCommand {

    @Getter
    public static CommandAPICommand instance = new CommandAPICommand("reload")
            .withPermission(FileAccessor.PERMISSION_COMMAND_RELOAD)
            .withShortDescription("Reload MiniChat.")
            .executes((sender, args) -> {
                // TODO: Reload and notify
                try {
                    MiniChatPlugin.reload();
                    sender.sendMessage(MiniParser.parsePlugin(FileAccessor.LOCALE_COMMAND_RELOAD_SUCCESSFUL));
                } catch (Exception e) {
                    sender.sendMessage(MiniParser.parsePlugin(FileAccessor.LOCALE_COMMAND_RELOAD_FAILED));
                }
            });
}