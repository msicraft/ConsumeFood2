package me.msicraft.consumefood2.PlayerData.File;

import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class PlayerDataFile {

    private final Player player;
    private final ConsumeFood2 plugin = ConsumeFood2.getPlugin();

    private final String folderName = "PlayerData";

    private File file;
    private FileConfiguration config;

    public PlayerDataFile(Player player) {
        this.player = player;
        String fileS = player.getUniqueId() + ".yml";
        this.file = new File(plugin.getDataFolder() + File.separator + folderName, fileS);
        if (!file.exists()) {
            createFile(player);
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public void createFile(Player player) {
        if (!this.file.exists()) {
            try {
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(this.file);
                yamlConfiguration.set("Name", player.getName());
                yamlConfiguration.save(this.file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public void reloadConfig() {
        if (this.config == null) {
            return;
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
        Reader defaultConfigStream;
        try {
            defaultConfigStream = new InputStreamReader(plugin.getResource(this.file.getName()), StandardCharsets.UTF_8);
            if (defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
                config.setDefaults(defaultConfig);
            }
        } catch (NullPointerException ex) {
            //ex.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            getConfig().save(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig(Player player) {
        String fileS = player.getUniqueId() + ".yml";
        if (this.file == null) {
            this.file = new File(plugin.getDataFolder() + File.separator + folderName, fileS);
        }
        if (!this.file.exists()) {
            plugin.saveResource(fileS, false);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public File getFile() {
        return file;
    }

}
