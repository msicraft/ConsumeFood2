package me.msicraft.consumefood2.Command;

import me.msicraft.API.Food.CustomFood;
import me.msicraft.API.Food.VanillaFood;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.CustomFood.Menu.CustomFoodEditGui;
import me.msicraft.consumefood2.Utils.MessageUtil;
import me.msicraft.consumefood2.VanillaFood.Menu.VanillaFoodEditGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MainCommand implements CommandExecutor {

    private final ConsumeFood2 plugin;

    public MainCommand(ConsumeFood2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("consumefood2")) {
            String var = args[0];
            if (var != null) {
                switch (var) {
                    case "reload" -> { //consumefood2 reload
                        if (!sender.hasPermission("consumefood2.command.reload")) {
                            MessageUtil.sendMessage(sender, "Permission-Error");
                            return false;
                        }
                        plugin.reloadVariables();
                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "Config files reloaded");
                        return true;
                    }
                    case "vanillafood" -> { //consumefood2 vanillafood <edit, give> <internalName> <amount> <targetPlayer>
                        String var2 = args[1];
                        if (var2 != null) {
                            switch (var2) {
                                case "edit" -> {
                                    if (!sender.hasPermission("consumefood2.command.vanillafood.edit")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    if (sender instanceof Player player) {
                                        plugin.getVanillaFoodManager().openVanillaFoodEditGui(VanillaFoodEditGui.Type.SELECT, player);
                                        return true;
                                    }
                                    return false;
                                }
                                case "give" -> {
                                    if (!sender.hasPermission("consumefood2.command.vanillafood.give")) {
                                        MessageUtil.sendMessage(sender, "Permission-error");
                                        return false;
                                    }
                                    try {
                                        String materialS = args[2];
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
                                        VanillaFood vanillaFood = plugin.getVanillaFoodManager().getVanillaFood(Material.getMaterial(materialS));
                                        if (vanillaFood == null) {
                                            sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "material does not exist");
                                            return false;
                                        }
                                        ItemStack vanillaFoodStack = new ItemStack(vanillaFood.getMaterial());
                                        for (int i = 0; i<amount; i++) {
                                            target.getInventory().addItem(vanillaFoodStack);
                                        }
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 vanillafood give <internalname> <amount> <targetPlayer>");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    case "customfood" -> { //consumefood2 customfood <edit, give, create, delete> <internalName> <amount> <targetPlayer>
                        String var2 = args[1];
                        if (var2 != null) {
                            switch (var2) {
                                case "edit" -> {
                                    if (!sender.hasPermission("consumefood2.command.customfood.edit")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    if (sender instanceof Player player) {
                                        plugin.getCustomFoodManager().openCustomFoodEditGui(CustomFoodEditGui.Type.SELECT, player);
                                        return true;
                                    }
                                    return false;
                                }
                                case "give" -> {
                                    if (!sender.hasPermission("consumefood2.command.customfood.give")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
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
                                        ItemStack customFoodStack = plugin.getCustomFoodManager().createItemStack(customFood);
                                        for (int i = 0; i<amount; i++) {
                                            target.getInventory().addItem(customFoodStack);
                                        }
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 customfood give <internalname> <amount> <targetPlayer>");
                                        return false;
                                    }
                                }
                                case "create" -> {
                                    if (!sender.hasPermission("consumefood2.command.customfood.create")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    try {
                                        String internalName = args[2];
                                        if (plugin.getCustomFoodManager().getAllInternalNames().contains(internalName)) {
                                            sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "This internalname already exists");
                                            return false;
                                        }
                                        CustomFood customFood = new CustomFood(Material.APPLE, internalName);
                                        String path = "Food." + internalName;
                                        plugin.getCustomFoodManager().getCustomFoodData().getConfig().set(path + ".Material", "APPLE");
                                        plugin.getCustomFoodManager().getCustomFoodData().saveConfig();
                                        plugin.getCustomFoodManager().registerCustomFood(internalName, customFood);
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "Created CustomFood");
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 customfood create <internalname>");
                                        return false;
                                    }
                                }
                                case "delete" -> {
                                    if (!sender.hasPermission("consumefood2.command.customfood.delete")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    try {
                                        String internalName = args[2];
                                        if (!plugin.getCustomFoodManager().getAllInternalNames().contains(internalName)) {
                                            sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "Internalname does not exist");
                                            return false;
                                        }
                                        String path = "Food." + internalName;
                                        plugin.getCustomFoodManager().getCustomFoodData().getConfig().set(path, null);
                                        plugin.getCustomFoodManager().getCustomFoodData().saveConfig();
                                        plugin.getCustomFoodManager().unregisterCustomFood(internalName);
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "CustomFood has been deleted");
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 customfood delete <internalname>");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    case "foodlevel" -> { //consumefood2 foodlevel <get, set, add> <amount> <targetPlayer>
                        String var2 = args[1];
                        if (var2!= null) {
                            switch (var2) {
                                case "get" -> { //consumefood2 foodlevel get <targetPlayer>
                                    if (!sender.hasPermission("consumefood2.command.foodlevel.get")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
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
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "FoodLevel: " + target.getFoodLevel());
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 foodlevel get <targetPlayer>");
                                        return false;
                                    }
                                }
                                case "set" -> { //consumefood2 foodlevel set <amount> <targetPlayer>
                                    if (!sender.hasPermission("consumefood2.command.foodlevel.set")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
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
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 foodlevel set <amount> <targetPlayer>");
                                        return false;
                                    }
                                }
                                case "add" -> { //consumefood2 foodlevel add <amount> <targetPlayer>
                                    if (!sender.hasPermission("consumefood2.command.foodlevel.add")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
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
                                        target.setFoodLevel(cal);
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "Player: " + target.getName());
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "FoodLevel set to " + cal);
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 foodlevel add <amount> <targetPlayer>");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    case "saturation" -> { //consumefood2 saturation <get, set, add> <amount> <targetPlayer>
                        String var2 = args[1];
                        if (var2!= null) {
                            switch (var2) {
                                case "get" -> { //consumefood2 saturation get <targetPlayer>
                                    if (!sender.hasPermission("consumefood2.command.saturation.get")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
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
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Saturation: " + target.getSaturation());
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 saturation get <targetPlayer>");
                                        return false;
                                    }
                                }
                                case "set" -> { //consumefood2 saturation set <amount> <targetPlayer>
                                    if (!sender.hasPermission("consumefood2.command.saturation.set")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    try {
                                        float amount = Float.parseFloat(args[2]);
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
                                        target.setSaturation(amount);
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "Player: " + target.getName());
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "Saturation set to " + amount);
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 saturation set <amount> <targetPlayer>");
                                        return false;
                                    }
                                }
                                case "add" -> { //consumefood2 saturation add <amount> <targetPlayer>
                                    if (!sender.hasPermission("consumefood2.command.saturation.add")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    try {
                                        float amount = Float.parseFloat(args[2]);
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
                                        float cal = target.getSaturation() + amount;
                                        target.setSaturation(cal);
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "Player: " + target.getName());
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.GREEN + "Saturation set to " + cal);
                                        return true;
                                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "/consumefood2 saturation add <amount> <targetPlayer>");
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