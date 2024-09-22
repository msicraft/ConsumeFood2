package me.msicraft.consumefood2.VanillaFood.Menu.Event;

import me.msicraft.API.Food.Food;
import me.msicraft.API.Food.FoodCommand;
import me.msicraft.API.Food.FoodPotionEffect;
import me.msicraft.API.Food.VanillaFood;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.PlayerData.Data.PlayerData;
import me.msicraft.consumefood2.VanillaFood.Menu.VanillaFoodEditGui;
import me.msicraft.consumefood2.VanillaFood.VanillaFoodManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

public class VanillaFoodEditEvent implements Listener {

    private final ConsumeFood2 plugin;

    public VanillaFoodEditEvent(ConsumeFood2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void vanillaFoodGuiChatEditEvent(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        if (playerData.hasTempData("VanillaFood_ChatEdit")) {
            e.setCancelled(true);
            VanillaFoodManager vanillaFoodManager = plugin.getVanillaFoodManager();
            if (!playerData.hasTempData("VanillaFood_Edit_Key")) {
                player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "material does not exist");
                playerData.removeTempData("VanillaFood_ChatEdit");
                Bukkit.getScheduler().runTask(plugin, () -> {
                    vanillaFoodManager.openVanillaFoodEditGui(VanillaFoodEditGui.Type.SELECT, player);
                });
                return;
            }
            String editData = (String) playerData.getTempData("VanillaFood_ChatEdit");
            Food.Options options = Food.Options.valueOf(editData);
            Material material = Material.getMaterial((String) playerData.getTempData("VanillaFood_Edit_Key"));
            VanillaFood vanillaFood = vanillaFoodManager.getVanillaFood(material);
            String message = e.getMessage();
            if (message.equals("cancel")) {
                playerData.removeTempData("VanillaFood_ChatEdit");
                Bukkit.getScheduler().runTask(plugin, () -> {
                    vanillaFoodManager.openVanillaFoodEditGui(VanillaFoodEditGui.Type.EDIT, player);
                });
                return;
            }
            boolean save = true;
            switch (options) {
                case POTION_EFFECT -> {
                    try {
                        String[] split = message.split(":");
                        PotionEffectType potionEffectType = PotionEffectType.getByName(split[0].toUpperCase());
                        if (potionEffectType != null) {
                            int level = Integer.parseInt(split[1]);
                            int duration = Integer.parseInt(split[2]);
                            double chance = Double.parseDouble(split[3]);
                            FoodPotionEffect foodPotionEffect = new FoodPotionEffect(potionEffectType, level, duration, chance);
                            vanillaFood.addPotionEffect(foodPotionEffect);
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
                        vanillaFood.addCommand(foodCommand);
                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
                        save = false;
                        player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Invalid Command Format=====");
                        player.sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <executeType>:<command>");
                    }
                }
                case FOOD_LEVEL -> {
                    int foodLevel = Integer.parseInt(message);
                    vanillaFood.setOption(Food.Options.FOOD_LEVEL, foodLevel);
                }
                case SATURATION -> {
                    double saturationD = Double.parseDouble(message);
                    vanillaFood.setOption(Food.Options.SATURATION, saturationD);
                }
                case COOLDOWN -> {
                    double cooldown = Double.parseDouble(message);
                    vanillaFood.setOption(Food.Options.COOLDOWN, cooldown);
                }
            }
            if (save) {
                vanillaFoodManager.saveOptionsToConfig(vanillaFood,options);
            }
            playerData.removeTempData("VanillaFood_ChatEdit");
            Bukkit.getScheduler().runTask(plugin, () -> {
                vanillaFoodManager.openVanillaFoodEditGui(VanillaFoodEditGui.Type.EDIT, player);
            });
        }
    }

    @EventHandler
    public void vanillaFoodGuiEvent(InventoryClickEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder() instanceof VanillaFoodEditGui vanillaFoodEditGui) {
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
            VanillaFoodManager vanillaFoodManager = plugin.getVanillaFoodManager();
            NamespacedKey selectKey = vanillaFoodEditGui.getSelectKey();
            NamespacedKey editKey = vanillaFoodEditGui.getEditKey();
            if (dataContainer.has(selectKey, PersistentDataType.STRING)) {
                String data = dataContainer.get(selectKey, PersistentDataType.STRING);
                if (data != null) {
                    int maxSize = vanillaFoodManager.getVanillaFoodMaterials().size();
                    int current = (int) playerData.getTempData("VanillaFood_Select_Page", 1);
                    switch (data) {
                        case "Next" -> {
                            int next = current + 1;
                            if (next > maxSize / 45) {
                                next = 0;
                            }
                            playerData.setTempData("VanillaFood_Select_Page", next);
                            vanillaFoodManager.openVanillaFoodEditGui(VanillaFoodEditGui.Type.SELECT, player);
                        }
                        case "Previous" -> {
                            int previous = current - 1;
                            if (previous < 0) {
                                previous = maxSize / 45;
                            }
                            playerData.setTempData("VanillaFood_Select_Page", previous);
                            vanillaFoodManager.openVanillaFoodEditGui(VanillaFoodEditGui.Type.SELECT, player);
                        }
                        case "Page" -> {}
                        default -> {
                            Material material = Material.getMaterial(data);
                            VanillaFood vanillaFood = vanillaFoodManager.getVanillaFood(material);
                            if (vanillaFood != null) {
                                if (e.isLeftClick()) {
                                    playerData.setTempData("VanillaFood_Edit_Key", data);
                                    vanillaFoodManager.openVanillaFoodEditGui(VanillaFoodEditGui.Type.EDIT, player);
                                } else if (e.isRightClick()) {
                                    ItemStack vanillaFoodStack = new ItemStack(material);
                                    player.getInventory().addItem(vanillaFoodStack);
                                }
                            } else {
                                player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "Invalid Material: " + data);
                            }
                        }
                    }
                }
            } else if (dataContainer.has(editKey, PersistentDataType.STRING)) {
                String data = dataContainer.get(editKey, PersistentDataType.STRING);
                if (data != null) {
                    switch (data) {
                        case "Back" -> {
                            vanillaFoodManager.openVanillaFoodEditGui(VanillaFoodEditGui.Type.SELECT, player);
                            playerData.removeTempData("VanillaFood_Edit_Key");
                        }
                        case "Edit_Item" -> {
                            if (e.isRightClick()) {
                                if (!playerData.hasTempData("VanillaFood_Edit_Key")) {
                                    player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "material does not exist");
                                    player.closeInventory();
                                    return;
                                }
                                Material material = Material.getMaterial((String) playerData.getTempData("VanillaFood_Edit_Key"));
                                ItemStack vanillaFoodStack = new ItemStack(material);
                                player.getInventory().addItem(vanillaFoodStack);
                            }
                        }
                        default -> {
                            if (!playerData.hasTempData("VanillaFood_Edit_Key")) {
                                player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "material does not exist");
                                player.closeInventory();
                                return;
                            }
                            Material material = Material.getMaterial((String) playerData.getTempData("VanillaFood_Edit_Key"));
                            VanillaFood vanillaFood = vanillaFoodManager.getVanillaFood(material);
                            Food.Options options = Food.Options.valueOf(data);
                            boolean open = true;
                            switch (options) {
                                case POTION_EFFECT -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the potion effect");
                                        player.sendMessage(ChatColor.GRAY + "Format: <potionType>:<level>:<duration>:<chance>");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("VanillaFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        vanillaFood.clearPotionEffects();
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
                                        playerData.setTempData("VanillaFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        vanillaFood.clearCommands();
                                    }
                                }
                                case FOOD_LEVEL -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the food level");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("VanillaFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        vanillaFood.setOption(Food.Options.FOOD_LEVEL, Food.Options.FOOD_LEVEL.getBaseValue());
                                    }
                                }
                                case SATURATION -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the saturation");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("VanillaFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        vanillaFood.setOption(Food.Options.SATURATION, Food.Options.SATURATION.getBaseValue());
                                    }
                                }
                                case COOLDOWN -> {
                                    if (e.isLeftClick()) {
                                        open = false;
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "Please enter the cooldown");
                                        player.sendMessage(ChatColor.GRAY + "Cancel when entering 'cancel'");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        playerData.setTempData("VanillaFood_ChatEdit", data);
                                        player.closeInventory();
                                    } else if (e.isRightClick()) {
                                        vanillaFood.setOption(Food.Options.COOLDOWN, Food.Options.COOLDOWN.getBaseValue());
                                    }
                                }
                                case INSTANT_EAT -> {
                                    boolean b = (boolean) vanillaFood.getOptionValue(Food.Options.INSTANT_EAT);
                                    if (b) {
                                        vanillaFood.setOption(Food.Options.INSTANT_EAT, false);
                                    } else {
                                        vanillaFood.setOption(Food.Options.INSTANT_EAT, true);
                                    }
                                    vanillaFoodManager.saveOptionsToConfig(vanillaFood, Food.Options.INSTANT_EAT);
                                }
                            }
                            if (open) {
                                playerData.setTempData("VanillaFood_Edit_Key", material.name());
                                vanillaFoodManager.openVanillaFoodEditGui(VanillaFoodEditGui.Type.EDIT, player);
                            }
                        }
                    }
                }
            }
        }
    }

}
