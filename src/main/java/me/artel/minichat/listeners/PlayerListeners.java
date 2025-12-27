package me.artel.minichat.listeners;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import me.artel.minichat.checks.MovementCheck;
import me.artel.minichat.files.FileAccessor;
import me.artel.minichat.logic.Formatter;
import me.artel.minichat.logic.MOTD;
import me.artel.minichat.logic.Check;
import me.artel.minichat.logic.Rule;
import me.artel.minichat.util.MiniParser;

public class PlayerListeners implements Listener {

    @EventHandler // No coyotes were harmed in the making of this listener
    public void onAnvilPrepare(PrepareAnvilEvent e) {
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
        var content =
            Stream.concat(
                Stream.of(e.getNewBookMeta().title()),
                e.getNewBookMeta().pages().stream()
            )
            .filter(Objects::nonNull)
            .map(MiniParser::serializeToPlainText)
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .collect(Collectors.joining(" "));

        for (Rule rule : Rule.rules()) {
            if (rule.checkBooks() && rule.matcher(e.getPlayer(), content)) {
                if (rule.cancel() || rule.replace()) {
                    e.setSigning(false);
                    e.setNewBookMeta(e.getPreviousBookMeta());
                }
            }
        }

        e.getPlayer().updateInventory();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent e) {
        for (Check check : Check.getChecks()) {
            if (check.chat(e)) {
                check.handle(e);
            }

            if (e.isCancelled()) {
                return;
            }
        }

        for (Rule rule : Rule.rules()) {
            // Check if this rule is being checked against chat
            if (rule.checkChat()) {
                // Handle violations for the rule
                e.message(rule.catcher(e.getPlayer(), e.message(), e));
            }

            // Return if a rule cancelled the event
            if (e.isCancelled()) {
                return;
            }
        }

        // TODO: Find a workaround for the message showing up as "modified" when replacements are run by rules on messages
        // Render the message as viewer unaware as we don't need to show each user a unique format
        if (FileAccessor.FORMAT_ENABLED) {
            e.renderer(ChatRenderer.viewerUnaware((player, playerDisplayName, message) -> Formatter.get(player, message)));
        }

        Check.updateChatData(e.getPlayer(), e.message());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        for (Check check : Check.getChecks()) {
            if (check.command(e)) {
                check.handle(e);
            }

            if (e.isCancelled()) {
                return;
            }
        }

        for (Rule rule : Rule.rules()) {
            // Check if this rule is being checked against commands
            if (rule.checkCommands()) {
                // Handle violations for the rule
                e.setMessage(rule.catcher(e.getPlayer(), e.getMessage(), e));
            }

            // Return if a rule cancelled the event
            if (e.isCancelled()) {
                return;
            }
        }

        Check.updateCommandData(e.getPlayer(), e.getMessage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e) {
        var content = e.lines().stream()
            .filter(Objects::nonNull)
            .map(MiniParser::serializeToPlainText)
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .collect(Collectors.joining(" "));

        for (Rule rule : Rule.rules()) {
            // Check if this rule is being checked against signs and if it matches
            if (rule.checkSigns() && rule.matcher(e.getPlayer(), content)) {
                // Prevent the sign from being created if the rule is being cancelled or replaced
                // TODO: Allow replacements here?
                if (rule.cancel() || rule.replace()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (FileAccessor.OPTIONS_MOVEMENT_REQUIRED_CHAT || FileAccessor.OPTIONS_MOVEMENT_REQUIRED_COMMAND) {
            MovementCheck.addPlayer(e.getPlayer());
        }

        if (FileAccessor.MOTD_ENABLED) {
            MOTD.sendRandom(e.getPlayer());
        }
    }
}