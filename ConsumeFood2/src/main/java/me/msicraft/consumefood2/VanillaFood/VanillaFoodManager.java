package me.msicraft.consumefood2.VanillaFood;

import me.clip.placeholderapi.PlaceholderAPI;
import me.msicraft.API.CoolDownType;
import me.msicraft.API.Food.Food;
import me.msicraft.API.Food.FoodCommand;
import me.msicraft.API.Food.FoodPotionEffect;
import me.msicraft.API.VanillaFood;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.VanillaFood.File.VanillaFoodData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.regex.PatternSyntaxException;

public class VanillaFoodManager {

    private final ConsumeFood2 plugin;
    private final VanillaFoodData vanillaFoodData;
    private CoolDownType coolDownType = CoolDownType.DISABLE;
    private double globalCoolDown = 0;

    public VanillaFoodManager(ConsumeFood2 plugin) {
        this.plugin = plugin;
        this.vanillaFoodData = new VanillaFoodData(plugin);
    }

    public VanillaFoodData getVanillaFoodData() {
        return vanillaFoodData;
    }

    private final Map<Material, VanillaFood> vanillaFoodMap = new HashMap<>();

    public void reloadVariables() {
        saveVanillaFoodData();
        loadVanillaFood();

        String cooldownTypeS = plugin.getConfig().getString("VanillaFood-Settings.Cooldown-Setting.Type");
        if (cooldownTypeS != null) {
            try {
                this.coolDownType = CoolDownType.valueOf(cooldownTypeS.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.coolDownType = CoolDownType.DISABLE;
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid CoolDown Type=====");
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid: " + cooldownTypeS);
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Default value of 'disable' is used");
            }
        }
        this.globalCoolDown = plugin.getConfig().getDouble("VanillaFood-Settings.Cooldown-Setting.Global-Cooldown", 0);
    }

    public void loadVanillaFood() {
        ConfigurationSection section = vanillaFoodData.getConfig().getConfigurationSection("Food");
        if (section != null) {
            int count = 0;
            FileConfiguration config = vanillaFoodData.getConfig();
            Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                try {
                    VanillaFood.Type vanillaFoodType = VanillaFood.Type.valueOf(key.toUpperCase());
                    Material material = Material.getMaterial(key.toUpperCase());
                    VanillaFood vanillaFood = vanillaFoodMap.getOrDefault(material, new VanillaFood(material));
                    String path = "Food." + key;
                    vanillaFood.addOption(Food.Options.FOOD_LEVEL, config.getInt(path + ".FoodLevel", vanillaFoodType.getBaseFoodLevel()));
                    vanillaFood.addOption(Food.Options.SATURATION, (float) config.getDouble(path + ".Saturation", vanillaFoodType.getBaseSaturation()));
                    vanillaFood.addOption(Food.Options.COOLDOWN, config.getDouble(path + ".CoolDown", (Double) Food.Options.COOLDOWN.getBaseValue()));
                    vanillaFood.addOption(Food.Options.INSTANT_EAT, config.getBoolean(path + ".InstantEat", (Boolean) Food.Options.INSTANT_EAT.getBaseValue()));

                    List<String> potionEffectList = config.getStringList(path + ".PotionEffect");
                    potionEffectList.forEach(format -> {
                        try {
                            String[] split = format.split(":");
                            PotionEffectType potionEffectType = PotionEffectType.getByName(split[0].toUpperCase());
                            if (potionEffectType != null) {
                                int level = Integer.parseInt(split[1]);
                                int duration = Integer.parseInt(split[2]);
                                double chance = Double.parseDouble(split[3]);
                                PotionEffect potionEffect = new PotionEffect(potionEffectType, duration, level);
                                FoodPotionEffect foodPotionEffect = new FoodPotionEffect(potionEffect, chance);
                                vanillaFood.addPotionEffect(foodPotionEffect);
                            } else {
                                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Unknown PotionEffectType=====");
                                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "VanillaFood: " + key);
                                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "PotionEffectType: " + split[0]);
                            }
                        } catch (NullPointerException | PatternSyntaxException | ArrayIndexOutOfBoundsException e) {
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid PotionEffect Format=====");
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "VanillaFood: " + key);
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid line: " + format);
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <potionType>:<level>:<duration>:<chance>");
                        }
                    });

                    List<String> commandList = config.getStringList(path + ".Command");
                    commandList.forEach(format -> {
                        try {
                            String[] split = format.split(":");
                            FoodCommand.ExecuteType executeType = FoodCommand.ExecuteType.valueOf(split[0].toUpperCase());
                            String command = split[1];
                            FoodCommand foodCommand = new FoodCommand(command, executeType);
                            vanillaFood.addCommand(foodCommand);
                        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid Command Format=====");
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "VanillaFood: " + key);
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid line: " + format);
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <command>:<args>");
                        }
                    });

                    vanillaFoodMap.put(material, vanillaFood);
                    count++;
                } catch (IllegalArgumentException e) {
                    Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "(VanillaFood) Invalid Material: " + key);
                }
            }
            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + count + " VanillaFood loaded");
        }
    }

    public void saveVanillaFoodData() {
        int count = 0;
        FileConfiguration config = vanillaFoodData.getConfig();
        for (Material material : vanillaFoodMap.keySet()) {
            String path = "Food." + material.name().toUpperCase();
            VanillaFood vanillaFood = vanillaFoodMap.get(material);

            config.set(path + "FoodLevel", vanillaFood.getOptionValue(Food.Options.FOOD_LEVEL));
            config.set(path + "Saturation", vanillaFood.getOptionValue(Food.Options.SATURATION));
            config.set(path + "CoolDown", vanillaFood.getOptionValue(Food.Options.COOLDOWN));
            config.set(path + "InstantEat", vanillaFood.getOptionValue(Food.Options.INSTANT_EAT));

            List<String> potionEffectList = new ArrayList<>();
            vanillaFood.getPotionEffects().forEach(potionEffect -> potionEffectList.add(potionEffect.toFormat()));
            config.set(path + ".PotionEffect", potionEffectList);

            List<String> commandList = new ArrayList<>();
            vanillaFood.getCommands().forEach(command -> commandList.add(command.toFormat()));
            config.set(path + ".Command", commandList);
            count++;
        }
        vanillaFoodData.saveConfig();
        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + count + " VanillaFood saved");
    }

    public VanillaFood getVanillaFood(Material material) {
        return vanillaFoodMap.getOrDefault(material, null);
    }

    public boolean isVanillaFood(ItemStack itemStack) {
        if (itemStack != null) {
            Material material = itemStack.getType();
            if (vanillaFoodMap.containsKey(material)) {
                return !itemStack.hasItemMeta();
            }
        }
        return false;
    }

    public void consumeVanillaFood(Player player, VanillaFood vanillaFood, EquipmentSlot hand) {
        int foodLevel = (int) vanillaFood.getOptionValue(Food.Options.FOOD_LEVEL);
        float saturation = (float) vanillaFood.getOptionValue(Food.Options.SATURATION);

        int calFoodLevel = player.getFoodLevel() + foodLevel;
        if (calFoodLevel > 20) {
            calFoodLevel = 20;
        }
        player.setFoodLevel(calFoodLevel);

        float calSaturation = player.getSaturation() + saturation;
        if (calSaturation > calFoodLevel) {
            calSaturation = calFoodLevel;
        }
        player.setSaturation(calSaturation);

        vanillaFood.getPotionEffects().forEach(foodPotionEffect -> {
            if (Math.random() <= foodPotionEffect.getChance()) {
                player.addPotionEffect(foodPotionEffect.getPotionEffect());
            }
        });

        final boolean usePlaceHolderAPI = plugin.isUsePlaceHolderAPI();
        vanillaFood.getCommands().forEach(foodCommand -> {
            String command = foodCommand.getCommand();
            if (usePlaceHolderAPI) {
                command = PlaceholderAPI.setPlaceholders(player, command);
            } else {
                command = command.replaceAll("%player_name%", player.getName());
            }
            FoodCommand.ExecuteType executeType = foodCommand.getExecuteType();
            switch (executeType) {
                case PLAYER:
                    Bukkit.dispatchCommand(player, command);
                    break;
                case CONSOLE:
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command);
            }
        });

        if (hand == EquipmentSlot.HAND) {
            ItemStack handStack = player.getInventory().getItemInMainHand();
            handStack.setAmount(handStack.getAmount() - 1);
        } else if (hand == EquipmentSlot.OFF_HAND) {
            ItemStack handStack = player.getInventory().getItemInOffHand();
            handStack.setAmount(handStack.getAmount() - 1);
        }
    }

    public CoolDownType getCoolDownType() {
        return coolDownType;
    }

    public double getGlobalCoolDown() {
        return globalCoolDown;
    }

}
