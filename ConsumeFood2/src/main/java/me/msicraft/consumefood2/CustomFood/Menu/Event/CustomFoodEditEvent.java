package me.msicraft.consumefood2.CustomFood.Menu.Event;

import me.msicraft.API.Food.CustomFood;
import me.msicraft.API.Food.Food;
import me.msicraft.API.Food.FoodCommand;
import me.msicraft.API.Food.FoodPotionEffect;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.CustomFood.CustomFoodManager;
import me.msicraft.consumefood2.CustomFood.Menu.CustomFoodEditGui;
import me.msicraft.consumefood2.PlayerData.Data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.util.regex.PatternSyntaxException;

public class CustomFoodEditEvent implements Listener {

    private final ConsumeFood2 plugin;

    public CustomFoodEditEvent(ConsumeFood2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void customFoodGuiChatEditEvent(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        if (playerData.hasTempData("CustomFood_ChatEdit")) {
            e.setCancelled(true);
            CustomFoodManager customFoodManager = plugin.getCustomFoodManager();
            if (!playerData.hasTempData("CustomFood_Edit_Key")) {
                player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "internalName does not exist");
                playerData.removeTempData("CustomFood_ChatEdit");
                Bukkit.getScheduler().runTask(plugin, () -> {
                    customFoodManager.openCustomFoodEditGui(CustomFoodEditGui.Type.SELECT, player);
                });
                return;
            }
            String editData = (String) playerData.getTempData("CustomFood_ChatEdit");
            Food.Options options = Food.Options.valueOf(editData);
            String internalName = (String) playerData.getTempData("CustomFood_Edit_Key");
            CustomFood customFood = customFoodManager.getCustomFood(internalName);
            String message = e.getMessage();
            if (message.equals("cancel")) {
                playerData.removeTempData("CustomFood_ChatEdit");
                Bukkit.getScheduler().runTask(plugin, () -> {
                    customFoodManager.openCustomFoodEditGui(CustomFoodEditGui.Type.EDIT, player);
                });
                return;
            }
            boolean save = true;
            switch (options) {
                case MATERIAL -> {
                    Material material = Material.getMaterial(message.toUpperCase());
                    if (material == null) {
                        player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "Invalid Material: " + message);
                        save = false;
                    } else {
                        customFood.setOption(Food.Options.MATERIAL, material);
                    }
                }
                case TEXTURE_VALUE -> {
                    customFood.setOption(Food.Options.TEXTURE_VALUE, message);
                }
                case DISPLAYNAME -> {
                    customFood.setOption(Food.Options.DISPLAYNAME, message);
                }
                case CUSTOM_MODEL_DATA -> {
                    int customModelData = Integer.parseInt(message);
                    customFood.setOption(Food.Options.CUSTOM_MODEL_DATA, customModelData);
                }
                case LORE -> {
                    customFood.addLore(message);
                }
                case POTION_EFFECT -> {
                    try {
                        String[] split = message.split(":");
                        PotionEffectType potionEffectType = PotionEffectType.getByName(split[0].toUpperCase());
                        if (potionEffectType != null) {
                            int level = Integer.parseInt(split[1]);
                            int duration = Integer.parseInt(split[2]);
                            double chance = Double.parseDouble(split[3]);
                            FoodPotionEffect foodPotionEffect = new FoodPotionEffect(potionEffectType, level, duration, chance);
                            customFood.addPotionEffect(foodPotionEffect);
                        } else {
                            save = false;
                            player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Unknown PotionEffectType=====");
                            player.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "PotionEffectType: " + split[0]);
                        }
                    } catch (NullPointerException | PatternSyntaxException | ArrayIndexOutOfBoundsException ex) {
                        save = false;
                        player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid PotionEffect Format=====");
                        player.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <potionType>:<level>:<duration>:<chance>");
                    }
                }
                case COMMAND -> {
                    try {
                        String[] split = message.split(":");
                        FoodCommand.ExecuteType executeType = FoodCommand.ExecuteType.valueOf(split[0].toUpperCase());
                        String command = split[1];
                        FoodCommand foodCommand = new FoodCommand(command, executeType);
                        customFood.addCommand(foodCommand);
                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
                        save = false;
                        player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid Command Format=====");
                        player.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <executeType>:<command>");
                    }
                }
                case FOOD_LEVEL -> {
                    int foodLevel = Integer.parseInt(message);
                    customFood.setOption(Food.Options.FOOD_LEVEL, foodLevel);
                }
                case SATURATION -> {
                    double saturationD = Double.parseDouble(message);
                    customFood.setOption(Food.Options.SATURATION, saturationD);
                }
                case COOLDOWN -> {
                    double cooldown = Double.parseDouble(message);
                    customFood.setOption(Food.Options.COOLDOWN, cooldown);
                }
                case ENCHANT -> {
                    try {
                        String[] a = message.split(":");
                        String enchantS = a[0];
                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantS));
                        int level = Integer.parseInt(a[1]);
                        customFood.addEnchantment(enchantment, level);
                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                        save = false;
                        player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid Enchant format=====");
                        player.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <enchant>:<level>");
                    }
                }
                case SOUND -> {
                    customFood.setOption(Food.Options.SOUND, message);
                }
                case POTION_COLOR -> {
                    customFood.setOption(Food.Options.POTION_COLOR, message);
                }
                case EAT_SECONDS -> {
                    double d = Double.parseDouble(message);
                    customFood.setOption(Food.Options.EAT_SECONDS, d);
                }
                case MAX_STACK_SIZE -> {
                    int maxStackSize = Integer.parseInt(message);
                    customFood.setOption(Food.Options.MAX_STACK_SIZE, maxStackSize);
                }
            }
            if (save) {
                customFoodManager.saveOptionToConfig(customFood, options);
            }
            playerData.removeTempData("CustomFood_ChatEdit");
            Bukkit.getScheduler().runTask(plugin, () -> {
                customFoodManager.openCustomFoodEditGui(CustomFoodEditGui.Type.EDIT, player);
            });
        }
    }

    @EventHandler
    public void customFoodGuiEvent(InventoryClickEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder() instanceof CustomFoodEditGui customFoodEditGui) {
            ClickType type = e.getClick();
            if (type == ClickType.NUMBER_KEY || type == ClickType.SWAP_OFFHAND
                    || type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            ItemStack itemStack = e.getCurrentItem();
            if (itemStack == null) {
                return;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            CustomFoodManager customFoodManager = plugin.getCustomFoodManager();
            NamespacedKey selectKey = customFoodEditGui.getSelectKey();
            NamespacedKey editKey = customFoodEditGui.getEditKey();
            if (dataContainer.has(selectKey, PersistentDataType.STRING)) {
                String data = dataContainer.get(selectKey, PersistentDataType.STRING);
                if (data != null) {
                    int maxSize = customFoodManager.getAllInternalNames().size();
                    int current = (int) playerData.getTempData("CustomFood_Select_Page", 1);
                    switch (data) {
                        case "Next" -> {
                            int next = current + 1;
                            if (next > maxSize / 45) {
                                next = 0;
                            }
                            playerData.setTempData("CustomFood_Select_Page", next);
                            customFoodManager.openCustomFoodEditGui(CustomFoodEditGui.Type.SELECT, player);
                        }
                        case "Previous" -> {
                            int previous = current - 1;
                            if (previous < 0) {
                                previous = maxSize / 45;
                            }
                            playerData.setTempData("CustomFood_Select_Page", previous);
                            customFoodManager.openCustomFoodEditGui(CustomFoodEditGui.Type.SELECT, player);
                        }
                        case "Page" -> {}
                        default -> {
                            if (customFoodManager.hasCustomFood(data)) {
                                if (e.isLeftClick()) {
                                    playerData.setTempData("CustomFood_Edit_Key", data);
                                    customFoodManager.openCustomFoodEditGui(CustomFoodEditGui.Type.EDIT, player);
                                } else if (e.isRightClick()) {
                                    CustomFood customFood = customFoodManager.getCustomFood(data);
                                    ItemStack customFoodStack = customFoodManager.createItemStack(customFood);
                                    player.getInventory().addItem(customFoodStack);
                                }
                            } else {
                                player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "Invalid InternalName: " + data);
                            }
                        }
                    }
                }
            } else if (dataContainer.has(editKey, PersistentDataType.STRING)) {
                String data = dataContainer.get(editKey, PersistentDataType.STRING);
                if (data != null) {
                    switch (data) {
                        case "Back" -> {
                            customFoodManager.openCustomFoodEditGui(CustomFoodEditGui.Type.SELECT, player);
                            //playerData.removeTempData("CustomFood_Edit_Key");
                        }
                        case "Edit_Item" -> {
                            if (e.isRightClick()) {
                                if (!playerData.hasTempData("CustomFood_Edit_Key")) {
                                    player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "internalName does not exist");
                                    player.closeInventory();
                                    return;
                                }
                                String internalName = (String) playerData.getTempData("CustomFood_Edit_Key");
                                CustomFood customFood = customFoodManager.getCustomFood(internalName);
                                ItemStack customFoodStack = customFoodManager.createItemStack(customFood);
                                player.getInventory().addItem(customFoodStack);
                            }
                        }
                        default -> {
                            if (!playerData.hasTempData("CustomFood_Edit_Key")) {
                                player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "internalName does not exist");
                                player.closeInventory();
                                return;
                            }
                            String internalName = (String) playerData.getTempData("CustomFood_Edit_Key");
                            CustomFood customFood = customFoodManager.getCustomFood(internalName);
                            Food.Options options = Food.Options.valueOf(data);
                            boolean open = true;
                            switch (options) {
                                case MATERIAL -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the material");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.MATERIAL, Food.Options.MATERIAL.getBaseValue());
                                    }
                                }
                                case TEXTURE_VALUE -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the texture value");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.TEXTURE_VALUE, Food.Options.TEXTURE_VALUE.getBaseValue());
                                    }
                                }
                                case DISPLAYNAME -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the display name");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.DISPLAYNAME, Food.Options.DISPLAYNAME.getBaseValue());
                                    }
                                }
                                case CUSTOM_MODEL_DATA -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the custom model data");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.CUSTOM_MODEL_DATA, Food.Options.CUSTOM_MODEL_DATA.getBaseValue());
                                    }
                                }
                                case LORE -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the lore");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.clearLore();
                                    }
                                }
                                case POTION_EFFECT -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the potion effect");
                                        player.sendMessage(ChatColor.GRAY + "Format: <potionType>:<level>:<duration>:<chance>");

                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.clearPotionEffects();
                                    }
                                }
                                case COMMAND -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the command");
                                        player.sendMessage(ChatColor.GRAY + "Format: <executeType>:<command>");
                                        player.sendMessage(ChatColor.GRAY + "ExecuteType: [console, player]");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.clearCommands();
                                    }
                                }
                                case FOOD_LEVEL -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the food level");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.FOOD_LEVEL, Food.Options.FOOD_LEVEL.getBaseValue());
                                    }
                                }
                                case SATURATION -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the saturation");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.SATURATION, Food.Options.SATURATION.getBaseValue());
                                    }
                                }
                                case COOLDOWN -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the cooldown");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.COOLDOWN, Food.Options.COOLDOWN.getBaseValue());
                                    }
                                }
                                case ENCHANT -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the enchant");
                                        player.sendMessage(ChatColor.GRAY + "Format: <enchant>:<level>");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.clearEnchant();
                                    }
                                }
                                case HIDE_ENCHANT -> {
                                    boolean b = (boolean) customFood.getOptionValue(Food.Options.HIDE_ENCHANT);
                                    if (b) {
                                        customFood.setOption(Food.Options.HIDE_ENCHANT, false);
                                    } else {
                                        customFood.setOption(Food.Options.HIDE_ENCHANT, true);
                                    }
                                    customFoodManager.saveOptionToConfig(customFood, Food.Options.HIDE_ENCHANT);
                                }
                                case DISABLE_CRAFTING -> {
                                    boolean b = (boolean) customFood.getOptionValue(Food.Options.DISABLE_CRAFTING);
                                    if (b) {
                                        customFood.setOption(Food.Options.DISABLE_CRAFTING, false);
                                    } else {
                                        customFood.setOption(Food.Options.DISABLE_CRAFTING, true);
                                    }
                                    customFoodManager.saveOptionToConfig(customFood, Food.Options.DISABLE_CRAFTING);
                                }
                                case DISABLE_SMELTING -> {
                                    boolean b = (boolean) customFood.getOptionValue(Food.Options.DISABLE_SMELTING);
                                    if (b) {
                                        customFood.setOption(Food.Options.DISABLE_SMELTING, false);
                                    } else {
                                        customFood.setOption(Food.Options.DISABLE_SMELTING, true);
                                    }
                                    customFoodManager.saveOptionToConfig(customFood, Food.Options.DISABLE_SMELTING);
                                }
                                case DISABLE_ANVIL -> {
                                    boolean b = (boolean) customFood.getOptionValue(Food.Options.DISABLE_ANVIL);
                                    if (b) {
                                        customFood.setOption(Food.Options.DISABLE_ANVIL, false);
                                    } else {
                                        customFood.setOption(Food.Options.DISABLE_ANVIL, true);
                                    }
                                    customFoodManager.saveOptionToConfig(customFood, Food.Options.DISABLE_ANVIL);
                                }
                                case DISABLE_ENCHANT -> {
                                    boolean b = (boolean) customFood.getOptionValue(Food.Options.DISABLE_ENCHANT);
                                    if (b) {
                                        customFood.setOption(Food.Options.DISABLE_ENCHANT, false);
                                    } else {
                                        customFood.setOption(Food.Options.DISABLE_ENCHANT, true);
                                    }
                                    customFoodManager.saveOptionToConfig(customFood, Food.Options.DISABLE_ENCHANT);
                                }
                                case SOUND -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the sound");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.SOUND, Food.Options.SOUND.getBaseValue());
                                    }
                                }
                                case POTION_COLOR -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the potion color");
                                        player.sendMessage(ChatColor.GRAY + "ex. #ffffff");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.POTION_COLOR, Food.Options.POTION_COLOR.getBaseValue());
                                    }
                                }
                                case HIDE_POTION_EFFECT -> {
                                    boolean b = (boolean) customFood.getOptionValue(Food.Options.HIDE_POTION_EFFECT);
                                    if (b) {
                                        customFood.setOption(Food.Options.HIDE_POTION_EFFECT, false);
                                    } else {
                                        customFood.setOption(Food.Options.HIDE_POTION_EFFECT, true);
                                    }
                                    customFoodManager.saveOptionToConfig(customFood, Food.Options.HIDE_POTION_EFFECT);
                                }
                                case UNSTACKABLE -> {
                                    boolean b = (boolean) customFood.getOptionValue(Food.Options.UNSTACKABLE);
                                    if (b) {
                                        customFood.setOption(Food.Options.UNSTACKABLE, false);
                                    } else {
                                        customFood.setOption(Food.Options.UNSTACKABLE, true);
                                    }
                                    customFoodManager.saveOptionToConfig(customFood, Food.Options.UNSTACKABLE);
                                }
                                case INSTANT_EAT -> {
                                    boolean b = (boolean) customFood.getOptionValue(Food.Options.INSTANT_EAT);
                                    if (b) {
                                        customFood.setOption(Food.Options.INSTANT_EAT, false);
                                    } else {
                                        customFood.setOption(Food.Options.INSTANT_EAT, true);
                                    }
                                    customFoodManager.saveOptionToConfig(customFood, Food.Options.INSTANT_EAT);
                                }
                                case ALWAYS_EAT -> {
                                    boolean b = (boolean) customFood.getOptionValue(Food.Options.ALWAYS_EAT);
                                    if (b) {
                                        customFood.setOption(Food.Options.ALWAYS_EAT, false);
                                    } else {
                                        customFood.setOption(Food.Options.ALWAYS_EAT, true);
                                    }
                                    customFoodManager.saveOptionToConfig(customFood, Food.Options.ALWAYS_EAT);
                                }
                                case EAT_SECONDS -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the eat seconds");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.EAT_SECONDS, Food.Options.EAT_SECONDS.getBaseValue());
                                    }
                                }
                                case MAX_STACK_SIZE -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the max stack size");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("CustomFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        customFood.setOption(Food.Options.MAX_STACK_SIZE, Food.Options.MAX_STACK_SIZE.getBaseValue());
                                    }
                                }
                                case HIDE_ADDITIONAL_TOOLTIP -> {
                                    boolean b = (boolean) customFood.getOptionValue(Food.Options.HIDE_ADDITIONAL_TOOLTIP);
                                    if (b) {
                                        customFood.setOption(Food.Options.HIDE_ADDITIONAL_TOOLTIP, false);
                                    } else {
                                        customFood.setOption(Food.Options.HIDE_ADDITIONAL_TOOLTIP, true);
                                    }
                                    customFoodManager.saveOptionToConfig(customFood, Food.Options.HIDE_ADDITIONAL_TOOLTIP);
                                }
                            }
                            if (open) {
                                playerData.setTempData("CustomFood_Edit_Key", internalName);
                                customFoodManager.openCustomFoodEditGui(CustomFoodEditGui.Type.EDIT, player);
                            }
                        }
                    }
                }
            }
        }
    }

}
