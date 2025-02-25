package me.msicraft.consumefood2.PlayerData.Data;

import me.msicraft.API.Data.CustomGui;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.CustomFood.Menu.CustomFoodEditGui;
import me.msicraft.consumefood2.PlayerData.File.PlayerDataFile;
import me.msicraft.consumefood2.VanillaFood.Menu.VanillaFoodEditGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerData {

    private final Player player;
    private final PlayerDataFile playerDataFile;

    private final Map<CustomGui.GuiType, CustomGui> customGuiMap = new HashMap<>();

    private final Map<String, Object> dataMap = new HashMap<>();
    private final Map<String, Object> tempDataMap = new HashMap<>();
    private final Set<String> tagSet = new HashSet<>();

    public PlayerData(Player player) {
        this.player = player;
        this.playerDataFile = new PlayerDataFile(player);
    }

    public void loadData() {
        FileConfiguration config = playerDataFile.getConfig();
        ConfigurationSection dataSection = config.getConfigurationSection("Data");
        if (dataSection != null) {
            Set<String> keys = dataSection.getKeys(false);
            for (String key : keys) {
                dataMap.put(key, dataSection.get(key, null));
            }
        }
    }

    public void saveData() {
        FileConfiguration config = playerDataFile.getConfig();
        dataMap.forEach((s, o) -> {
            config.set("Data." + s, o);
        });
        playerDataFile.saveConfig();
    }

    public CustomGui getCustomGui(CustomGui.GuiType guiType) {
        CustomGui customGui = null;
        if (customGuiMap.containsKey(guiType)) {
            customGui = customGuiMap.get(guiType);
        }
        if (customGui == null) {
            switch (guiType) {
                case CUSTOM_FOOD -> {
                    customGui = new CustomFoodEditGui(ConsumeFood2.getPlugin());
                    customGuiMap.put(guiType, customGui);
                }
                case VANILLA_FOOD -> {
                    customGui = new VanillaFoodEditGui(ConsumeFood2.getPlugin());
                    customGuiMap.put(guiType, customGui);
                }
                default -> {
                    customGui = new CustomFoodEditGui(ConsumeFood2.getPlugin());
                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid Menu Type: " + guiType.name());
                }
            }
        }
        return customGui;
    }

    public void setTempData(String key, Object object) {
        tempDataMap.put(key, object);
    }

    public Object getTempData(String key) {
        return tempDataMap.getOrDefault(key, null);
    }

    public Object getTempData(String key, Object def) {
        Object object = getTempData(key);
        if (!hasTempData(key) || object == null) {
            return def;
        }
        return object;
    }

    public boolean hasTempData(String key) {
        return tempDataMap.containsKey(key);
    }

    public void removeTempData(String key) {
        tempDataMap.remove(key);
    }

    public void addTag(String tag) {
        tagSet.add(tag);
    }

    public void removeTag(String tag) {
        tagSet.remove(tag);
    }

    public boolean hasTag(String tag) {
        return tagSet.contains(tag);
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerDataFile getPlayerDataFile() {
        return playerDataFile;
    }

    public void setData(String key, Object value) {
        dataMap.put(key, value);
    }

    public Object getData(String key, Object def) {
        if (dataMap.containsKey(key)) {
            return dataMap.get(key);
        }
        return def;
    }

}
