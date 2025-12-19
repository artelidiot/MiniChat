package me.artel.minichat.files;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.SneakyThrows;
import me.artel.minichat.MiniChatPlugin;

@Getter
public class FileManager {
    private static final JavaPlugin plugin = MiniChatPlugin.getInstance();

    private static final YAMLFile
        announcementsFile = new YAMLFile(plugin, "announcements.yml"),
        formatFile = new YAMLFile(plugin, "format.yml"),
        localeFile = new YAMLFile(plugin, "locale.yml"),
        motdFile = new YAMLFile(plugin, "motd.yml"),
        optionsFile = new YAMLFile(plugin, "options.yml"),
        rulesFile = new YAMLFile(plugin, "rules.yml");

    private static final ImmutableList<YAMLFile> files = ImmutableList.<YAMLFile>builder()
        .add(announcementsFile)
        .add(formatFile)
        .add(localeFile)
        .add(motdFile)
        .add(optionsFile)
        .add(rulesFile)
        .build();

    public static void saveFiles() {
        files.forEach(YAMLFile::save);
    }

    @SneakyThrows
    public static void reloadFiles() {
        files.forEach(YAMLFile::load);
    }

    public static CommentedConfigurationNode getAnnouncements() {
        return announcementsFile.getNode();
    }

    public static CommentedConfigurationNode getFormat() {
        return formatFile.getNode();
    }

    public static CommentedConfigurationNode getLocale() {
        return localeFile.getNode();
    }

    public static CommentedConfigurationNode getMOTD() {
        return motdFile.getNode();
    }

    public static CommentedConfigurationNode getOptions() {
        return optionsFile.getNode();
    }

    public static CommentedConfigurationNode getRules() {
        return rulesFile.getNode();
    }
}