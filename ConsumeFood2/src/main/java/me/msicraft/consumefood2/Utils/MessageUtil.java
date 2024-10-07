package me.msicraft.consumefood2.Utils;

import me.msicraft.API.Common;
import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtil {

    public enum FoodType {
        VANILLAFOOD, CUSTOMFOOD
    }

    private MessageUtil() {
    }

    public static String getConfigMessage(String path, boolean applyColorCodes) {
        if (!applyColorCodes) {
            return ConsumeFood2.getPlugin().getMessageData().getConfig().getString(path, null);
        }
        String message = ConsumeFood2.getPlugin().getMessageData().getConfig().getString(path, null);
        if (message != null) {
            return Common.getInstance().translateColorCodes(message);
        }
        return null;
    }

    public static void sendMessage(CommandSender sender, String messagePath) {
        String message = getConfigMessage(messagePath, true);
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    public static void sendErrorMessage(FoodType foodType, String errorType, String invalidKey, String... extraMessage) {
        switch (foodType) {
            case VANILLAFOOD -> {
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "=====VanillaFood " + errorType + "=====");
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid Material: " + invalidKey);
                for (String em : extraMessage) {
                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + em);
                }
            }
            case CUSTOMFOOD -> {
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "=====CustomFood " + errorType + "=====");
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid InternalName: " + invalidKey);
                for (String em : extraMessage) {
                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + em);
                }
            }
        }
    }

}
