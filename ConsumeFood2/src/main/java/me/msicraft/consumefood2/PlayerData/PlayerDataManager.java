package me.msicraft.consumefood2.PlayerData;

import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.PlayerData.Data.PlayerData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerDataManager {

    private final ConsumeFood2 plugin;

    public PlayerDataManager(ConsumeFood2 plugin) {
        this.plugin = plugin;
    }

    private final Map<UUID, PlayerData> registeredPlayerDataMap = new HashMap<>();

    public void saveAll() {
        registeredPlayerDataMap.forEach((uuid, playerData) -> {
            playerData.saveData();
        });
    }

    public void registerPlayerData(Player player) {
        PlayerData playerData = new PlayerData(player);
        registeredPlayerDataMap.put(player.getUniqueId(), playerData);
    }

    public void unregisterPlayerData(Player player) {
        registeredPlayerDataMap.remove(player.getUniqueId());
    }

    public PlayerData getPlayerData(Player player) {
        return registeredPlayerDataMap.getOrDefault(player.getUniqueId(), new PlayerData(player));
    }

    public Set<UUID> getUUIDSets() {
        return registeredPlayerDataMap.keySet();
    }

}
