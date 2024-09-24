package me.msicraft.API.CustomEvent;

import me.msicraft.API.Food.VanillaFood;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class VanillaFoodConsumeEvent extends ConsumeFoodEvent{

    private final VanillaFood vanillaFood;

    public VanillaFoodConsumeEvent(boolean isSuccess, double leftCooldown, Player player, EquipmentSlot useSlot, VanillaFood vanillaFood) {
        super(isSuccess, leftCooldown, player, useSlot);
        this.vanillaFood = vanillaFood;
    }

    public VanillaFood getVanillaFood() {
        return vanillaFood;
    }

}
