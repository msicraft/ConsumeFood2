package me.msicraft.consumefood2.CustomFood.Event;

import me.clip.placeholderapi.PlaceholderAPI;
import me.msicraft.API.CoolDownType;
import me.msicraft.API.CustomEvent.CustomFoodConsumeEvent;
import me.msicraft.API.Food.CustomFood;
import me.msicraft.API.Food.Food;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.CustomFood.CustomFoodManager;
import me.msicraft.consumefood2.Utils.MessageUtil;
import me.msicraft.consumefood2.Utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomFoodRelatedEvent implements Listener {

    private final ConsumeFood2 plugin;
    private final CustomFoodManager customFoodManager;

    private final Map<UUID, Long> globalCooldownMap = new HashMap<>();
    private final Map<UUID, Map<String, Long>> personalCooldownMap = new HashMap<>();

    public CustomFoodRelatedEvent(ConsumeFood2 plugin) {
        this.plugin = plugin;
        this.customFoodManager = plugin.getCustomFoodManager();
    }

    @EventHandler
    public void customFoodConsumeEvent(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ItemStack itemStack = e.getItem();
        String internalName = customFoodManager.getInternalName(itemStack);
        if (internalName != null) {
            CustomFood customFood = customFoodManager.getCustomFood(internalName);
            if (customFood != null) {
                if (plugin.isUseFoodComponent()) {
                    customFoodConsume(player, customFood, null, true);
                    customFoodManager.applyExecuteCommands(player, customFood);
                    return;
                }
                e.setCancelled(true);
                EquipmentSlot hand = PlayerUtil.getUseHand(player, itemStack);
                customFoodConsume(player, customFood, hand, false);
            }
        }
    }

    @EventHandler
    public void customFoodInstantConsumeEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack itemStack = e.getItem();
        if (itemStack != null) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR) {
                String internalName = customFoodManager.getInternalName(itemStack);
                if (internalName != null) {
                    CustomFood customFood = customFoodManager.getCustomFood(internalName);
                    if (customFood != null) {
                        boolean instantEat = (boolean) customFood.getOptionValue(Food.Options.INSTANT_EAT);
                        if (instantEat) {
                            e.setCancelled(true);
                            customFoodConsume(player, customFood, e.getHand(), false);
                        }
                    }
                }
            }
        }
    }

    private void customFoodConsume(Player player, CustomFood customFood, EquipmentSlot hand, boolean useFoodComponent) {
        CoolDownType coolDownType = customFoodManager.getCoolDownType();
        UUID playerUUID = player.getUniqueId();
        long time = System.currentTimeMillis();
        switch (coolDownType) {
            case DISABLE -> {
                customFoodManager.consumeCustomFood(player, customFood, hand, useFoodComponent);
            }
            case GLOBAL -> {
                if (globalCooldownMap.containsKey(playerUUID)) {
                    if (globalCooldownMap.get(playerUUID) > time) {
                        long left = (globalCooldownMap.get(playerUUID) - time) / 1000;
                        String message = MessageUtil.getConfigMessage("CustomFood-Global-Cooldown-Left", true);
                        if (message != null && !message.isEmpty()) {
                            message = message.replaceAll("%customfood_name%", (String) customFood.getOptionValue(Food.Options.DISPLAYNAME));
                            message = message.replaceAll("%customfood_global_time_left%", String.valueOf(left));
                            message = PlaceholderAPI.setPlaceholders(player, message);
                            player.sendMessage(message);
                        }

                        Bukkit.getPluginManager().callEvent(new CustomFoodConsumeEvent(false, left,
                                player, hand, customFood));
                        return;
                    }
                }
                globalCooldownMap.put(playerUUID, (long) (time + (customFoodManager.getGlobalCoolDown()) * 1000));
                customFoodManager.consumeCustomFood(player, customFood, hand, useFoodComponent);
            }
            case PERSONAL -> {
                String internalName = customFood.getInternalName();
                double foodCooldown = (double) customFood.getOptionValue(Food.Options.COOLDOWN);
                Map<String, Long> temp = personalCooldownMap.getOrDefault(playerUUID, new HashMap<>());
                if (temp.containsKey(internalName) && temp.get(internalName) > time) {
                    long left = (temp.get(internalName) - time) / 1000;
                    String message = MessageUtil.getConfigMessage("CustomFood-Personal-Cooldown-Left", true);
                    if (message != null && !message.isEmpty()) {
                        message = message.replaceAll("%customfood_name%", (String) customFood.getOptionValue(Food.Options.DISPLAYNAME));
                        message = message.replaceAll("%customfood_personal_time_left%", String.valueOf(left));
                        message = PlaceholderAPI.setPlaceholders(player, message);
                        player.sendMessage(message);
                    }

                    Bukkit.getPluginManager().callEvent(new CustomFoodConsumeEvent(false, left,
                            player, hand, customFood));
                    return;
                }
                temp.put(internalName, (long) (time + (foodCooldown * 1000)));
                personalCooldownMap.put(playerUUID, temp);
                customFoodManager.consumeCustomFood(player, customFood, hand, useFoodComponent);
            }
        }
    }

    @EventHandler
    public void disableCrafting(PrepareItemCraftEvent e) {
        ItemStack[] itemStacks = e.getInventory().getMatrix();
        for (ItemStack itemStack : itemStacks) {
            String internalName = customFoodManager.getInternalName(itemStack);
            if (internalName != null) {
                CustomFood customFood = customFoodManager.getCustomFood(internalName);
                if (customFood != null) {
                    if ((boolean) customFood.getOptionValue(Food.Options.DISABLE_CRAFTING)) {
                        e.getInventory().setResult(null);
                    }
                }
            }
        }
    }

    @EventHandler
    public void disableSmelting(FurnaceSmeltEvent e) {
        ItemStack itemStack = e.getSource();
        String internalName = customFoodManager.getInternalName(itemStack);
        if (internalName != null) {
            CustomFood customFood = customFoodManager.getCustomFood(internalName);
            if (customFood != null) {
                if ((boolean) customFood.getOptionValue(Food.Options.DISABLE_SMELTING)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void disableSmelting2(FurnaceBurnEvent e) {
        ItemStack itemStack = e.getFuel();
        String internalName = customFoodManager.getInternalName(itemStack);
        if (internalName != null) {
            CustomFood customFood = customFoodManager.getCustomFood(internalName);
            if (customFood != null) {
                if ((boolean) customFood.getOptionValue(Food.Options.DISABLE_SMELTING)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void disableAnvil(PrepareAnvilEvent e) {
        ItemStack[] itemStacks = e.getInventory().getStorageContents();
        for (ItemStack itemStack : itemStacks) {
            String internalName = customFoodManager.getInternalName(itemStack);
            if (internalName != null) {
                CustomFood customFood = customFoodManager.getCustomFood(internalName);
                if (customFood != null) {
                    if ((boolean) customFood.getOptionValue(Food.Options.DISABLE_ANVIL)) {
                        e.setResult(null);
                    }
                }
            }
        }
    }

    @EventHandler
    public void disableEnchant(PrepareItemEnchantEvent e) {
        ItemStack[] itemStacks = e.getInventory().getStorageContents();
        for (ItemStack itemStack : itemStacks) {
            String internalName = customFoodManager.getInternalName(itemStack);
            if (internalName != null) {
                CustomFood customFood = customFoodManager.getCustomFood(internalName);
                if (customFood != null) {
                    if ((boolean) customFood.getOptionValue(Food.Options.DISABLE_ENCHANT)) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void disablePlayerHeadPlace(BlockPlaceEvent e) {
        if (customFoodManager.isDisablePlayerHeadPlace()) {
            ItemStack itemStack = e.getItemInHand();
            if (customFoodManager.isCustomFood(itemStack)) {
                e.setCancelled(true);
            }
        }
    }

}
