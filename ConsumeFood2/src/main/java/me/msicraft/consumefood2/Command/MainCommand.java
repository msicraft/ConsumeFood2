package me.msicraft.consumefood2.Command;

import me.msicraft.API.Food.CustomFood;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.Utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {

    private final ConsumeFood2 plugin;

    public MainCommand(ConsumeFood2 plugin) {
        this.plugin = plugin;
    }

    private void sendMessage(CommandSender sender, String messagePath) {
        String message = MessageUtil.getMessages(messagePath, true);
        if (message!= null && !message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("consumefood2")) {
            String var = args[0];
            if (var != null) {
                switch (var) {
                    case "reload" -> { //consumefood2 reload
                        if (!sender.hasPermission("consumefood2.command.reload")) {
                            sendMessage(sender, "Permission-Error");
                            return false;
                        }
                        plugin.reloadVariables();
                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "Plugin config files reloaded");
                        return true;
                    }
                    case "customfood" -> { //consumefood2 customfood <give> <internalName> <amount> <targetPlayer>
                        String var2 = args[1];
                        if (var2 != null) {
                            switch (var2) {
                                case "give" -> {
                                    if (!sender.hasPermission("consumefood2.command.customfood.give")) {
                                        sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    try {
                                        String internalName = args[2];
                                        int amount = Integer.parseInt(args[3]);
                                        String targetS = args[4];
                                        Player target = Bukkit.getPlayer(targetS);
                                        if (target == null) {
                                            if (sender instanceof Player p) {
                                                target = p;
                                            } else {
                                                sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "Target not found!");
                                                return false;
                                            }
                                        }
                                        CustomFood customFood = plugin.getCustomFoodManager().getCustomFood(internalName);
                                        if (customFood == null) {
                                            sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "Internalname does not exist");
                                            return false;
                                        }
                                        for (int i = 0; i<amount; i++) {
                                            target.getInventory().addItem(plugin.getCustomFoodManager().createItemStack(customFood));
                                        }
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 customfood give <internalname> <amount> <targetPlayer>");
                                    }
                                }
                            }
                        }
                    }
                    case "hunger" -> { //consumefood2 hunger <get, set, add> <amount> <targetPlayer>
                        String var2 = args[1];
                        if (var2!= null) {
                            switch (var2) {
                                case "get" -> { //consumefood2 hunger get <targetPlayer>
                                    if (!sender.hasPermission("consumefood2.command.hunger.get")) {
                                        sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    try {
                                        String targetS = args[2];
                                        Player target = Bukkit.getPlayer(targetS);
                                        if (target == null) {
                                            if (sender instanceof Player p) {
                                                target = p;
                                            } else {
                                                sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "Target not found!");
                                                return false;
                                            }
                                        }
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Player: " + target.getName());
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "FoodLevel:" + target.getFoodLevel());
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 hunger get <targetPlayer>");
                                        return false;
                                    }
                                }
                                case "set" -> { //consumefood2 hunger set <amount> <targetPlayer>
                                    if (!sender.hasPermission("consumefood2.command.hunger.set")) {
                                        sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    try {
                                        int amount = Integer.parseInt(args[2]);
                                        String targetS = args[3];
                                        Player target = Bukkit.getPlayer(targetS);
                                        if (target == null) {
                                            if (sender instanceof Player p) {
                                                target = p;
                                            } else {
                                                sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "Target not found!");
                                                return false;
                                            }
                                        }
                                        target.setFoodLevel(amount);
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "Player: " + target.getName());
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "FoodLevel set to " + amount);
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 hunger set <amount> <targetPlayer>");
                                        return false;
                                    }
                                }
                                case "add" -> { //consumefood2 hunger add <amount> <targetPlayer>
                                    if (!sender.hasPermission("consumefood2.command.hunger.add")) {
                                        sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    try {
                                        int amount = Integer.parseInt(args[2]);
                                        String targetS = args[3];
                                        Player target = Bukkit.getPlayer(targetS);
                                        if (target == null) {
                                            if (sender instanceof Player p) {
                                                target = p;
                                            } else {
                                                sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "Target not found!");
                                                return false;
                                            }
                                        }
                                        int cal = target.getFoodLevel() + amount;
                                        if (cal > 20) {
                                            cal = 20;
                                        }
                                        target.setFoodLevel(cal);
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "Player: " + target.getName());
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "FoodLevel set to " + cal);
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 hunger add <amount> <targetPlayer>");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

}