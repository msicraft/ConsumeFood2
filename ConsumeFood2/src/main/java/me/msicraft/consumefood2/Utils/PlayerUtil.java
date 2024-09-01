package me.msicraft.consumefood2.Utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerUtil {

    public static EquipmentSlot getUseHand(Player player, ItemStack itemStack) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (itemStack.isSimilar(handItem)) {
            return EquipmentSlot.HAND;
        }
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (itemStack.isSimilar(offHandItem)) {
            return EquipmentSlot.OFF_HAND;
        }
        return null;
    }

}
