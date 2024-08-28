package me.msicraft.consumefood2.VanillaFood;

import me.msicraft.API.VanillaFood;
import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class VanillaFoodManager {

    private final ConsumeFood2 plugin;

    public VanillaFoodManager(ConsumeFood2 plugin) {
        this.plugin = plugin;
    }

    private final Map<Material, VanillaFood> vanillaFoodMap = new HashMap<>();

}
