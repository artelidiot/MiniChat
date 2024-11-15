package me.artel.minichat.files;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class YAMLFile {
    private final JavaPlugin plugin;
    private final String fileName;
    private final File file;
    private final YamlConfigurationLoader loader;
    private CommentedConfigurationNode node;

    public YAMLFile(JavaPlugin plugin, String fileName) {
        this(plugin, new File(plugin.getDataFolder(), fileName));
    }

    @SneakyThrows
    public YAMLFile(JavaPlugin plugin, File file) {
        this.plugin = plugin;
        this.fileName = file.getName();
        this.file = file;

        save();

        this.loader = YamlConfigurationLoader.builder()
                .file(file)
                .build();

        this.node = loader.load();
    }

    public void save() {
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }

    @SneakyThrows
    public void load() {
        save();
        node = loader.load();
    }
}