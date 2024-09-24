package me.msicraft.API.CustomEvent;

import me.msicraft.API.Food.CustomFood;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class CustomFoodConsumeEvent extends ConsumeFoodEvent {

    private final CustomFood customFood;

    public CustomFoodConsumeEvent(boolean isSuccess, double leftCooldown, Player player, EquipmentSlot useSlot, CustomFood customFood) {
        super(isSuccess, leftCooldown, player, useSlot);
        this.customFood = customFood;
    }

    public CustomFood getCustomFood() {
        return customFood;
    }

}
