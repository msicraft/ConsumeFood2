package me.msicraft.consumefood2.CustomFood;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import me.clip.placeholderapi.PlaceholderAPI;
import me.msicraft.API.Common;
import me.msicraft.API.CoolDownType;
import me.msicraft.API.CustomEvent.CustomFoodConsumeEvent;
import me.msicraft.API.CustomException.InvalidFormat;
import me.msicraft.API.CustomException.UnknownPotionEffectType;
import me.msicraft.API.Data.CustomGui;
import me.msicraft.API.Food.*;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.CustomFood.File.CustomFoodData;
import me.msicraft.consumefood2.CustomFood.Menu.CustomFoodEditGui;
import me.msicraft.consumefood2.CustomFood.Task.AutoUpdateTask;
import me.msicraft.consumefood2.PlayerData.Data.PlayerData;
import me.msicraft.consumefood2.Utils.GuiUtil;
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

import java.util.*;

public class CustomFoodManager {

    private final ConsumeFood2 plugin;
    private final CustomFoodData customFoodData;
    private CoolDownType coolDownType = CoolDownType.DISABLE;
    private double globalCoolDown = 0;
    private boolean disablePlayerHeadPlace = true;

    private boolean autoUpdateEnabled = false;
    private int autoUpdateTicks = 20;
    private AutoUpdateTask autoUpdateTask = null;

    private final NamespacedKey customFoodKey;
    private final NamespacedKey unStackableKey;

    public CustomFoodManager(ConsumeFood2 plugin) {
        this.plugin = plugin;
        this.customFoodData = new CustomFoodData(plugin);

        this.customFoodKey = new NamespacedKey(plugin, "CustomFood");
        this.unStackableKey = new NamespacedKey(plugin, "UnStackable");
    }

    private final Map<String, CustomFood> customFoodMap = new HashMap<>();
    private final List<String> internalNames = new ArrayList<>();

    public void reloadVariables() {
        customFoodData.reloadConfig();

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

        this.autoUpdateEnabled = plugin.getConfig().contains("CustomFood-Settings.AutoUpdateInventory.Enabled") && plugin.getConfig().getBoolean("CustomFood-Settings.AutoUpdateInventory.Enabled");
        this.autoUpdateTicks = plugin.getConfig().contains("CustomFood-Settings.AutoUpdateInventory.UpdateTicks") ? plugin.getConfig().getInt("CustomFood-Settings.AutoUpdateInventory.UpdateTicks") : 20;

        loadCustomFood();

        if (autoUpdateTask != null) {
            autoUpdateTask.cancel();
            autoUpdateTask = null;
        }
        if (autoUpdateEnabled) {
            autoUpdateTask = new AutoUpdateTask(this);
            autoUpdateTask.runTaskTimer(plugin, 0, autoUpdateTicks);
        }
    }

    public void openCustomFoodEditGui(CustomFoodEditGui.Type type, Player player) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        CustomFoodEditGui customFoodEditGui = (CustomFoodEditGui) playerData.getCustomGui(CustomGui.GuiType.CUSTOM_FOOD);
        player.openInventory(customFoodEditGui.getInventory());
        customFoodEditGui.setGui(type, player);
    }

    public void saveOptionToConfig(CustomFood customFood, Food.Options options) {
        FileConfiguration config = customFoodData.getConfig();
        String path = "Food." + customFood.getInternalName() + "." + options.getPath();
        if (options == Food.Options.LORE || options == Food.Options.POTION_EFFECT || options == Food.Options.COMMAND
                || options == Food.Options.ENCHANT || options == Food.Options.MATERIAL) {
            switch (options) {
                case MATERIAL -> {
                    config.set(path, customFood.getMaterial().name().toUpperCase());
                }
                case LORE -> {
                    config.set(path, customFood.getLore());
                }
                case POTION_EFFECT -> {
                    List<String> potionEffectList = new ArrayList<>();
                    customFood.getPotionEffects().forEach(foodPotionEffect -> {
                        potionEffectList.add(foodPotionEffect.toFormat());
                    });
                    if (potionEffectList.isEmpty()) {
                        config.set(path, null);
                    } else {
                        config.set(path, potionEffectList);
                    }
                }
                case COMMAND -> {
                    List<String> commandList = new ArrayList<>();
                    customFood.getCommands().forEach(command -> commandList.add(command.toFormat()));
                    if (commandList.isEmpty()) {
                        config.set(path, null);
                    } else {
                        config.set(path, commandList);
                    }
                }
                case ENCHANT -> {
                    List<String> enchantFormatList = customFood.getEnchantFormatList();
                    if (enchantFormatList.isEmpty()) {
                        config.set(path, null);
                    } else {
                        config.set(path, enchantFormatList);
                    }
                }
            }
        } else {
            config.set(path, customFood.getOptionValue(options));
        }
        customFoodData.saveConfig();
    }

    public void loadCustomFood() {
        FileConfiguration config = customFoodData.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Food");
        int count = 0;
        if (section != null) {
            this.internalNames.clear();
            Set<String> internalNames = section.getKeys(false);
            Food.Options[] foodOptions = Food.Options.values();
            for (String internalName : internalNames) {
                String path = "Food." + internalName;
                String materialName = config.getString(path + ".Material");
                if (materialName == null) {
                    materialName = "APPLE";
                    MessageUtil.sendErrorMessage(MessageUtil.FoodType.CUSTOMFOOD, "Material does not exist. The default APPLE is used", internalName);
                }
                Material material = Material.getMaterial(materialName.toUpperCase());
                if (material == null) {
                    material = Material.APPLE;
                    MessageUtil.sendErrorMessage(MessageUtil.FoodType.CUSTOMFOOD, "Invalid Material", internalName);
                }
                CustomFood customFood;
                if (customFoodMap.containsKey(internalName)) {
                    customFood = customFoodMap.get(internalName);
                } else {
                    customFood = new CustomFood(material, internalName);
                }
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
                for (Food.Options option : foodOptions) {
                    if (option == Food.Options.MATERIAL || option == Food.Options.TEXTURE_VALUE
                            || option == Food.Options.LORE || option == Food.Options.ENCHANT
                            || option == Food.Options.UUID || option == Food.Options.POTION_EFFECT || option == Food.Options.COMMAND) {
                        continue;
                    }
                    String p = path + "." + option.getPath();
                    if (config.contains(p)) {
                        Food.ValueType valueType = option.getValueType();
                        switch (valueType) {
                            case STRING -> {
                                customFood.setOption(option, config.getString(p, (String) option.getBaseValue()));
                            }
                            case INTEGER -> {
                                customFood.setOption(option, config.getInt(p, (int) option.getBaseValue()));
                            }
                            case DOUBLE -> {
                                customFood.setOption(option, config.getDouble(p, (double) option.getBaseValue()));
                            }
                            case BOOLEAN -> {
                                customFood.setOption(option, config.getBoolean(p, (boolean) option.getBaseValue()));
                            }
                        }
                    }
                }

                customFood.clearLore();
                List<String> lore = config.getStringList(path + ".Lore");
                for (String l : lore) {
                    customFood.addLore(l);
                }

                customFood.clearEnchant();
                List<String> enchantFormatList = config.getStringList(path + ".Enchant");
                if (!enchantFormatList.isEmpty()) {
                    enchantFormatList.forEach(format -> {
                        try {
                            String[] a = format.split(":");
                            String enchantS = a[0];
                            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantS));
                            if (enchantment == null) {
                                MessageUtil.sendErrorMessage(MessageUtil.FoodType.CUSTOMFOOD, "Invalid Enchantment", internalName, "Line: " + format);
                            } else {
                                int level = Integer.parseInt(a[1]);
                                customFood.addEnchantment(enchantment, level);
                            }
                        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                            MessageUtil.sendErrorMessage(MessageUtil.FoodType.CUSTOMFOOD, "Invalid Enchant Format", internalName,
                                    "Line: " + format, "Format: <enchant>:<level>");
                        }
                    });
                }

                customFood.getPotionEffects().clear();
                List<String> potionEffectList = config.getStringList(path + ".PotionEffect");
                potionEffectList.forEach(format -> {
                    try {
                        FoodPotionEffect foodPotionEffect = Common.getInstance().formatToFoodPotionEffect(format);
                        customFood.addPotionEffect(foodPotionEffect);
                    } catch (UnknownPotionEffectType | InvalidFormat e) {
                        MessageUtil.sendErrorMessage(MessageUtil.FoodType.CUSTOMFOOD, "Invalid PotionEffect", internalName,
                                "Line: " + format, "Format: <PotionEffectType>:<level>:<duration>:<chance>");
                        //e.printStackTrace();
                    }
                });

                customFood.getCommands().clear();
                List<String> commandList = config.getStringList(path + ".Command");
                commandList.forEach(format -> {
                    try {
                        FoodCommand foodCommand = Common.getInstance().formatToFoodCommand(format);
                        customFood.addCommand(foodCommand);
                    } catch (InvalidFormat e) {
                        MessageUtil.sendErrorMessage(MessageUtil.FoodType.VANILLAFOOD, "Invalid ExecuteCommand", internalName,
                                "Line: " + format, "Format: <executeType>:<command>");
                        //e.printStackTrace();
                    }
                });

                customFoodMap.put(internalName, customFood);
                this.internalNames.add(internalName);

                count++;
            }
        }
        Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + count + " CustomFood loaded");
    }

    public void saveCustomFoodToConfig(CustomFood customFood, boolean saveConfig) {
        FileConfiguration config = customFoodData.getConfig();
        Set<Food.Options> optionsSet = customFood.getOptions();
        String path = "Food." + customFood.getInternalName();
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
        customFood.getCommands().forEach(command -> commandList.add(command.toFormat()));
        if (commandList.isEmpty()) {
            config.set(path + ".Command", null);
        } else {
            config.set(path + ".Command", commandList);
        }
        if (saveConfig) {
            customFoodData.saveConfig();
        }
    }

    public void saveCustomFood() {
        int count = 0;
        Set<String> internalNames = customFoodMap.keySet();
        for (String internalName : internalNames) {
            CustomFood customFood = customFoodMap.get(internalName);
            saveCustomFoodToConfig(customFood, false);
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

    public boolean hasCustomFood(String internalName) {
        return customFoodMap.containsKey(internalName);
    }

    public CustomFood getCustomFood(String internalName) {
        return customFoodMap.getOrDefault(internalName, null);
    }

    public void registerCustomFood(CustomFood customFood) {
        String internalName = customFood.getInternalName();
        customFoodMap.put(internalName, customFood);
        this.internalNames.add(internalName);
    }

    public void unregisterCustomFood(String internalName) {
        customFoodMap.remove(internalName);
        this.internalNames.remove(internalName);
    }

    public void createCustomFood(String internalName) {
        CustomFood customFood = new CustomFood(Material.APPLE, internalName);
        String path = "Food." + internalName;
        customFoodData.getConfig().set(path + ".Material", "APPLE");
        customFoodData.saveConfig();
        registerCustomFood(customFood);
    }

    public void deleteCustomFood(String internalName) {
        if (customFoodMap.containsKey(internalName)) {
            String path = "Food." + internalName;
            customFoodData.getConfig().set(path, null);
            customFoodData.saveConfig();
            unregisterCustomFood(internalName);
        }
    }

    public void updateInventory(Player player) {
        int max = player.getInventory().getContents().length;
        Map<Integer, ItemStack> map = new HashMap<>(max);
        for (int i = 0; i < max; i++) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (isCustomFood(itemStack)) {
                String internalName = getInternalName(itemStack);
                ItemStack updateItemStack = createItemStack(getCustomFood(internalName));
                updateItemStack.setAmount(itemStack.getAmount());
                map.put(i, updateItemStack);
            }
        }
        for (int i : map.keySet()) {
            ItemStack itemStack = map.get(i);
            player.getInventory().setItem(i, itemStack);
        }
    }

    public ItemStack createItemStack(CustomFood customFood) {
        if (customFood == null) {
            return GuiUtil.AIR_STACK;
        }
        ItemStack itemStack = new ItemStack((Material) customFood.getOptionValue(Food.Options.MATERIAL));
        if (plugin.isUseFoodComponent()) {
            return Upper_1_20_6.getInstance().createCustomFoodItemStack(ConsumeFood2.getPlugin().getBukkitVersion(), customFood, Map.of("CustomFood", customFoodKey, "UnStackable", unStackableKey));
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        if (customFood.hasOption(Food.Options.DISPLAYNAME)) {
            String displayName = (String) customFood.getOptionValue(Food.Options.DISPLAYNAME);
            if (displayName != null) {
                displayName = Common.getInstance().translateColorCodes(displayName);
                itemMeta.setDisplayName(displayName);
            }
        }
        if (customFood.hasOption(Food.Options.CUSTOM_MODEL_DATA)) {
            itemMeta.setCustomModelData((int) customFood.getOptionValue(Food.Options.CUSTOM_MODEL_DATA));
        }
        List<String> lore = new ArrayList<>(customFood.getLore().size());
        for (String s : customFood.getLore()) {
            s = Common.getInstance().translateColorCodes(s);
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

    public void consumeCustomFood(Player player, CustomFood customFood, EquipmentSlot hand, boolean useFoodComponent) {
        Bukkit.getScheduler().runTask(plugin, ()-> {
            Bukkit.getPluginManager().callEvent(new CustomFoodConsumeEvent(true, -1,
                    player, hand, customFood));
        });
        ItemStack itemStack = null;
        if (hand == EquipmentSlot.HAND) {
            itemStack = player.getInventory().getItemInMainHand();
        } else if (hand == EquipmentSlot.OFF_HAND) {
            itemStack = player.getInventory().getItemInOffHand();
        }
        if (itemStack == null) {
            return;
        }

        applyExecuteCommands(player, customFood);
        applySound(player, customFood);
        if (useFoodComponent) {
            checkMaxConsumeCount(player, customFood, itemStack, true);
            if (ConsumeFood2.getPlugin().getBukkitVersion() >= 1212) {
                applyPotionEffects(player, customFood);
            }
            return;
        }
        applyFoodLevelAndSaturation(player, customFood);
        applyPotionEffects(player, customFood);

        Material material = (Material) customFood.getOptionValue(Food.Options.MATERIAL);
        try {
            VanillaFood.Type vanillaType = VanillaFood.Type.valueOf(material.name().toUpperCase());
            if (vanillaType.isBottle()) {
                player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
            } else if (vanillaType.isBowl()) {
                player.getInventory().addItem(new ItemStack(Material.BOWL));
            }
        } catch (IllegalArgumentException ignored) {}

        checkMaxConsumeCount(player, customFood, itemStack, false);

    }

    public void checkMaxConsumeCount(Player player, CustomFood customFood, ItemStack itemStack, boolean useComponent) {
        if (customFood.hasOption(Food.Options.MAX_CONSUME_COUNT)) {
            int maxConsumeCount = (int) customFood.getOptionValue(Food.Options.MAX_CONSUME_COUNT);
            if (maxConsumeCount == -1) {
                return;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            if (playerData == null) {
                return;
            }
            String key = customFood.getInternalName() + "_ConsumeCount";
            int consumeCount = (int) playerData.getData(key, 0);
            if (consumeCount + 1 >= maxConsumeCount) {
                // 모든 횟수 사용
                if (useComponent) {
                } else {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }
                playerData.setData(key, 0);
            } else {
                // 횟수 남음
                playerData.setData(key, consumeCount + 1);

                if (useComponent) {
                    player.getInventory().addItem(itemStack);
                }
            }
        }
    }

    public void applyFoodLevelAndSaturation(Player player, CustomFood customFood) {
        int foodLevel = (int) customFood.getOptionValue(Food.Options.FOOD_LEVEL);
        double saturationD = (double) customFood.getOptionValue(Food.Options.SATURATION);
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
    }

    public void applyPotionEffects(Player player, CustomFood customFood) {
        customFood.getPotionEffects().forEach(foodPotionEffect -> {
            if (Math.random() <= foodPotionEffect.getChance()) {
                player.addPotionEffect(foodPotionEffect.getPotionEffect());
            }
        });
    }

    public void applyExecuteCommands(Player player, CustomFood customFood) {
        boolean usePlaceHolderAPI = plugin.isUsePlaceHolderAPI();
        customFood.getCommands().forEach(foodCommand -> {
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
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        });
    }

    public void applySound(Player player, CustomFood customFood) {
        String soundS = (String) customFood.getOptionValue(Food.Options.SOUND);
        if (soundS != null) {
            try {
                String[] a = soundS.split(":");
                String soundName = a[0].toLowerCase();
                float volume = Float.parseFloat(a[1]);
                float pitch = Float.parseFloat(a[2]);
                player.playSound(player.getLocation(), soundName, volume, pitch);
            } catch (ArrayIndexOutOfBoundsException | NullPointerException | NumberFormatException ex) {
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid Sound Format=====");
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "CustomFood: " + customFood.getInternalName());
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid line: " + soundS);
                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <sound>:<volume>:<pitch>");
            }
        }
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

    public List<String> getAllInternalNames() {
        return internalNames;
    }

}
