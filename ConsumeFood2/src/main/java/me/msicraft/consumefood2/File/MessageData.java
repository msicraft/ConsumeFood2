package me.msicraft.consumefood2.File;

import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class MessageData {

    private final ConsumeFood2 plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;

    public MessageData(ConsumeFood2 plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFile == null)
            this.configFile = new File(plugin.getDataFolder(), "Message.yml");

        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);

        InputStream defaultStream = plugin.getResource("Message.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (this.dataConfig == null)
            reloadConfig();

        return this.dataConfig;
    }

    public void saveConfig() {
        if (this.dataConfig == null || this.configFile == null)
            return;
        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
        }

    }

    public void saveDefaultConfig() {
        if (this.configFile == null)
            this.configFile = new File(plugin.getDataFolder(), "Message.yml");

        if (!this.configFile.exists()) {
            plugin.saveResource("Message.yml", false);
        }
    }

}
