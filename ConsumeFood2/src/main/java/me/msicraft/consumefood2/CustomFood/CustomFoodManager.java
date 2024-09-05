package me.msicraft.consumefood2.CustomFood;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import me.clip.placeholderapi.PlaceholderAPI;
import me.msicraft.API.CoolDownType;
import me.msicraft.API.Food.*;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.CustomFood.File.CustomFoodData;
import me.msicraft.consumefood2.Utils.MessageUtil;
import me.msicraft.upper_1_20_6.Upper_1_20_6;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.regex.PatternSyntaxException;

public class CustomFoodManager {

    private final ConsumeFood2 plugin;
    private final CustomFoodData customFoodData;
    private CoolDownType coolDownType = CoolDownType.DISABLE;
    private double globalCoolDown = 0;
    private boolean disablePlayerHeadPlace = true;

    private final NamespacedKey customFoodKey;
    private final NamespacedKey unStackableKey;

    public CustomFoodManager(ConsumeFood2 plugin) {
        this.plugin = plugin;
        this.customFoodData = new CustomFoodData(plugin);

        this.customFoodKey = new NamespacedKey(plugin, "CustomFood");
        this.unStackableKey = new NamespacedKey(plugin, "UnStackable");
    }

    private final Map<String, CustomFood> customFoodMap = new HashMap<>();

    public void reloadVariables() {
        saveCustomFood();
        loadCustomFood();

        String cooldownTypeS = plugin.getConfig().getString("CustomFood-Settings.Cooldown.Type");
        if (cooldownTypeS != null) {
            try {
                this.coolDownType = CoolDownType.valueOf(cooldownTypeS.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.coolDownType = CoolDownType.DISABLE;
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "=====Invalid CoolDown Type=====");
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid: " + cooldownTypeS);
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Default value of 'disable' is used");
            }
        }
        this.globalCoolDown = plugin.getConfig().getDouble("CustomFood-Settings.Cooldown.Global-Cooldown", 0);
        this.disablePlayerHeadPlace = plugin.getConfig().getBoolean("CustomFood-Settings.PlayerHead.DisablePlace", true);
    }

    public void loadCustomFood() {
        FileConfiguration config = customFoodData.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Food");
        int count = 0;
        if (section != null) {
            Set<String> internalNames = section.getKeys(false);
            Food.Options[] foodOptions = Food.Options.values();
            for (String internalName : internalNames) {
                String path = "Food." + internalName;
                Material material = Material.getMaterial(path + ".Material");
                if (material == null) {
                    material = Material.APPLE;
                }
                CustomFood customFood = customFoodMap.getOrDefault(internalName, new CustomFood(material, internalName));
                if (material == Material.PLAYER_HEAD) {
                    String textureValue = config.getString(path + ".TextureValue");
                    if (textureValue != null) {
                        customFood.setOption(Food.Options.TEXTURE_VALUE, textureValue);
                    }
                    String uuidS = config.getString(path + ".UUID");
                    if (uuidS == null) {
                        uuidS = UUID.randomUUID().toString();
                    }
                    customFood.setOption(Food.Options.UUID, UUID.fromString(uuidS));
                }
                for (Food.Options options : foodOptions) {
                    if (options == Food.Options.MATERIAL || options == Food.Options.TEXTURE_VALUE
                            || options == Food.Options.LORE || options == Food.Options.ENCHANT
                            || options == Food.Options.UUID) {
                        continue;
                    }
                    String p = path + "." + options.getPath();
                    if (config.contains(p)) {
                        customFood.setOption(options, config.get(p, options.getBaseValue()));
                    }
                }
                List<String> lore = config.getStringList(path + ".Lore");
                for (String l : lore) {
                    customFood.addLore(l);
                }

                List<String> enchantFormatList = config.getStringList(path + ".Enchant");
                for (String format : enchantFormatList) {
                    try {
                        String[] a = format.split(":");
                        String enchantS = a[0];
                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantS));
                        int level = Integer.parseInt(a[1]);
                        customFood.addEnchantment(enchantment, level);
                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid Enchant format=====");
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "InternalName: " + internalName);
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid line: " + format);
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <enchant>:<level>");
                    }
                }

                customFood.removeAllPotionEffects();
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
                            customFood.addPotionEffect(foodPotionEffect);
                        } else {
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Unknown PotionEffectType=====");
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "CustomFood: " + internalName);
                            Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "PotionEffectType: " + split[0]);
                        }
                    } catch (NullPointerException | PatternSyntaxException | ArrayIndexOutOfBoundsException e) {
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid PotionEffect Format=====");
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "CustomFood: " + internalName);
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid line: " + format);
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <potionType>:<level>:<duration>:<chance>");
                    }
                });

                customFood.removeAllCommands();
                List<String> commandList = config.getStringList(path + ".Command");
                commandList.forEach(format -> {
                    try {
                        String[] split = format.split(":");
                        FoodCommand.ExecuteType executeType = FoodCommand.ExecuteType.valueOf(split[0].toUpperCase());
                        String command = split[1];
                        FoodCommand foodCommand = new FoodCommand(command, executeType);
                        customFood.addCommand(foodCommand);
                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid Command Format=====");
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "CustomFood: " + internalName);
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid line: " + format);
                        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <executeType>:<command>");
                    }
                });
            }
        }
        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + count + " CustomFood loaded");
    }

    public void saveCustomFood() {
        int count = 0;
        FileConfiguration config = customFoodData.getConfig();
        Set<String> internalNames = customFoodMap.keySet();
        for (String internalName : internalNames) {
            String path = "Food." + internalName;
            CustomFood customFood = customFoodMap.get(internalName);
            Set<Food.Options> optionsSet = customFood.getOptions();
            for (Food.Options options : optionsSet) {
                String p = path + "." + options.getPath();
                config.set(p, customFood.getOptionValue(options));
            }

            config.set(path + ".Lore", customFood.getLore());
            config.set(path + ".Enchant", customFood.getEnchantFormatList());

            List<String> potionEffectList = new ArrayList<>();
            customFood.getPotionEffects().forEach(potionEffect -> potionEffectList.add(potionEffect.toFormat()));
            config.set(path + ".PotionEffect", potionEffectList);

            List<String> commandList = new ArrayList<>();
            customFood.getCommands().forEach(command -> commandList.add(command.toFormat()));
            config.set(path + ".Command", commandList);
            count++;
        }
        customFoodData.saveConfig();
        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + count + " CustomFood saved");
    }

    public boolean isCustomFood(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                return dataContainer.has(customFoodKey, PersistentDataType.STRING);
            }
        }
        return false;
    }

    public String getInternalName(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                if (dataContainer.has(customFoodKey, PersistentDataType.STRING)) {
                    return dataContainer.get(customFoodKey, PersistentDataType.STRING);
                }
            }
        }
        return null;
    }

    public CustomFood getCustomFood(String internalName) {
        return customFoodMap.getOrDefault(internalName, null);
    }

    public ItemStack createItemStack(CustomFood customFood) {
        ItemStack itemStack = new ItemStack((Material) customFood.getOptionValue(Food.Options.MATERIAL));
        if (plugin.isUseFoodComponentFunction()) {
            return Upper_1_20_6.getInstance().createCustomFoodItemStack(customFood, Map.of("CustomFood", customFoodKey, "UnStackable", unStackableKey));
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        if (customFood.hasOption(Food.Options.DISPLAYNAME)) {
            String displayName = (String) customFood.getOptionValue(Food.Options.DISPLAYNAME);
            displayName = MessageUtil.translateColorCodes(displayName);
            itemMeta.setDisplayName(displayName);
        }
        if (customFood.hasOption(Food.Options.CUSTOM_MODEL_DATA)) {
            itemMeta.setCustomModelData((int) customFood.getOptionValue(Food.Options.CUSTOM_MODEL_DATA));
        }
        List<String> lore = new ArrayList<>(customFood.getLore().size());
        for (String s : customFood.getLore()) {
            s = MessageUtil.translateColorCodes(s);
            lore.add(s);
        }
        itemMeta.setLore(lore);

        if ((boolean) customFood.getOptionValue(Food.Options.HIDE_ENCHANT)) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        if ((boolean) customFood.getOptionValue(Food.Options.HIDE_POTION_EFFECT)) {
            itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }
        if ((boolean) customFood.getOptionValue(Food.Options.UNSTACKABLE)) {
            dataContainer.set(unStackableKey, PersistentDataType.STRING, UUID.randomUUID().toString());
        }

        dataContainer.set(customFoodKey, PersistentDataType.STRING, customFood.getInternalName());
        itemStack.setItemMeta(itemMeta);

        for (Enchantment enchantment : customFood.getEnchantments()) {
            int level = customFood.getEnchantmentLevel(enchantment);
            itemStack.addUnsafeEnchantment(enchantment, level);
        }

        if (itemStack.getType() == Material.PLAYER_HEAD) {
            NBT.modify(itemStack, nbt -> {
                ReadWriteNBT skullOwnerCompound = nbt.getOrCreateCompound("SkullOwner");
                skullOwnerCompound.setUUID("Id", (UUID) customFood.getOptionValue(Food.Options.UUID));
                skullOwnerCompound.getOrCreateCompound("Properties")
                        .getCompoundList("textures")
                        .addCompound()
                        .setString("Value", (String) customFood.getOptionValue(Food.Options.TEXTURE_VALUE));
            });
        } else if (itemStack.getType() == Material.GLASS_BOTTLE) {
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            String colorCode = (String) customFood.getOptionValue(Food.Options.POTION_COLOR);
            Color color;
            java.awt.Color awtColor;
            try {
                awtColor = java.awt.Color.decode(colorCode);
                color = Color.fromBGR(awtColor.getBlue(), awtColor.getGreen(), awtColor.getRed());
            } catch (IllegalArgumentException | NullPointerException e) {
                color = Color.WHITE;
            }
            potionMeta.setColor(color);
            itemStack.setItemMeta(potionMeta);
        }
        return itemStack;
    }

    public void consumeCustomFood(Player player, CustomFood customFood, EquipmentSlot hand) {
        applyFoodLevelAndSaturation(player, customFood);
        applyPotionEffects(player, customFood);
        applyExecuteCommands(player, customFood);

        Material material = (Material) customFood.getOptionValue(Food.Options.MATERIAL);
        try {
            VanillaFood.Type vanillaType = VanillaFood.Type.valueOf(material.name().toUpperCase());
            if (vanillaType.isBottle()) {
                player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
            } else if (vanillaType.isBowl()) {
                player.getInventory().addItem(new ItemStack(Material.BOWL));
            }
        } catch (IllegalArgumentException ignored) {}

        if (hand == EquipmentSlot.HAND) {
            ItemStack handStack = player.getInventory().getItemInMainHand();
            handStack.setAmount(handStack.getAmount() - 1);
        } else if (hand == EquipmentSlot.OFF_HAND) {
            ItemStack handStack = player.getInventory().getItemInOffHand();
            handStack.setAmount(handStack.getAmount() - 1);
        }
    }

    public void applyFoodLevelAndSaturation(Player player, CustomFood customFood) {
        int foodLevel = (int) customFood.getOptionValue(Food.Options.FOOD_LEVEL);
        float saturation = (float) customFood.getOptionValue(Food.Options.SATURATION);

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
    }

    public void applyPotionEffects(Player player, CustomFood customFood) {
        customFood.getPotionEffects().forEach(foodPotionEffect -> {
            if (Math.random() <= foodPotionEffect.getChance()) {
                player.addPotionEffect(foodPotionEffect.getPotionEffect());
            }
        });
    }

    public void applyExecuteCommands(Player player, CustomFood customFood) {
        final boolean usePlaceHolderAPI = plugin.isUsePlaceHolderAPI();
        customFood.getCommands().forEach(foodCommand -> {
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
    }

    public CustomFoodData getCustomFoodData() {
        return customFoodData;
    }

    public CoolDownType getCoolDownType() {
        return coolDownType;
    }

    public double getGlobalCoolDown() {
        return globalCoolDown;
    }

    public boolean isDisablePlayerHeadPlace() {
        return disablePlayerHeadPlace;
    }

    public NamespacedKey getCustomFoodKey() {
        return customFoodKey;
    }

}
