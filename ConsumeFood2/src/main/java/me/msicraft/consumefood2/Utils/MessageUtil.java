package me.msicraft.consumefood2.Utils;

import me.msicraft.API.Common;
import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.command.CommandSender;

public class MessageUtil {

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

}
