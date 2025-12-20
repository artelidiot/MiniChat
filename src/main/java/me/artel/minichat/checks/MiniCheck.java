package me.artel.minichat.checks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.Getter;
import me.artel.minichat.MiniChatPlugin;
import me.artel.minichat.checks.impl.DelayCheck;
import me.artel.minichat.checks.impl.ParrotCheck;
import me.artel.minichat.checks.impl.SimilarityCheck;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.util.MiniParser;
import net.kyori.adventure.text.Component;

public interface MiniCheck {

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

    @Getter
    enum Action {
        CHAT("chat"), COMMAND("command");

        final String actionName;

        Action(String actionName) {
            this.actionName = actionName;
        }
    }
}