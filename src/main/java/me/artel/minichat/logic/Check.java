package me.artel.minichat.logic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.google.common.collect.ImmutableList;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import me.artel.minichat.MiniChatPlugin;
import me.artel.minichat.checks.DelayCheck;
import me.artel.minichat.checks.MovementCheck;
import me.artel.minichat.checks.ParrotCheck;
import me.artel.minichat.checks.SimilarityCheck;
import me.artel.minichat.checks.UppercaseCheck;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.util.MiniParser;
import net.kyori.adventure.text.Component;

public abstract class Check {
    @Getter
    private static final ImmutableList<Check> checks = ImmutableList.<Check>builder()
        .add(new DelayCheck())
        .add(new MovementCheck())
        .add(new ParrotCheck())
        .add(new SimilarityCheck())
        .add(new UppercaseCheck())
        .build();

    @Getter
    public enum Action {
        CHAT("chat"), COMMAND("command");

        final String actionName;

        Action(String actionName) {
            this.actionName = actionName;
        }
    }

    public boolean chat(AsyncChatEvent e) {
        return false;
    }

    public void handle(AsyncChatEvent e) {
        return;
    }

    public boolean command(PlayerCommandPreprocessEvent e) {
        return false;
    }

    public void handle(PlayerCommandPreprocessEvent e) {
        return;
    }

    public static void updateChatData(Player player, Component chatMessage) {
        if (!player.hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_DELAY)) {
            DelayCheck.getChatDelayMap().put(player.getUniqueId(), System.nanoTime());
        }

        ParrotCheck.setLatestMessage(MiniParser.serializeToPlainText(chatMessage));
        if (FileAccessor.OPTIONS_CHAT_PARROTING_DECAY > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                MiniChatPlugin.getInstance(),
                () -> ParrotCheck.setLatestMessage(null),
                FileAccessor.OPTIONS_CHAT_PARROTING_DECAY
            );
        }

        if (!player.hasPermission(FileAccessor.PERMISSION_BYPASS_CHAT_SIMILARITY)) {
            SimilarityCheck.getChatHistoryMap().put(player.getUniqueId(), MiniParser.serializeToPlainText(chatMessage));
        }
    }

    public static void updateCommandData(Player player, String command) {
        if (!player.hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_DELAY)) {
            DelayCheck.getCommandDelayMap().put(player.getUniqueId(), System.nanoTime());
        }

        if (!player.hasPermission(FileAccessor.PERMISSION_BYPASS_COMMAND_SIMILARITY)) {
            SimilarityCheck.getCommandHistoryMap().put(player.getUniqueId(), command);
        }
    }
}