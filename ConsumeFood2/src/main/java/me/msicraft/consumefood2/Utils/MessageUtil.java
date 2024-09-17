package me.msicraft.consumefood2.Utils;

import me.msicraft.API.ConsumeFood2API;
import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.command.CommandSender;

public class MessageUtil {

    private MessageUtil() {
    }

    public static String getMessages(String path, boolean applyColorCodes) {
        if (!applyColorCodes) {
            return ConsumeFood2.getPlugin().getMessageData().getConfig().getString(path, null);
        }
        String message = ConsumeFood2.getPlugin().getMessageData().getConfig().getString(path, null);
        if (message != null) {
            return ConsumeFood2API.translateColorCodes(message);
        }
        return null;
    }

    public static void sendMessage(CommandSender sender, String messagePath) {
        String message = getMessages(messagePath, true);
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

}
