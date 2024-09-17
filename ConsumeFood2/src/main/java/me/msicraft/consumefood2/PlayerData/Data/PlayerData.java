package me.msicraft.consumefood2.PlayerData.Data;

import me.msicraft.API.Data.CustomGui;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.CustomFood.Menu.CustomFoodEditGui;
import me.msicraft.consumefood2.PlayerData.File.PlayerDataFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerData {

    private final Player player;
    private final PlayerDataFile playerDataFile;

    private final Map<CustomGui.GuiType, CustomGui> customGuiMap = new HashMap<>();

    private final Map<String, Object> tempDataMap = new HashMap<>();
    private final Set<String> tagSet = new HashSet<>();

    public PlayerData(Player player) {
        this.player = player;
        this.playerDataFile = new PlayerDataFile(player);
    }

    public void loadData() {
    }

    public void saveData() {
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

}
