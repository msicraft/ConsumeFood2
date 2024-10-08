package me.msicraft.consumefood2.VanillaFood.Event;

import me.clip.placeholderapi.PlaceholderAPI;
import me.msicraft.API.CoolDownType;
import me.msicraft.API.CustomEvent.VanillaFoodConsumeEvent;
import me.msicraft.API.Food.Food;
import me.msicraft.API.Food.VanillaFood;
import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.Utils.MessageUtil;
import me.msicraft.consumefood2.Utils.PlayerUtil;
import me.msicraft.consumefood2.VanillaFood.VanillaFoodManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VanillaFoodRelatedEvent implements Listener {

    private final ConsumeFood2 plugin;
    private final VanillaFoodManager vanillaFoodManager;

    private final Map<UUID, Long> globalCooldownMap = new HashMap<>();
    private final Map<UUID, Map<Material, Long>> personalCooldownMap = new HashMap<>();

    public VanillaFoodRelatedEvent(ConsumeFood2 plugin) {
        this.plugin = plugin;
        this.vanillaFoodManager = plugin.getVanillaFoodManager();
    }

    @EventHandler
    public void vanillaFoodConsumeEvent(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ItemStack itemStack = e.getItem();
        if (vanillaFoodManager.isVanillaFood(itemStack) && !plugin.getCustomFoodManager().isCustomFood(itemStack)) {
            VanillaFood vanillaFood = vanillaFoodManager.getVanillaFood(itemStack.getType());
            if (vanillaFood != null) {
                e.setCancelled(true);
                EquipmentSlot hand = PlayerUtil.getUseHand(player, itemStack);
                vanillaFoodConsume(player, itemStack, vanillaFood, hand);
            }
        }
    }

    @EventHandler
    public void vanillaFoodInstantConsumeEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack itemStack = e.getItem();
        if (itemStack != null) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR) {
                if (vanillaFoodManager.isVanillaFood(itemStack) && !plugin.getCustomFoodManager().isCustomFood(itemStack)) {
                    VanillaFood vanillaFood = vanillaFoodManager.getVanillaFood(itemStack.getType());
                    if (vanillaFood != null) {
                        boolean instantEat = (boolean) vanillaFood.getOptionValue(Food.Options.INSTANT_EAT);
                        if (instantEat) {
                            e.setCancelled(true);
                            vanillaFoodConsume(player, itemStack, vanillaFood, e.getHand());
                        }
                    }
                }
            }
        }
    }

    private void vanillaFoodConsume(Player player, ItemStack itemStack, VanillaFood vanillaFood, EquipmentSlot hand) {
        CoolDownType coolDownType = vanillaFoodManager.getCoolDownType();
        UUID playerUUID = player.getUniqueId();
        long time = System.currentTimeMillis();
        switch (coolDownType) {
            case DISABLE -> {
                vanillaFoodManager.consumeVanillaFood(player, vanillaFood, hand);
            }
            case GLOBAL -> {
                if (globalCooldownMap.containsKey(playerUUID)) {
                    if (globalCooldownMap.get(playerUUID) > time) {
                        long left = (globalCooldownMap.get(playerUUID) - time) / 1000;
                        String message = MessageUtil.getConfigMessage("VanillaFood-Global-Cooldown-Left", true);
                        if (message != null && !message.isEmpty()) {
                            message = message.replaceAll("%vanillafood_name%", (String) vanillaFood.getOptionValue(Food.Options.DISPLAYNAME));
                            message = message.replaceAll("%vanillafood_global_timeleft%", String.valueOf(left));
                            message = PlaceholderAPI.setPlaceholders(player, message);
                            player.sendMessage(message);
                        }

                        Bukkit.getPluginManager().callEvent(new VanillaFoodConsumeEvent(false, left,
                                player, hand, vanillaFood));
                        return;
                    }
                }
                globalCooldownMap.put(playerUUID, (long) (time + (vanillaFoodManager.getGlobalCoolDown()) * 1000));
                vanillaFoodManager.consumeVanillaFood(player, vanillaFood, hand);
            }
            case PERSONAL -> {
                double foodCooldown = (double) vanillaFood.getOptionValue(Food.Options.COOLDOWN);
                Map<Material, Long> temp = personalCooldownMap.getOrDefault(playerUUID, new HashMap<>());
                if (temp.containsKey(itemStack.getType()) && temp.get(itemStack.getType()) > time) {
                    long left = (temp.get(itemStack.getType()) - time) / 1000;
                    String message = MessageUtil.getConfigMessage("VanillaFood-Global-Cooldown-Left", true);
                    if (message != null && !message.isEmpty()) {
                        message = message.replaceAll("%vanillafood_name%", (String) vanillaFood.getOptionValue(Food.Options.DISPLAYNAME));
                        message = message.replaceAll("%vanillafood_personal_timeleft%", String.valueOf(left));
                        message = PlaceholderAPI.setPlaceholders(player, message);
                        player.sendMessage(message);
                    }

                    Bukkit.getPluginManager().callEvent(new VanillaFoodConsumeEvent(false, left,
                            player, hand, vanillaFood));
                    return;
                }
                temp.put(itemStack.getType(), (long) (time + (foodCooldown * 1000)));
                personalCooldownMap.put(playerUUID, temp);
                vanillaFoodManager.consumeVanillaFood(player, vanillaFood, hand);
            }
        }
    }

}
