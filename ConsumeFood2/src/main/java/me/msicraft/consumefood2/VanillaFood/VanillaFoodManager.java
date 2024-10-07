package me.msicraft.consumefood2.VanillaFood;

import me.clip.placeholderapi.PlaceholderAPI;
import me.msicraft.API.Common;
import me.msicraft.API.CoolDownType;
import me.msicraft.API.CustomEvent.VanillaFoodConsumeEvent;
import me.msicraft.API.CustomException.InvalidFormat;
import me.msicraft.API.CustomException.UnknownPotionEffectType;
import me.msicraft.API.Data.CustomGui;
import me.msicraft.API.Food.Food;
import me.msicraft.API.Food.FoodCommand;
import me.msicraft.API.Food.FoodPotionEffect;
import me.msicraft.API.Food.VanillaFood;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.PlayerData.Data.PlayerData;
import me.msicraft.consumefood2.Utils.MessageUtil;
import me.msicraft.consumefood2.VanillaFood.File.VanillaFoodData;
import me.msicraft.consumefood2.VanillaFood.Menu.VanillaFoodEditGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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
    private final List<Material> vanillaFoodMaterials = new ArrayList<>();

    public void reloadVariables() {
        vanillaFoodData.reloadConfig();

        String cooldownTypeS = plugin.getConfig().getString("VanillaFood-Settings.Cooldown.Type");
        if (cooldownTypeS != null) {
            try {
                this.coolDownType = CoolDownType.valueOf(cooldownTypeS.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.coolDownType = CoolDownType.DISABLE;
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "=====Invalid VanillaFood CoolDown Type=====");
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid: " + cooldownTypeS);
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Default value of 'disable' is used");
            }
        }
        this.globalCoolDown = plugin.getConfig().getDouble("VanillaFood-Settings.Cooldown.Global-Cooldown", 0);

        loadVanillaFood();
    }

    public void openVanillaFoodEditGui(VanillaFoodEditGui.Type type, Player player) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        VanillaFoodEditGui vanillaFoodEditGui = (VanillaFoodEditGui) playerData.getCustomGui(CustomGui.GuiType.VANILLA_FOOD);
        player.openInventory(vanillaFoodEditGui.getInventory());
        vanillaFoodEditGui.setGui(type, player);
    }

    public void saveOptionsToConfig(VanillaFood vanillaFood, Food.Options options) {
        FileConfiguration config = vanillaFoodData.getConfig();
        String path = "Food." + vanillaFood.getMaterial().name().toUpperCase() + "." + options.getPath();
        config.set(path, vanillaFood.getOptionValue(options));
        vanillaFoodData.saveConfig();
    }

    public void loadVanillaFood() {
        FileConfiguration config = vanillaFoodData.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Food");
        int count = 0;
        if (section != null) {
            vanillaFoodMaterials.clear();
            Set<String> keys = section.getKeys(false);
            Food.Options[] foodOptions = Food.Options.values();
            for (String key : keys) {
                Material material = Material.getMaterial(key.toUpperCase());
                if (material == null) {
                    MessageUtil.sendErrorMessage(MessageUtil.FoodType.VANILLAFOOD, "Invalid Material", key);
                    continue;
                } else {
                    try {
                        VanillaFood.Type type = VanillaFood.Type.valueOf(material.name());
                    } catch (IllegalArgumentException e) {
                        MessageUtil.sendErrorMessage(MessageUtil.FoodType.VANILLAFOOD, "VanillaFood Type does not exist", key);
                        continue;
                    }
                }
                VanillaFood vanillaFood;
                if (vanillaFoodMap.containsKey(material)) {
                    vanillaFood = vanillaFoodMap.get(material);
                } else {
                    vanillaFood = new VanillaFood(material);
                }
                String path = "Food." + key;
                for (Food.Options option : foodOptions) {
                    if (option == Food.Options.MATERIAL) {
                        continue;
                    }
                    if (!option.isCustomFoodOption()) {
                        String p = path + "." + option.getPath();
                        if (config.contains(p)) {
                            Food.ValueType valueType = option.getValueType();
                            switch (valueType) {
                                case STRING -> {
                                    vanillaFood.setOption(option, config.getString(p, (String) option.getBaseValue()));
                                }
                                case INTEGER -> {
                                    vanillaFood.setOption(option, config.getInt(p, (int) option.getBaseValue()));
                                }
                                case DOUBLE -> {
                                    vanillaFood.setOption(option, config.getDouble(p, (double) option.getBaseValue()));
                                }
                                case BOOLEAN -> {
                                    vanillaFood.setOption(option, config.getBoolean(p, (boolean) option.getBaseValue()));
                                }
                            }
                        }
                    }
                }

                vanillaFood.getPotionEffects().clear();
                List<String> potionEffectList = config.getStringList(path + ".PotionEffect");
                potionEffectList.forEach(format -> {
                    try {
                        FoodPotionEffect foodPotionEffect = Common.getInstance().formatToFoodPotionEffect(format);
                        vanillaFood.addPotionEffect(foodPotionEffect);
                    } catch (UnknownPotionEffectType | InvalidFormat e) {
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "=====Invalid PotionEffect=====");
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "VanillaFood: " + key);
                        e.printStackTrace();
                    }
                });

                vanillaFood.getCommands().clear();
                List<String> commandList = config.getStringList(path + ".Command");
                commandList.forEach(format -> {
                    try {
                        FoodCommand foodCommand = Common.getInstance().formatToFoodCommand(format);
                        vanillaFood.addCommand(foodCommand);
                    } catch (InvalidFormat e) {
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "=====Invalid ExecuteCommand=====");
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "VanillaFood: " + key);
                        e.printStackTrace();
                    }
                });

                vanillaFoodMap.put(material, vanillaFood);
                vanillaFoodMaterials.add(material);

                count++;
            }
        }
        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + count + " VanillaFood loaded");
    }

    public void saveVanillaFoodToConfig(VanillaFood vanillaFood, boolean saveConfig) {
        FileConfiguration config = vanillaFoodData.getConfig();
        Set<Food.Options> optionsSet = vanillaFood.getOptions();
        String basePath = "Food." + vanillaFood.getMaterial().name().toUpperCase();
        for (Food.Options options : optionsSet) {
            if (options == Food.Options.MATERIAL || options == Food.Options.POTION_EFFECT || options == Food.Options.COMMAND) {
                continue;
            }
            String path = basePath + "." + options.getPath();
            config.set(path, vanillaFood.getOptionValue(options));
        }
        List<String> potionEffectList = new ArrayList<>();
        vanillaFood.getPotionEffects().forEach(potionEffect -> potionEffectList.add(potionEffect.toFormat()));
        if (potionEffectList.isEmpty()) {
            config.set(basePath + ".PotionEffect", null);
        } else {
            config.set(basePath + ".PotionEffect", potionEffectList);
        }

        List<String> commandList = new ArrayList<>();
        vanillaFood.getCommands().forEach(command -> commandList.add(command.toFormat()));
        if (commandList.isEmpty()) {
            config.set(basePath + ".Command", null);
        } else {
            config.set(basePath + ".Command", commandList);
        }
        if (saveConfig) {
            vanillaFoodData.saveConfig();
        }
    }

    public void saveVanillaFood() {
        int count = 0;
        FileConfiguration config = vanillaFoodData.getConfig();
        for (Material material : vanillaFoodMap.keySet()) {
            String path = "Food." + material.name().toUpperCase();
            VanillaFood vanillaFood = vanillaFoodMap.get(material);

            for (Food.Options options : vanillaFood.getOptions()) {
                if (options == Food.Options.MATERIAL || options == Food.Options.POTION_EFFECT || options == Food.Options.COMMAND) {
                    continue;
                }
                config.set(path + "." + options.getPath(), vanillaFood.getOptionValue(options));
            }

            List<String> potionEffectList = new ArrayList<>();
            vanillaFood.getPotionEffects().forEach(potionEffect -> potionEffectList.add(potionEffect.toFormat()));
            if (potionEffectList.isEmpty()) {
                config.set(path + ".PotionEffect", null);
            } else {
                config.set(path + ".PotionEffect", potionEffectList);
            }

            List<String> commandList = new ArrayList<>();
            vanillaFood.getCommands().forEach(command -> commandList.add(command.toFormat()));
            if (commandList.isEmpty()) {
                config.set(path + ".Command", null);
            } else {
                config.set(path + ".Command", commandList);
            }

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

    public boolean hasVanillaFood(Material material) {
        return vanillaFoodMap.containsKey(material);
    }

    public void consumeVanillaFood(Player player, VanillaFood vanillaFood, EquipmentSlot hand) {
        Bukkit.getScheduler().runTask(plugin, ()-> {
            Bukkit.getPluginManager().callEvent(new VanillaFoodConsumeEvent(true, -1,
                    player, hand, vanillaFood));
        });

        int foodLevel = (int) vanillaFood.getOptionValue(Food.Options.FOOD_LEVEL);
        double saturationD = (double) vanillaFood.getOptionValue(Food.Options.SATURATION);
        float saturation = (float) saturationD;

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
                case PLAYER -> {
                    Bukkit.dispatchCommand(player, command);
                }
                case CONSOLE -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command);
                }
            }
        });

        Material material = (Material) vanillaFood.getOptionValue(Food.Options.MATERIAL);
        VanillaFood.Type vanillaType = VanillaFood.Type.valueOf(material.name().toUpperCase());
        if (vanillaType.isBottle()) {
            player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
        } else if (vanillaType.isBowl()) {
            player.getInventory().addItem(new ItemStack(Material.BOWL));
        }

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

    public List<Material> getVanillaFoodMaterials() {
        return vanillaFoodMaterials;
    }

}
