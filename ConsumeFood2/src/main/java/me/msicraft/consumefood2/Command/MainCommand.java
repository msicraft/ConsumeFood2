package me.msicraft.consumefood2.Command;

import me.msicraft.API.Food.*;
import me.msicraft.consumefood.ConsumeFood;
import me.msicraft.consumefood.File.CustomFoodConfig;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.CustomFood.Menu.CustomFoodEditGui;
import me.msicraft.consumefood2.Utils.MessageUtil;
import me.msicraft.consumefood2.VanillaFood.Menu.VanillaFoodEditGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

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
                    case "migrate" -> { //consumefood2 migrate [customfood, vanillafood]
                        String var2 = args[1];
                        if (var2 != null) {
                            switch (var2) {
                                case "customfood" -> {
                                    if (!sender.hasPermission("consumefood2.command.migrate.customfood")) {
                                        MessageUtil.sendMessage(sender, "Permission-Error");
                                        return false;
                                    }
                                    if (Bukkit.getPluginManager().getPlugin("ConsumeFood") == null) {
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "ConsumeFood plugin not found");
                                        return false;
                                    }
                                    Set<CustomFood> migrateCustomFoodSet = new HashSet<>();
                                    CustomFoodConfig customFoodConfig = ConsumeFood.customFoodConfig;
                                    FileConfiguration oldConfig = customFoodConfig.getConfig();
                                    ConfigurationSection section = oldConfig.getConfigurationSection("CustomFood");
                                    if (section != null) {
                                        Food.Options[] foodOptions = Food.Options.values();
                                        Set<String> internalNames = section.getKeys(false);
                                        int count = 0;
                                        for (String internalName : internalNames) {
                                            if (plugin.getCustomFoodManager().hasCustomFood(internalName)) {
                                                sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Migration Failed=====");
                                                sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Reason: Internal name already exists");
                                                sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "InternalName: " + internalName);
                                                return false;
                                            }
                                            String path = "CustomFood." + internalName;
                                            Material material = Material.APPLE;
                                            if (oldConfig.contains(path + ".Material")) {
                                                String m = oldConfig.getString(path + ".Material");
                                                Material ma = Material.getMaterial(m.toUpperCase());
                                                if (ma == null) {
                                                    sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Migration Failed=====");
                                                    sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Reason: Invalid Material");
                                                    sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid Material: " + m.toUpperCase());
                                                    sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "InternalName: " + internalName);
                                                    return false;
                                                }
                                                material = ma;
                                            }
                                            count++;
                                            CustomFood customFood = new CustomFood(material, internalName);
                                            for (Food.Options options : foodOptions) {
                                                if (options == Food.Options.MATERIAL) {
                                                    continue;
                                                }
                                                if (options.getOldPath() == null) {
                                                    continue;
                                                }
                                                String oldPath = path + "." + options.getOldPath();
                                                if (oldConfig.contains(oldPath)) {
                                                    switch (options) {
                                                        case TEXTURE_VALUE, DISPLAYNAME, SOUND, POTION_COLOR, MAX_STACK_SIZE -> {
                                                            customFood.setOption(options, oldConfig.getString(oldPath, (String) options.getBaseValue()));
                                                        }
                                                        case CUSTOM_MODEL_DATA, FOOD_LEVEL -> {
                                                            customFood.setOption(options, oldConfig.getInt(oldPath, (int) options.getBaseValue()));
                                                        }
                                                        case SATURATION, COOLDOWN, EAT_SECONDS -> {
                                                            customFood.setOption(options, oldConfig.getDouble(oldPath, (double) options.getBaseValue()));
                                                        }
                                                        case HIDE_ENCHANT, DISABLE_CRAFTING, DISABLE_SMELTING, DISABLE_ANVIL, DISABLE_ENCHANT
                                                        , HIDE_POTION_EFFECT, HIDE_ADDITIONAL_TOOLTIP, UNSTACKABLE, INSTANT_EAT, ALWAYS_EAT -> {
                                                            customFood.setOption(options, oldConfig.getBoolean(oldPath, (boolean) options.getBaseValue()));
                                                        }
                                                        case LORE -> {
                                                            oldConfig.getStringList(oldPath).forEach(customFood::addLore);
                                                        }
                                                        case UUID -> {
                                                            if (material == Material.PLAYER_HEAD) {
                                                                String uuidS = oldConfig.getString(oldPath, UUID.randomUUID().toString());
                                                                if (uuidS == null) {
                                                                    uuidS = UUID.randomUUID().toString();
                                                                }
                                                                customFood.setOption(options, UUID.fromString(uuidS));
                                                            }
                                                        }
                                                        case POTION_EFFECT -> {
                                                            oldConfig.getStringList(oldPath).forEach(format -> {
                                                                String[] split = format.split(":");
                                                                PotionEffectType potionEffectType = PotionEffectType.getByName(split[0].toUpperCase());
                                                                if (potionEffectType != null) {
                                                                    int level = Integer.parseInt(split[1]);
                                                                    int duration = Integer.parseInt(split[2]);
                                                                    double chance = Double.parseDouble(split[3]);
                                                                    FoodPotionEffect foodPotionEffect = new FoodPotionEffect(potionEffectType, level, duration, chance);
                                                                    customFood.addPotionEffect(foodPotionEffect);
                                                                } else {
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Migrate Error: Unknown PotionEffectType=====");
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "CustomFood: " + internalName);
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "PotionEffectType: " + split[0]);
                                                                }
                                                            });
                                                        }
                                                        case COMMAND -> {
                                                            oldConfig.getStringList(oldPath).forEach(format -> {
                                                                try {
                                                                    String[] split = format.split(":");
                                                                    FoodCommand.ExecuteType executeType = FoodCommand.ExecuteType.valueOf(split[0].toUpperCase());
                                                                    String c = split[1];
                                                                    FoodCommand foodCommand = new FoodCommand(c, executeType);
                                                                    customFood.addCommand(foodCommand);
                                                                } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Migrate Error: Invalid Command Format=====");
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "CustomFood: " + internalName);
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid line: " + format);
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <executeType>:<command>");
                                                                }
                                                            });
                                                        }
                                                        case ENCHANT -> {
                                                            oldConfig.getStringList(oldPath).forEach(format -> {
                                                                try {
                                                                    String[] a = format.split(":");
                                                                    String enchantS = a[0];
                                                                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantS));
                                                                    int level = Integer.parseInt(a[1]);
                                                                    customFood.addEnchantment(enchantment, level);
                                                                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Migrate Error: Invalid Enchant format=====");
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "InternalName: " + internalName);
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid line: " + format);
                                                                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <enchant>:<level>");
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                            migrateCustomFoodSet.add(customFood);
                                        }
                                        for (CustomFood customFood : migrateCustomFoodSet) {
                                            FileConfiguration config = plugin.getCustomFoodManager().getCustomFoodData().getConfig();
                                            String path = "Food." + customFood.getInternalName();
                                            Set<Food.Options> optionsSet = customFood.getOptions();
                                            for (Food.Options options : optionsSet) {
                                                if (options == Food.Options.LORE || options == Food.Options.ENCHANT
                                                        || options == Food.Options.POTION_EFFECT || options == Food.Options.COMMAND) {
                                                    continue;
                                                }
                                                String p = path + "." + options.getPath();
                                                if (options == Food.Options.MATERIAL) {
                                                    Material material = customFood.getMaterial();
                                                    config.set(p, material.name());
                                                    continue;
                                                } else if (options == Food.Options.UUID) {
                                                    config.set(p, customFood.getOptionValue(options).toString());
                                                    continue;
                                                }
                                                config.set(p, customFood.getOptionValue(options));
                                            }

                                            List<String> lore = customFood.getLore();
                                            if (lore.isEmpty()) {
                                                config.set(path + ".Lore", null);
                                            } else {
                                                config.set(path + ".Lore", lore);
                                            }

                                            List<String> enchantFormatList = customFood.getEnchantFormatList();
                                            if (enchantFormatList.isEmpty()) {
                                                config.set(path + ".Enchant", null);
                                            } else {
                                                config.set(path + ".Enchant", enchantFormatList);
                                            }

                                            List<String> potionEffectList = new ArrayList<>();
                                            customFood.getPotionEffects().forEach(potionEffect -> potionEffectList.add(potionEffect.toFormat()));
                                            if (potionEffectList.isEmpty()) {
                                                config.set(path + ".PotionEffect", null);
                                            } else {
                                                config.set(path + ".PotionEffect", potionEffectList);
                                            }

                                            List<String> commandList = new ArrayList<>();
                                            customFood.getCommands().forEach(commandS -> commandList.add(commandS.toFormat()));
                                            if (commandList.isEmpty()) {
                                                config.set(path + ".Command", null);
                                            } else {
                                                config.set(path + ".Command", commandList);
                                            }
                                            plugin.getCustomFoodManager().registerCustomFood(customFood);
                                        }
                                        plugin.getCustomFoodManager().getCustomFoodData().saveConfig();
                                        sender.sendMessage(ConsumeFood2.PREFIX + ChatColor.AQUA + count + ChatColor.GREEN + " customfoods were successfully migrated");
                                        return true;
                                    }
                                }
                            }
                        }
                        return false;
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
                                        plugin.getCustomFoodManager().registerCustomFood(customFood);
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