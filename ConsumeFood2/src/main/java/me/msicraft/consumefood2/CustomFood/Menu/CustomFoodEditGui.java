package me.msicraft.consumefood2.CustomFood.Menu;

import me.msicraft.API.ConsumeFood2API;
import me.msicraft.API.Data.CustomGui;
import me.msicraft.API.Food.CustomFood;
import me.msicraft.API.Food.Food;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.CustomFood.CustomFoodManager;
import me.msicraft.consumefood2.PlayerData.Data.PlayerData;
import me.msicraft.consumefood2.Utils.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomFoodEditGui extends CustomGui {

    public enum Type {
        SELECT, EDIT
    }

    private final Inventory gui;
    private final ConsumeFood2 plugin;

    private final NamespacedKey selectKey;
    private final NamespacedKey editKey;

    public CustomFoodEditGui(ConsumeFood2 plugin) {
        this.plugin = plugin;
        this.gui = Bukkit.createInventory(this, 54, "CustomFood Edit");

        this.selectKey = new NamespacedKey(plugin, "CustomFood_Select");
        this.editKey = new NamespacedKey(plugin, "CustomFood_Edit");
    }

    public void setGui(Type type, Player player) {
        gui.clear();
        switch (type) {
            case SELECT -> {
                setSelectGui(player);
            }
            case EDIT -> {
                setEditGui(player);
            }
        }
    }

    private void setSelectGui(Player player) {
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.ARROW, "Next", GuiUtil.EMPTY_LORE, -1, selectKey, "Next");
        gui.setItem(50, itemStack);
        itemStack = GuiUtil.createItemStack(Material.ARROW, "Previous", GuiUtil.EMPTY_LORE, -1, selectKey, "Previous");
        gui.setItem(48, itemStack);

        CustomFoodManager customFoodManager = plugin.getCustomFoodManager();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);

        List<String> internalNames = customFoodManager.getAllInternalNames();
        int maxSize = internalNames.size();
        int page = (int) playerData.getTempData("CustomFood_Select_Page", 0);
        int guiCount = 0;
        int lastCount = page * 45;

        String pageS = "Page: " + (page + 1) + "/" + ((maxSize / 45) + 1);
        itemStack = GuiUtil.createItemStack(Material.BOOK, pageS, GuiUtil.EMPTY_LORE, -1, selectKey, "Page");
        gui.setItem(49, itemStack);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Left Click: edit");
        lore.add(ChatColor.YELLOW + "Right Click: get item");
        for (int a = lastCount; a < maxSize; a++) {
            String internalName = internalNames.get(a);
            CustomFood customFood = customFoodManager.getCustomFood(internalName);
            if (customFood != null) {
                itemStack = customFood.getGuiItemStack(plugin.isUseFoodComponent());
                ItemMeta itemMeta = itemStack.getItemMeta();
                PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                itemMeta.setLore(lore);
                dataContainer.set(selectKey, PersistentDataType.STRING, internalName);

                itemStack.setItemMeta(itemMeta);
                gui.setItem(guiCount, itemStack);
                guiCount++;
                if (guiCount >= 45) {
                    break;
                }
            }
        }
    }

    private final int[] editSlots = new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25,
            28,29,30,31,32,33,34, 37,38,39,40,41,42,43, 46,47,48,49,50,51,52};

    private void setEditGui(Player player) {
        CustomFoodManager customFoodManager = plugin.getCustomFoodManager();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        if (!playerData.hasTempData("CustomFood_Edit_Key")) {
            player.sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "internalName does not exist");
            player.closeInventory();
            return;
        }
        String internalName = (String) playerData.getTempData("CustomFood_Edit_Key");
        CustomFood customFood = customFoodManager.getCustomFood(internalName);
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.BARRIER, ChatColor.WHITE + "Back", GuiUtil.EMPTY_LORE, -1,
                editKey, "Back");
        gui.setItem(0, itemStack);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Right Click: get item");
        lore.add("");
        for (String s : customFood.getLore()) {
            s = ConsumeFood2API.getInstance().translateColorCodes(s);
            lore.add(s);
        }
        itemStack = customFoodManager.createItemStack(customFood);
        ItemMeta tItemMeta = itemStack.getItemMeta();
        PersistentDataContainer tDataContainer = tItemMeta.getPersistentDataContainer();
        tDataContainer.set(editKey, PersistentDataType.STRING, "Edit_Item");
        tItemMeta.setLore(lore);
        itemStack.setItemMeta(tItemMeta);
        gui.setItem(4, itemStack);

        boolean upper_1_20_5 = plugin.isUseFoodComponent();
        int count = 0;
        Food.Options[] foodOptions = Food.Options.values();
        for (Food.Options options : foodOptions) {
            if (options == Food.Options.UUID) {
                continue;
            }
            lore.clear();
            ItemMeta itemMeta;

            lore.add(ChatColor.YELLOW + "Left Click: edit value (change value)");
            lore.add(ChatColor.YELLOW + "Right Click: reset");
            lore.add("");
            lore.add(ChatColor.GRAY + "Set " + options.getDisplayName());
            for (String s : options.getDescription()) {
                lore.add(ChatColor.WHITE + s);
            }
            lore.add("");
            switch (options) {
                case MATERIAL -> {
                    itemStack = new ItemStack(customFood.getMaterial());
                    lore.add(ChatColor.GRAY + "Current Material: " + customFood.getMaterial().name());
                }
                case TEXTURE_VALUE -> {
                    itemStack = new ItemStack(Material.PLAYER_HEAD);
                    itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.WHITE + "TextureValue");
                    lore.add(ChatColor.GRAY + "Current Texture Value: " + customFood.getOptionValue(Food.Options.TEXTURE_VALUE));
                }
                case DISPLAYNAME -> {
                    itemStack = new ItemStack(Material.OAK_SIGN);
                    lore.add(ChatColor.GRAY + "Current DisplayName: " + customFood.getOptionValue(Food.Options.DISPLAYNAME));
                }
                case CUSTOM_MODEL_DATA -> {
                    itemStack = new ItemStack(Material.NAME_TAG);
                    lore.add(ChatColor.GRAY + "Current Custom Model Data: " + customFood.getOptionValue(Food.Options.CUSTOM_MODEL_DATA));
                }
                case LORE -> {
                    itemStack = new ItemStack(Material.PAPER);
                    lore.add(ChatColor.GRAY + "Current Lore: ");
                    for (String s : customFood.getLore()) {
                        s = ConsumeFood2API.getInstance().translateColorCodes(s);
                        lore.add(s);
                    }
                }
                case POTION_EFFECT -> {
                    itemStack = new ItemStack(Material.POTION);
                    lore.add(ChatColor.GRAY + "Format: <potionType>:<level>:<duration>:<chance>");
                    lore.add(ChatColor.GRAY + "Current Potion Effect: ");
                    customFood.getPotionEffects().forEach(foodPotionEffect -> {
                        lore.add(ChatColor.GRAY + foodPotionEffect.toFormat());
                    });
                }
                case COMMAND -> {
                    itemStack = new ItemStack(Material.COMMAND_BLOCK);
                    lore.add(ChatColor.GRAY + "Format: <executeType>:<command>");
                    lore.add(ChatColor.GRAY + "Current Command: ");
                    customFood.getCommands().forEach(foodCommand -> {
                        lore.add(ChatColor.GRAY + foodCommand.toFormat());
                    });
                }
                case FOOD_LEVEL -> {
                    itemStack = new ItemStack(Material.PORKCHOP);
                    lore.add(ChatColor.GRAY + "Current Food Level: " + customFood.getOptionValue(Food.Options.FOOD_LEVEL));
                }
                case SATURATION -> {
                    itemStack = new ItemStack(Material.COOKED_PORKCHOP);
                    lore.add(ChatColor.GRAY + "Current Saturation: " + customFood.getOptionValue(Food.Options.SATURATION));
                }
                case COOLDOWN -> {
                    itemStack = new ItemStack(Material.COMPASS);
                    lore.add(ChatColor.GRAY + "Current Cooldown: " + customFood.getOptionValue(Food.Options.COOLDOWN));
                }
                case ENCHANT -> {
                    itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                    lore.add(ChatColor.GRAY + "Format: <enchant>:<level>");
                    lore.add(ChatColor.GRAY + "Current Enchant: ");
                    lore.addAll(customFood.getEnchantFormatList());
                }
                case HIDE_ENCHANT -> {
                    itemStack = new ItemStack(Material.ENCHANTING_TABLE);
                    lore.add(ChatColor.GRAY + "Current HideEnchant: " + customFood.getOptionValue(Food.Options.HIDE_ENCHANT));
                }
                case DISABLE_CRAFTING -> {
                    itemStack = new ItemStack(Material.CRAFTING_TABLE);
                    lore.add(ChatColor.GRAY + "Current Disable Crafting: " + customFood.getOptionValue(Food.Options.DISABLE_CRAFTING));
                }
                case DISABLE_SMELTING -> {
                    itemStack = new ItemStack(Material.FURNACE);
                    lore.add(ChatColor.GRAY + "Current Disable Smelting: " + customFood.getOptionValue(Food.Options.DISABLE_SMELTING));
                }
                case DISABLE_ANVIL -> {
                    itemStack = new ItemStack(Material.ANVIL);
                    lore.add(ChatColor.GRAY + "Current Disable Anvil: " + customFood.getOptionValue(Food.Options.DISABLE_ANVIL));
                }
                case DISABLE_ENCHANT -> {
                    itemStack = new ItemStack(Material.ENCHANTING_TABLE);
                    lore.add(ChatColor.GRAY + "Current Disable Enchant: " + customFood.getOptionValue(Food.Options.DISABLE_ENCHANT));
                }
                case SOUND -> {
                    itemStack = new ItemStack(Material.JUKEBOX);
                    lore.add(ChatColor.GRAY + "Current Sound: " + customFood.getOptionValue(Food.Options.SOUND));
                }
                case POTION_COLOR -> {
                    itemStack = new ItemStack(Material.SPLASH_POTION);
                    lore.add(ChatColor.GRAY + "Current Potion Color: " + customFood.getOptionValue(Food.Options.POTION_COLOR));
                }
                case HIDE_POTION_EFFECT -> {
                    itemStack = new ItemStack(Material.LINGERING_POTION);
                    lore.add(ChatColor.GRAY + "Current HidePotionEffect: " + customFood.getOptionValue(Food.Options.HIDE_POTION_EFFECT));
                }
                case UNSTACKABLE -> {
                    itemStack = new ItemStack(Material.CHEST);
                    lore.add(ChatColor.GRAY + "Current Unstackable: " + customFood.getOptionValue(Food.Options.UNSTACKABLE));
                }
                case INSTANT_EAT -> {
                    itemStack = new ItemStack(Material.CAKE);
                    lore.add(ChatColor.GRAY + "Current Instant Eat: " + customFood.getOptionValue(Food.Options.INSTANT_EAT));
                }
                case ALWAYS_EAT -> {
                    if (!upper_1_20_5) {
                        continue;
                    }
                    itemStack = new ItemStack(Material.CAKE);
                    lore.add(ChatColor.GRAY + "Current Always Eat: " + customFood.getOptionValue(Food.Options.ALWAYS_EAT));
                }
                case EAT_SECONDS -> {
                    if (!upper_1_20_5) {
                        continue;
                    }
                    itemStack = new ItemStack(Material.KELP);
                    lore.add(ChatColor.GRAY + "Current Eat Seconds: " + customFood.getOptionValue(Food.Options.EAT_SECONDS));
                }
                case MAX_STACK_SIZE -> {
                    if (!upper_1_20_5) {
                        continue;
                    }
                    itemStack = new ItemStack(Material.SHULKER_BOX);
                    lore.add(ChatColor.GRAY + "Current Max Stack Size: " + customFood.getOptionValue(Food.Options.MAX_STACK_SIZE));
                }
                case HIDE_ADDITIONAL_TOOLTIP -> {
                    if (!upper_1_20_5) {
                        continue;
                    }
                    itemStack = new ItemStack(Material.FIREWORK_ROCKET);
                    lore.add(ChatColor.GRAY + "Current Hide Additional Tooltip: " + customFood.getOptionValue(Food.Options.HIDE_ADDITIONAL_TOOLTIP));
                }
            }
            itemMeta = itemStack.getItemMeta();
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            itemMeta.setDisplayName(options.getDisplayName());
            itemMeta.setLore(lore);
            dataContainer.set(editKey,  PersistentDataType.STRING, options.name().toUpperCase());
            itemStack.setItemMeta(itemMeta);

            gui.setItem(editSlots[count], itemStack);
            count++;
        }
    }

    public NamespacedKey getSelectKey() {
        return selectKey;
    }

    public NamespacedKey getEditKey() {
        return editKey;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return gui;
    }

}
