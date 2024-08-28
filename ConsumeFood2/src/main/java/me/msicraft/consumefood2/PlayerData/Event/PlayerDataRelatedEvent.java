package me.msicraft.consumefood2.PlayerData.Event;

import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.PlayerData.Data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataRelatedEvent implements Listener {

    private final ConsumeFood2 plugin;

    public PlayerDataRelatedEvent(ConsumeFood2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();

        if (e.getResult() == PlayerLoginEvent.Result.ALLOWED) {
            plugin.getPlayerDataManager().registerPlayerData(player);
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);

        //playerData.loadData();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        //playerData.saveData();

        plugin.getPlayerDataManager().unregisterPlayerData(player);
    }

}
