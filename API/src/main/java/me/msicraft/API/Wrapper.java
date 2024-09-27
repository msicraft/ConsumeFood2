package me.msicraft.API;

import me.msicraft.API.Food.CustomFood;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface Wrapper {

    ItemStack createCustomFoodItemStack(CustomFood customFood, Map<String, NamespacedKey> namespacedKeyMap);

    default String translateColorCodes(String message) {
        return CommonAPI.getInstance().translateColorCodes(message);
    }
}
