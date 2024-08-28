package me.msicraft.consumefood2.PlayerData.Data;

import me.msicraft.consumefood2.PlayerData.File.PlayerDataFile;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class PlayerData {

    private final Player player;
    private final PlayerDataFile playerDataFile;

    private final Set<String> tagSet = new HashSet<>();

    public PlayerData(Player player) {
        this.player = player;
        this.playerDataFile = new PlayerDataFile(player);
    }

    public void loadData() {
    }

    public void saveData() {
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
