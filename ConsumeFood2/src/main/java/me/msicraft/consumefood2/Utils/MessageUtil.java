package me.msicraft.consumefood2.Utils;

import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    private MessageUtil() {
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String translateColorCodes(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String c = message.substring(matcher.start(), matcher.end());
            message = message.replace(c, net.md_5.bungee.api.ChatColor.of(c) + "");
            matcher = HEX_PATTERN.matcher(message);
        }
        return message;
    }

    public static String getMessages(String path, boolean applyColorCodes) {
        if (!applyColorCodes) {
            return ConsumeFood2.getPlugin().getMessageData().getConfig().getString(path, null);
        }
        String message = ConsumeFood2.getPlugin().getMessageData().getConfig().getString(path, null);
        if (message != null) {
            return translateColorCodes(message);
        }
        return null;
    }

}
