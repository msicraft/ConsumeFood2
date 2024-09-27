package me.msicraft.consumefood2.Utils;

import me.msicraft.API.CommonAPI;
import me.msicraft.API.CustomException.InvalidFormat;
import me.msicraft.API.CustomException.MigrationFail;
import me.msicraft.API.CustomException.UnknownPotionEffectType;
import me.msicraft.API.Food.CustomFood;
import me.msicraft.API.Food.Food;
import me.msicraft.API.Food.FoodCommand;
import me.msicraft.API.Food.FoodPotionEffect;
import me.msicraft.consumefood.File.CustomFoodConfig;
import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.util.UUID;

public class MigrationUtil {

    private MigrationUtil() {}

    public static CustomFood getOldCustomFoodMigration(CustomFoodConfig customFoodConfig, String internalName) throws MigrationFail {
        if (ConsumeFood2.getPlugin().getCustomFoodManager().hasCustomFood(internalName)) {
            throw new MigrationFail("Internalname already exists", internalName);
        }
        FileConfiguration oldConfig = customFoodConfig.getConfig();
        String path = "CustomFood." + internalName;
        Material material = Material.APPLE;
        if (oldConfig.contains(path + ".Material")) {
            String m = oldConfig.getString(path + ".Material");
            Material ma = Material.getMaterial(m.toUpperCase());
            if (ma == null) {
                throw new MigrationFail("Unknown Material", internalName);
            }
            material = ma;
        }
        CustomFood customFood = new CustomFood(material, internalName);
        for (Food.Options options : Food.Options.values()) {
            if (options == Food.Options.MATERIAL || options.getOldPath() == null) {
                continue;
            }
            String oldPath = path + "." + options.getOldPath();
            if (oldConfig.contains(oldPath)) {
                switch (options) {
                    case TEXTURE_VALUE, DISPLAYNAME, SOUND, POTION_COLOR, MAX_STACK_SIZE -> {
                        customFood.setOption(options, oldConfig.getString(oldPath, (String) options.getBaseValue()));
                    }
                    case CUSTOM_MODEL_DATA, FOOD_LEVEL -> {
                        customFood.setOption(options, oldConfig.getInt(oldPath, (int) options.getBaseValue()));
                    }
                    case SATURATION, COOLDOWN, EAT_SECONDS -> {
                        customFood.setOption(options, oldConfig.getDouble(oldPath, (double) options.getBaseValue()));
                    }
                    case HIDE_ENCHANT, DISABLE_CRAFTING, DISABLE_SMELTING, DISABLE_ANVIL, DISABLE_ENCHANT
                    , HIDE_POTION_EFFECT, HIDE_ADDITIONAL_TOOLTIP, UNSTACKABLE, INSTANT_EAT, ALWAYS_EAT -> {
                        customFood.setOption(options, oldConfig.getBoolean(oldPath, (boolean) options.getBaseValue()));
                    }
                    case LORE -> {
                        oldConfig.getStringList(oldPath).forEach(customFood::addLore);
                    }
                    case UUID -> {
                        if (material == Material.PLAYER_HEAD) {
                            String uuidS = oldConfig.getString(oldPath, UUID.randomUUID().toString());
                            if (uuidS == null) {
                                uuidS = UUID.randomUUID().toString();
                            }
                            customFood.setOption(options, UUID.fromString(uuidS));
                        }
                    }
                    case POTION_EFFECT -> {
                        oldConfig.getStringList(oldPath).forEach(format -> {
                            try {
                                FoodPotionEffect foodPotionEffect = CommonAPI.getInstance().formatToFoodPotionEffect(format);
                                customFood.addPotionEffect(foodPotionEffect);
                            } catch (InvalidFormat | UnknownPotionEffectType e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    case COMMAND -> {
                        oldConfig.getStringList(oldPath).forEach(format -> {
                            try {
                                FoodCommand foodCommand = CommonAPI.getInstance().formatToFoodCommand(format);
                                customFood.addCommand(foodCommand);
                            } catch (InvalidFormat e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    case ENCHANT -> {
                        oldConfig.getStringList(oldPath).forEach(format -> {
                            try {
                                String[] a = format.split(":");
                                String enchantS = a[0];
                                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantS));
                                int level = Integer.parseInt(a[1]);
                                customFood.addEnchantment(enchantment, level);
                            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.RED + "=====Migrate Error: Invalid Enchant format=====");
                                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "InternalName: " + internalName);
                                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Invalid line: " + format);
                                Bukkit.getConsoleSender().sendMessage(ConsumeFood2.PREFIX + ChatColor.YELLOW + "Format: <enchant>:<level>");
                            }
                        });
                    }
                }
            }
        }
        return customFood;
    }

}
