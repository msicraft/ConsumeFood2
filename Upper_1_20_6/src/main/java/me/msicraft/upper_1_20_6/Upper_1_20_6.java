package me.msicraft.upper_1_20_6;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import me.msicraft.API.Food.CustomFood;
import me.msicraft.API.Food.Food;
import me.msicraft.API.Food.FoodPotionEffect;
import me.msicraft.API.Wrapper;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Upper_1_20_6 implements Wrapper {

    private static Upper_1_20_6 instance;

    private Upper_1_20_6() {
    }

    public static Upper_1_20_6 getInstance() {
        if (instance == null) {
            instance = new Upper_1_20_6();
        }
        return instance;
    }

    @Override
    public ItemStack createCustomFoodItemStack(CustomFood customFood, Map<String, NamespacedKey> namespacedKeyMap) {
        ItemStack itemStack = new ItemStack((Material) customFood.getOptionValue(Food.Options.MATERIAL));
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        FoodComponent foodComponent = itemMeta.getFood();

        if (customFood.hasOption(Food.Options.DISPLAYNAME)) {
            String displayName = (String) customFood.getOptionValue(Food.Options.DISPLAYNAME);
            displayName = translateColorCodes(displayName);
            itemMeta.setDisplayName(displayName);
        }
        if (customFood.hasOption(Food.Options.CUSTOM_MODEL_DATA)) {
            itemMeta.setCustomModelData((int) customFood.getOptionValue(Food.Options.CUSTOM_MODEL_DATA));
        }
        List<String> lore = new ArrayList<>(customFood.getLore().size());
        for (String s : customFood.getLore()) {
            s = translateColorCodes(s);
            lore.add(s);
        }
        itemMeta.setLore(lore);

        if ((boolean) customFood.getOptionValue(Food.Options.HIDE_ENCHANT)) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        //if ((boolean) customFood.getOptionValue(Food.Options.HIDE_POTION_EFFECT)) {}
        if ((boolean) customFood.getOptionValue(Food.Options.UNSTACKABLE)) {
            dataContainer.set(namespacedKeyMap.get("UnStackable"), PersistentDataType.STRING, UUID.randomUUID().toString());
        }

        if (customFood.hasOption(Food.Options.FOOD_LEVEL)) {
            foodComponent.setNutrition((int) customFood.getOptionValue(Food.Options.FOOD_LEVEL));
        }
        if (customFood.hasOption(Food.Options.SATURATION)) {
            double saturationD = (double) customFood.getOptionValue(Food.Options.SATURATION);
            foodComponent.setSaturation((float) saturationD);
        }
        if (customFood.hasOption(Food.Options.EAT_SECONDS)) {
            foodComponent.setEatSeconds((int) customFood.getOptionValue(Food.Options.EAT_SECONDS));
        }
        foodComponent.setCanAlwaysEat((boolean) customFood.getOptionValue(Food.Options.ALWAYS_EAT));
        for (FoodPotionEffect foodPotionEffect : customFood.getPotionEffects()) {
            foodComponent.addEffect(foodPotionEffect.getPotionEffect(), Float.parseFloat(String.valueOf(foodPotionEffect.getChance())));
        }

        dataContainer.set(namespacedKeyMap.get("CustomFood"), PersistentDataType.STRING, customFood.getInternalName());

        itemMeta.setFood(foodComponent);
        itemStack.setItemMeta(itemMeta);

        for (Enchantment enchantment : customFood.getEnchantments()) {
            int level = customFood.getEnchantmentLevel(enchantment);
            itemStack.addUnsafeEnchantment(enchantment, level);
        }

        if (itemStack.getType() == Material.PLAYER_HEAD) {
            NBT.modifyComponents(itemStack, nbt -> {
                ReadWriteNBT profileNbt = nbt.getOrCreateCompound("minecraft:profile");
                profileNbt.setUUID("id", (UUID) customFood.getOptionValue(Food.Options.UUID));
                ReadWriteNBT propertiesNbt = profileNbt.getCompoundList("properties").addCompound();
                propertiesNbt.setString("name", "textures");
                propertiesNbt.setString("value", (String) customFood.getOptionValue(Food.Options.TEXTURE_VALUE));
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

}
