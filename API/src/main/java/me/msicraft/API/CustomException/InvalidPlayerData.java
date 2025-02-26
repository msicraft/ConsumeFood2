package me.msicraft.API.CustomException;

import org.bukkit.entity.Player;

public class InvalidPlayerData extends RuntimeException {

    public InvalidPlayerData(Player player) {
        super("Invalid player data -> Player: " + player.getName() + "(" + player.getUniqueId() + ")");
    }

}
