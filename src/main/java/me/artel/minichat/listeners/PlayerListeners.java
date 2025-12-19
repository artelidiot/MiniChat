package me.artel.minichat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.artel.minichat.checks.impl.DelayCheck;
import me.artel.minichat.checks.impl.MovementCheck;
import me.artel.minichat.checks.impl.SimilarityCheck;
import me.artel.minichat.checks.impl.UppercaseCheck;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.logic.Formatter;
import me.artel.minichat.logic.MOTD;
import me.artel.minichat.logic.Rule;

public class PlayerListeners implements Listener {

    @EventHandler // No coyotes were harmed in the making of this listener
    public void onAnvilPrepare(PrepareAnvilEvent e) {
        // TODO: Apparently all #getRenameText() methods are being deprecated?
        String renameText = e.getView().getRenameText();

        // We don't need to do anything if the rename text is blank
        if (renameText == null || renameText.isBlank()) {
            return;
        }

        for (Rule rule : Rule.rules()) {
            // Check if this rule is being checked against anvils and if it matches
            if (rule.checkAnvils() && rule.matcher((Player) e.getViewers().getFirst(), renameText)) {
                // Prevent the item from being created if the rule is being cancelled or replaced
                // TODO: Allow replacements here?
                if (rule.cancel() || rule.replace()) {
                    e.setResult(null);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerEditBook(PlayerEditBookEvent e) {
        // TODO: Figure out how to efficiently modify books with Paper's API
        e.getPlayer().updateInventory();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent e) {
        if (DelayCheck.chat(e.getPlayer())) {
            DelayCheck.handle(e.getPlayer(), e);
        }

        if (MovementCheck.chat(e.getPlayer())) {
            MovementCheck.handle(e.getPlayer(), e);
        }

        // TODO: Parrot

        if (SimilarityCheck.chat(e.getPlayer(), e.message())) {
            SimilarityCheck.handle(e.getPlayer(), e);
        }

        if (UppercaseCheck.chat(e.getPlayer(), e.message())) {
            UppercaseCheck.handle(e.getPlayer(), e.message(), e);
        }

        for (Rule rule : Rule.rules()) {
            // Check if this rule is being checked against chat
            if (rule.checkChat()) {
                // Handle violations for the rule
                e.message(rule.catcher(e.getPlayer(), e.message(), e));
            }
        }

        // TODO: Find a workaround for the message showing up as "modified" when replacements are run by rules on messages
        // Render the message as viewer unaware as we don't need to show each user a unique format
        if (FileAccessor.FORMAT_ENABLED) {
            e.renderer(ChatRenderer.viewerUnaware((player, playerDisplayName, message) -> Formatter.get(player, message)));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (DelayCheck.command(e.getPlayer())) {
            DelayCheck.handle(e.getPlayer(), e);
        }

        if (MovementCheck.command(e.getPlayer())) {
            MovementCheck.handle(e.getPlayer(), e);
        }

        if (SimilarityCheck.command(e.getPlayer(), e.getMessage())) {
            SimilarityCheck.handle(e.getPlayer(), e);
        }

        if (UppercaseCheck.command(e.getPlayer(), e.getMessage())) {
            UppercaseCheck.handle(e.getPlayer(), e.getMessage(), e);
        }

        for (Rule rule : Rule.rules()) {
            // Check if this rule is being checked against commands
            if (rule.checkCommands()) {
                // Handle violations for the rule
                e.setMessage(rule.catcher(e.getPlayer(), e.getMessage(), e));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e) {
        // TODO
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (FileAccessor.MOTD_ENABLED) {
            MOTD.sendRandom(e.getPlayer());
        }
    }
}