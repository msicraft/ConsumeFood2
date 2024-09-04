package me.msicraft.API;

import me.msicraft.API.Food.CustomFood;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Wrapper {

    ItemStack createCustomFoodItemStack(CustomFood customFood, Map<String, NamespacedKey> namespacedKeyMap);

    Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    default String translateColorCodes(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String c = message.substring(matcher.start(), matcher.end());
            message = message.replace(c, net.md_5.bungee.api.ChatColor.of(c) + "");
            matcher = HEX_PATTERN.matcher(message);
        }
        return message;
    }
}
