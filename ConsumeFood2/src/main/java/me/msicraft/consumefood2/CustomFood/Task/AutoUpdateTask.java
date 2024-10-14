package me.msicraft.consumefood2.CustomFood.Task;

import me.msicraft.consumefood2.CustomFood.CustomFoodManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoUpdateTask extends BukkitRunnable {

    private final CustomFoodManager customFoodManager;

    public AutoUpdateTask(CustomFoodManager customFoodManager) {
        this.customFoodManager = customFoodManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOnline()) {
                customFoodManager.updateInventory(player);
            }
        }
    }

}
