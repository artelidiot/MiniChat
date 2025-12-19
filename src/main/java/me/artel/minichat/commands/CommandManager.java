package me.artel.minichat.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.artel.minichat.MiniChatPlugin;
import me.artel.minichat.commands.minichat.MiniChatCommand;

public class CommandManager {
    private static final ImmutableList<LiteralCommandNode<CommandSourceStack>> commands = ImmutableList.<LiteralCommandNode<CommandSourceStack>>builder()
        .add(MiniChatCommand.getCommand())
        .build();

    public static void handle() {
        MiniChatPlugin.getInstance().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commandLifecycle -> {
            commands.forEach(command -> commandLifecycle.registrar().register(command));
        });
    }
}